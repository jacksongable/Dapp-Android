package com.thedappapp.dapp.app;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.activities.ChatSelectorActivity;
import com.thedappapp.dapp.activities.MyGroupActivity;
import com.thedappapp.dapp.activities.RequestsActivity;
import com.thedappapp.dapp.activities.MapsActivity;
import com.thedappapp.dapp.activities.FeedActivity;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

/**
 * Created by jackson on 7/7/16.
 */
public class DrawerResources {

    private static final int HOME = 1;
    private static final int REQUESTS = 2;
    private static final int MAP = 3;
    private static final int FEED = 4;
    private static final int CHAT = 5;
    private static final int LOG_OUT = 7;

    private static final IDrawerItem[] items;
    private static final String TAG;

    private static DappActivity context;

    static {
        TAG = DrawerResources.class.getSimpleName();

        items = new IDrawerItem[7];
        items[0] = new PrimaryDrawerItem().withName("My Group");
        items[1] = new PrimaryDrawerItem().withName("Requests");
        items[2] = new PrimaryDrawerItem().withName("Map");
        items[3] = new PrimaryDrawerItem().withName("Feed");
        items[4] = new PrimaryDrawerItem().withName("Chat");
        items[5] = new DividerDrawerItem();
        items[6] = new PrimaryDrawerItem().withName("Log out");
    }


    public static void withContext(DappActivity activity) {
        context = activity;
    }

    public static IDrawerItem[] items () {
        return items;
    }

    public static AccountHeader header () {
        return new AccountHeaderBuilder().withActivity(context)
                .withHeaderBackground(R.drawable.drawer_banner)
                .addProfiles(profile())
                .withSelectionListEnabled(false)
                .build();
    }

    private static ProfileDrawerItem profile () {
        return new ProfileDrawerItem().withName(User.me().getUsername());
        //.withIcon(App.fetchProfileImageIfNecessary()); Deactivated.
    }

    public static IDrawerItem getCurrentlyLoadedActivityItem () {
        if (context instanceof MyGroupActivity) return items[0];
        if (context instanceof MapsActivity) return items[1];
        if (context instanceof FeedActivity) return items[2];
        if (context instanceof ChatSelectorActivity) return items[3];
        return null;
    }

    public static class DrawerListener implements Drawer.OnDrawerItemClickListener {

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
                    newClass = FeedActivity.class;
                    break;
                case CHAT:
                    Log.d(TAG, "Chat item selected.");
                    newClass = ChatSelectorActivity.class;
                    break;
                case LOG_OUT:
                    Log.d(TAG, "Chat item selected.");
                    App.logout(sContext);
                    return false;
                default:
                    throw new RuntimeException("Illegal option selected.");
            }

            if (! context.getClass().equals(newClass)) {
                Intent intent = new Intent(context, newClass);
                context.startActivity(intent);
            }

            return false;
        }
    }

}
