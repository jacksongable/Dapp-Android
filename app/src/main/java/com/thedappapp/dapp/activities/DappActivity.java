package com.thedappapp.dapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.Drawer;

import com.thedappapp.dapp.interfaces.NoMenu;
import com.thedappapp.dapp.interfaces.NoToolbar;
import com.mikepenz.materialdrawer.DrawerBuilder;

/**
 * The abstract root activity that all activities in this app should inherit from. This class provides
 * functions that all activities in this app implement.
 *
 * @author Jackson Gable
 * @version 0.2
 */
public abstract class DappActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onStart() {
        super.onStart();
        if (! (this instanceof NoToolbar) && this instanceof NoMenu)
            setToolbar();
        else if (!(this instanceof NoMenu) && !(this instanceof NoToolbar)) {
            setToolbar();
            enableDrawer();
        }
    }

    protected void setToolbar () {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        mToolbar = tb;
    }

    protected void enableDrawer () {
        if (mToolbar == null)
            throw new IllegalStateException("Cannot enable drawer on a null Toolbar. Make sure you " +
                    "<include> the Toolbar in the current layout and call setToolbar().");
        else {
            com.mikepenz.materialdrawer.Drawer drawer = com.thedappapp.dapp.app.Drawer.set(this, mToolbar);

            int activityIdentifier = -1;
            if (this instanceof MyGroupActivity) activityIdentifier = Drawer.HOME;
            else if (this instanceof RequestsActivity) activityIdentifier = Drawer.REQUESTS;
            else if (this instanceof MapsActivity) activityIdentifier = Drawer.MAP;
            else if (this instanceof MainFeedActivity) activityIdentifier = Drawer.FEED;
            else if (this instanceof ChatSelectorActivity || this instanceof ChatThreadActivity) activityIdentifier = Drawer.CHAT;

            if (activityIdentifier != -1) drawer.setSelectionAtPosition(activityIdentifier, false);
        }
    }
}