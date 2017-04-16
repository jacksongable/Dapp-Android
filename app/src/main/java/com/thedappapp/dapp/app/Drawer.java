package com.thedappapp.dapp.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.activities.ChatSelectorActivity;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.activities.MyGroupActivity;
import com.thedappapp.dapp.activities.RequestsActivity;
import com.thedappapp.dapp.activities.MapsActivity;
import com.thedappapp.dapp.activities.MainFeedActivity;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.thedappapp.dapp.activities.SignInActivity;

/**
 * Created by jackson on 7/7/16.
 */
public class Drawer {

    public static final int INVITE = 1;
    public static final int FEED = 3;
    public static final int MAP = 4;
    public static final int REQUESTS = 5;
    public static final int CHAT = 6;
    public static final int HOME = 7;
    public static final int LOG_OUT = 9;

    private static final IDrawerItem[] items;
    private static final String TAG;

    static {
        TAG = Drawer.class.getSimpleName();

        items = new IDrawerItem[9];
        items[0] = new PrimaryDrawerItem().withName("Invite your friends!").withIcon(R.drawable.ic_share_black_24dp);
        items[1] = new DividerDrawerItem();
        items[2] = new PrimaryDrawerItem().withName("Feed").withIcon(R.drawable.ic_whatshot_black_24dp);
        items[3] = new PrimaryDrawerItem().withName("Map").withIcon(R.drawable.ic_map_black_24dp);
        items[4] = new PrimaryDrawerItem().withName("Requests").withIcon(R.drawable.ic_notifications_black_24dp);
        items[5] = new PrimaryDrawerItem().withName("Chat").withIcon(R.drawable.ic_chat_black_24dp);
        items[6] = new PrimaryDrawerItem().withName("My Group").withIcon(R.drawable.ic_group_black_24dp);
        items[7] = new DividerDrawerItem();
        items[8] = new SecondaryDrawerItem().withName("Log out");
    }

    public static com.mikepenz.materialdrawer.Drawer set (DappActivity context, Toolbar toolbar) {
        return new DrawerBuilder().withActivity(context)
                .withToolbar(toolbar)
                .addDrawerItems(Drawer.items())
                .withAccountHeader(Drawer.header(context))
                .withOnDrawerItemClickListener(new Drawer.DrawerListener(context))
                .build();
    }


    private static IDrawerItem[] items () {
        return items;
    }

    private static AccountHeader header (DappActivity context) {
        return new AccountHeaderBuilder().withActivity(context)
                .withHeaderBackground(R.drawable.drawer_banner)
                .addProfiles(profile())
                .withSelectionListEnabled(false)
                .build();
    }

    private static ProfileDrawerItem profile () {
        String str = App.getApp().me().getPhotoUrl().toString();
        return new ProfileDrawerItem().withName(App.getApp().me().getDisplayName())
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
                case REQUESTS:
                    Log.d(TAG, "Invitations item selected.");
                    newClass = RequestsActivity.class;
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
            Intent text = new Intent(Intent.ACTION_VIEW);
            text.setData(Uri.parse("sms:"));
            text.setType("vnd.android-dir/mms-sms");
            text.putExtra(Intent.EXTRA_TEXT, "Hey! Let's hang out! Download Dapp ehre and start " +
                    "connecting to people near you!\n\nhttp://thedappapp.com");
            context.startActivity(text);
        }
    }

}
