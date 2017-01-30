package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.fragments.CurrentGroupFragment;
import com.thedappapp.dapp.fragments.NoCurrentGroupFragment;
import com.thedappapp.dapp.objects.group.Group;

/**
 * The main activity of the application. This is the user's "home page" if you will.
 *
 * @author Jackson Gable
 * @version Alpha
 */
public class MainActivity extends DappActivity
        implements NoCurrentGroupFragment.Callback, CurrentGroupFragment.Callback {

    private FrameLayout mFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrame = (FrameLayout) findViewById(R.id.fragment);
    }

    @Override
    protected void onStart() {
        if (App.getApp().hasCurrentGroup())
            onHasCurrentGroup();
        else onNoCurrentGroup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (App.getApp().hasCurrentGroup())
            onHasCurrentGroup();
        else onNoCurrentGroup();
    }

    private void resetFrameLayout() {
        mFrame.removeAllViewsInLayout();
    }

    @Override
    public void onCreateGroupRequestReceived() {
        Intent intent = new Intent (this, CreateGroupActivity.class);
        intent.setAction(CreateGroupActivity.ACTION_CREATE);
        startActivity(intent);
        resetFrameLayout();
    }

    public void onHasCurrentGroup() {
        Group current = App.getApp().getCurrentGroup();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, CurrentGroupFragment.newInstance(current)).commitAllowingStateLoss();
    }

    public void onNoCurrentGroup() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, NoCurrentGroupFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    public void onEditRequest(Group group) {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        intent.setAction(CreateGroupActivity.ACTION_EDIT);
        intent.putExtra("edit", group);
        startActivity(intent);
    }

    @Override
    public void onDeleteRequest(Group group) {
        group.save(DatabaseOperationCodes.DELETE);
        App.getApp().setCurrentGroup(null);
        onNoCurrentGroup();
    }
}
