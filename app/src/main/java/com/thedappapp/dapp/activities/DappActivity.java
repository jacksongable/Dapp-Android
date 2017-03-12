package com.thedappapp.dapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DrawerResources;

import com.thedappapp.dapp.interfaces.NoDatabase;
import com.thedappapp.dapp.interfaces.NoToolbar;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.thedappapp.dapp.interfaces.ToolbarWithoutDrawer;

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
        if (!(this instanceof NoDatabase)) {

        }
        if (this instanceof NoToolbar)
            return;
        else setToolbar();
        if (!(this instanceof ToolbarWithoutDrawer))
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
            if (this instanceof MainActivity) activityIdentifier = 1;
            else if (this instanceof RequestsActivity) activityIdentifier = 2;
            else if (this instanceof MapsActivity) activityIdentifier = 3;
            else if (this instanceof FeedActivity) activityIdentifier = 4;
            else if (this instanceof ChatSelectorActivity || this instanceof ChatThreadActivity) activityIdentifier = 5;

            if (activityIdentifier != -1) drawer.setSelectionAtPosition(activityIdentifier, false);
        }
    }

    /*
    @Subscribe
    public void onGroupDataUpdate (GroupDataEvent event) {
        Log.i(getClass().getSimpleName(), "GroupDataEvent recieved from bus.");
        App.getApp().setCurrentGroup(event.getNewGroup());
        App.getApp().USER.child(App.getApp().me().getUid()).child("group").setValue(event.getNewGroup().getMeta().getUid());
    }
    */

    @Override
    protected void onStop () {
        super.onStop();
        if (!(this instanceof NoDatabase)) {

        }
    }
}