package com.thedappapp.dapp.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.login.LoginManager;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.activities.ChatSelectorActivity;
import com.thedappapp.dapp.activities.MyGroupActivity;
import com.thedappapp.dapp.activities.MapsActivity;
import com.thedappapp.dapp.activities.MainFeedActivity;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.thedappapp.dapp.activities.SignInActivity;
import com.thedappapp.dapp.services.NotificationService;

/**
 * Created by jackson on 7/7/16.
 */
public class Drawer {

    public static final int INVITE = 1;
    public static final int FEED = 3;
    public static final int MAP = 4;
    public static final int CHAT = 5;
    public static final int HOME = 6;
    public static final int LOG_OUT = 8;

    private static final String TAG = Drawer.class.getSimpleName();

    private static com.mikepenz.materialdrawer.Drawer drawer;


    public static com.mikepenz.materialdrawer.Drawer set (DappActivity context, Toolbar toolbar) {
        drawer = new DrawerBuilder().withActivity(context)
                .withToolbar(toolbar)
                .addDrawerItems(Drawer.items())
                .withAccountHeader(Drawer.header(context))
                .withOnDrawerItemClickListener(new Drawer.DrawerListener(context))
                .build();

        return drawer;
    }


    private static IDrawerItem[] items () {
        int red = App.getApp().getResources().getColor(R.color.red);
        int white = App.getApp().getResources().getColor(R.color.white);
        int gradient = com.mikepenz.materialdrawer.R.drawable.material_drawer_badge;
        BadgeStyle style = new BadgeStyle(gradient, red, red, white);

        return new IDrawerItem[] {
                new PrimaryDrawerItem().withName("Invite your friends!").withIcon(R.drawable.ic_share_black_24dp),
                new DividerDrawerItem(),
                new PrimaryDrawerItem().withName("Feed").withIcon(R.drawable.ic_whatshot_black_24dp),
                new PrimaryDrawerItem().withName("Map").withIcon(R.drawable.ic_map_black_24dp),

                NotificationService.getChatInt() != 0 ?
                    new PrimaryDrawerItem().withName("Chat").withIcon(R.drawable.ic_chat_black_24dp).withIdentifier(CHAT)
                            .withBadge(String.valueOf(NotificationService.getChatInt()))
                            .withBadgeStyle(style)
                    :
                    new PrimaryDrawerItem().withName("Chat").withIcon(R.drawable.ic_chat_black_24dp).withIdentifier(CHAT)
                            .withBadgeStyle(style),

                new PrimaryDrawerItem().withName("My Group").withIcon(R.drawable.ic_group_black_24dp),
                new DividerDrawerItem(),
                new SecondaryDrawerItem().withName("Log out")
        };
    }

    public static void updateChatBadge (int val) {
        ((PrimaryDrawerItem) drawer.getDrawerItem(CHAT)).withBadge(val != 0 ? String.valueOf(val) : "");
    }

    private static AccountHeader header (DappActivity context) {
        return new AccountHeaderBuilder().withActivity(context)
                .withHeaderBackground(R.drawable.drawer_banner)
                .addProfiles(profile())
                .withSelectionListEnabled(false)
                .build();
    }

    private static ProfileDrawerItem profile () {
        String str = App.me().getPhotoUrl().toString();
        return new ProfileDrawerItem().withName(App.me().getDisplayName())
                                      .withIcon(str);
    }

    public static class DrawerListener implements com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener {

        private DappActivity context;

        private DrawerListener (DappActivity context) {
            this.context = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            Log.d(TAG, "Item clicked at position " + position);
            Class<? extends DappActivity> newClass;

            switch (position) {
                case HOME:
                    Log.d(TAG, "Home item selected.");
                    newClass = MyGroupActivity.class;
                    break;
                case MAP:
                    Log.d(TAG, "Maps item selected.");
                    newClass = MapsActivity.class;
                    break;
                case FEED:
                    Log.d(TAG, "Feed item selected.");
                    newClass = MainFeedActivity.class;
                    break;
                case CHAT:
                    Log.d(TAG, "Chat item selected.");
                    newClass = ChatSelectorActivity.class;
                    break;
                case INVITE:
                    sendTextInvite();
                    newClass = null;
                    break;
                case LOG_OUT:
                    Log.d(TAG, "Logout item selected.");
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    newClass = SignInActivity.class;
                    break;
                default:
                    throw new RuntimeException("Illegal option selected.");
            }

            if (newClass != null && ! context.getClass().equals(newClass)) {
                Intent intent = new Intent(context, newClass);
                context.startActivity(intent);
            }

            return false;
        }

        private void sendTextInvite () {
            final Uri imageUri = Uri.parse("https://static.wixstatic.com/media/ce4cf3_f8a887fe7c024d4e83405a558ae61aa9~mv2_d_1250_1250_s_2.png/v1/fill/w_80,h_80,al_c,usm_0.66_1.00_0.01/ce4cf3_f8a887fe7c024d4e83405a558ae61aa9~mv2_d_1250_1250_s_2.png");

            final int LOGIN_PROVIDER = 1;
            if (App.me().getProviderData().get(LOGIN_PROVIDER).getProviderId().equals("facebook.com")) {
                if (AppInviteDialog.canShow()) {
                    final String deepAppLinkFacebook = "https://fb.me/1352827251467411";
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(deepAppLinkFacebook)
                            .setPreviewImageUrl(imageUri.toString())
                            .build();
                    AppInviteDialog.show(context, content);
                }
            }
            else {
                //Firebase Invites Code
                final Uri deepAppLinkFirebase = Uri.parse("https://r46u3.app.goo.gl/xJdE");
                final int REQUEST_INVITE = 0b10010;
                Intent intent = new AppInviteInvitation.IntentBuilder("Invite your friends!")
                        .setMessage("Let's hang out! Download Dapp and start connecting to people near you!")
                        .setDeepLink(deepAppLinkFirebase)
                        .setCustomImage(imageUri)
                        .setCallToActionText("Download now!")
                        .build();
                context.startActivityForResult(intent, REQUEST_INVITE);
            }

        }
    }

}
