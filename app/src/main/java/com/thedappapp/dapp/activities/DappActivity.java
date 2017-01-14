package com.thedappapp.dapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.DrawerResources;


import com.thedappapp.dapp.interfaces.NoToolbarActivity;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.thedappapp.dapp.interfaces.ToolbarWithoutDrawerActivity;

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
        if (this instanceof NoToolbarActivity)
            return;
        else setToolbar();
        if (!(this instanceof ToolbarWithoutDrawerActivity))
            enableDrawer();
    }


    protected void setToolbar () {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        mToolbar = tb;
    }

    protected void enableDrawer () {
        DrawerResources.withContext(this);
        if (mToolbar == null)
            throw new IllegalStateException("Cannot enable drawer on a null Toolbar. Make sure you " +
                    "<include> the Toolbar in the current layout and call setToolbar().");
        else {
            Drawer drawer = new DrawerBuilder().withActivity(this)
                    .withToolbar(mToolbar)
                    .addDrawerItems(DrawerResources.items())
                    .withAccountHeader(DrawerResources.header())
                    .withOnDrawerItemClickListener(new DrawerResources.DrawerListener())
                    .build();

            int activityIdentifier = -1;
            if (this instanceof MyGroupActivity) activityIdentifier = 1;
            else if (this instanceof InvitationViewerActivity) activityIdentifier = 2;
            else if (this instanceof MapsActivity) activityIdentifier = 3;
            else if (this instanceof FeedActivity) activityIdentifier = 4;
            else if (this instanceof ChatSelectorActivity || this instanceof ChatThreadActivity) activityIdentifier = 5;

            if (activityIdentifier != -1) drawer.setSelectionAtPosition(activityIdentifier, false);
        }
    }
}