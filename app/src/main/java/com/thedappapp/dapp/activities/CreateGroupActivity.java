package com.thedappapp.dapp.activities;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.Camera;
import com.thedappapp.dapp.fragments.CreateGroupPage1Fragment;
import com.thedappapp.dapp.fragments.CreateGroupPage2Fragment;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.group.GroupFactory;
import com.thedappapp.dapp.app.FirebaseSaveKeys;

import java.util.List;

public class CreateGroupActivity extends DappActivity
        implements CreateGroupPage1Fragment.Page1FragmentInteractionListener, CreateGroupPage2Fragment.Page2FragmentInteractionListener {

    private FragmentManager fragmentManager;
    private int fragment;
    private Camera camera;
    private CreateGroupPage1Fragment page1;
    private CreateGroupPage2Fragment page2;
    private Bundle page1Bundle, page2Bundle;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        if (getIntent().getExtras().getParcelable("editGroup") == null)
            editMode = false;
        else editMode = true;

        fragmentManager = getSupportFragmentManager();
        fragment = R.id.fragment;
        camera = new Camera();

        if (editMode) {
            page1 = CreateGroupPage1Fragment.newInstance((Group) getIntent().getExtras().getParcelable("editGroup"));
            page2 = CreateGroupPage2Fragment.newInstance((Group) (getIntent().getExtras().getParcelable("editGroup")));
        } else {
            page1 = CreateGroupPage1Fragment.newInstance(null);
            page2 = CreateGroupPage2Fragment.newInstance(null);
        }

        startPage1();
    }

    protected void startPage1 () {
        fragmentManager.beginTransaction()
                .replace(fragment, page1)
                .commitAllowingStateLoss();
    }

    protected void finalizePage1 () {
        page1Bundle = page1.pullInformation();
    }

    protected void startPage2 () {
        fragmentManager.beginTransaction()
                       .replace(fragment, page2)
                       .commitAllowingStateLoss();
    }

    protected void finalizePage2 () {
        page2Bundle = page2.pullInformation();
    }

    protected void buildGroup () {
        String name = page1Bundle.getString("name");
        String bio = page1Bundle.getString("bio");
        //photo
        List<String> interests = page2Bundle.getStringArrayList("interests");

        GroupFactory factory = new GroupFactory();
        Group group = factory.withName(name).withBio(bio).withLeader(FirebaseAuth.getInstance().getCurrentUser().getUid())
                             .useLocation(this).build();

        if (editMode) group.saveToFirebase(FirebaseSaveKeys.UPDATE);
        else group.saveToFirebase(FirebaseSaveKeys.CREATE);
    }

    @Override
    public void onPage1Interaction(CreateGroupPage1Fragment.Page1FragmentInteractionListener.RequestCode code) {
        switch (code) {
            case DISPATCH_CAMERA:
                camera.dispatch(this);
                break;
            case DONE:
                finalizePage1();
                startPage2();
                break;
            default: throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    @Override
    public void onPage2Interaction(CreateGroupPage2Fragment.Page2FragmentInteractionListener.RequestCode code) {
        switch (code) {
            case DONE:
                finalizePage2();
                buildGroup();
            default: throw new IllegalArgumentException("Illegal request code received.");
        }
    }


}