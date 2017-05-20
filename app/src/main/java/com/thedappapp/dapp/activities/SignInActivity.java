package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.interfaces.NoDrawer;
import com.thedappapp.dapp.services.TokenUploadService;

public class SignInActivity extends DappActivity implements NoDrawer {

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int GOOGLE_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private AuthListener listener;
    private LoginButton facebookButton;
    private SignInButton googleButton;
    private CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private CarouselView carousel;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        facebookButton = (LoginButton) findViewById(R.id.login_button);
        googleButton = (SignInButton) findViewById(R.id.google_button);
        loading = (ProgressBar) findViewById(R.id.progressBar);

        carousel = (CarouselView) findViewById(R.id.carouselView);
        carousel.setPageCount(4);

        final String[] carouselText = {"Connect with people near you who share your interests.",
                "Create a group and state what you want to do.", "Send a request to chat with cool people.",
                "If they accept, you can chat!"};

        carousel.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View view = LayoutInflater.from(SignInActivity.this).inflate(R.layout.content_carousel_view, null);
                ((TextView) view.findViewById(R.id.text)).setText(carouselText[position]);
                return view;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        listener = new AuthListener();

        TextView disclaimer = (TextView) findViewById(R.id.disclaimer);
        disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        StringBuilder disclaimerText = new StringBuilder();
        disclaimerText.append("By signing up for Dapp, you<br>agree to Dapp's ");
        disclaimerText.append("<a href=\"http://www.thedappapp.com/user-agreement\">User Agreement</a>");
        disclaimerText.append(" and ");
        disclaimerText.append("<a href=\"http://www.thedappapp.com/privacy-policy\">Privacy Policy</a>.");
        disclaimer.setText(Html.fromHtml(disclaimerText.toString()));
    }

    private void doLoginLoadView (boolean isLoading) {
        loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        googleButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        facebookButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        carousel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
        setupFacebookAuth();
        setupGoogleAuth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
    }

    private void setupGoogleAuth () {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

       mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG, "GoogleApiClient: Connection failed.");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });
    }

    private void setupFacebookAuth() {
        facebookButton.setReadPermissions("email", "public_profile");

        callbackManager = CallbackManager.Factory.create();

        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookSignin(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignin(result);
        }
        else callbackManager.onActivityResult(requestCode, resultCode, data);


    }

    private void handleFacebookSignin(AccessToken token) {
        Log.d(TAG, "handleFacebookSignin:" + token);
        doLoginLoadView(true);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                 .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void handleGoogleSignin(GoogleSignInResult result) {
        doLoginLoadView(true);
        GoogleSignInAccount acct = result.getSignInAccount();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null)
            mAuth.removeAuthStateListener(listener);
    }

    private class AuthListener implements FirebaseAuth.AuthStateListener {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                Intent uploadToken = new Intent(SignInActivity.this, TokenUploadService.class);
                uploadToken.setAction(TokenUploadService.REGISTER_TOKEN);
                startService(uploadToken);

                Intent main = new Intent(SignInActivity.this, MainFeedActivity.class);
                startActivity(main);

                SignInActivity.this.finish();
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                doLoginLoadView(false);
            }
        }

    }
}
