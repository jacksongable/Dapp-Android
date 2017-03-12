package com.thedappapp.dapp.activities;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Camera;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.fragments.CreateGroupPage1Fragment;
import com.thedappapp.dapp.fragments.CreateGroupPage2Fragment;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.group.GroupFactory;

import java.util.List;

public class CreateGroupActivity extends DappActivity
        implements CreateGroupPage1Fragment.Page1FragmentInteractionListener, CreateGroupPage2Fragment.Page2FragmentInteractionListener {

    private static final String TAG = CreateGroupActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    private int fragment;
    private Camera camera;
    private CreateGroupPage1Fragment page1;
    private CreateGroupPage2Fragment page2;
    private Bundle page1Bundle, page2Bundle;
    private boolean editMode;

    public static final String ACTION_EDIT = "com.thedappapp.dapp.activities.actions.EDIT_GROUP";
    public static final String ACTION_CREATE = "com.thedappapp.dapp.activities.actions.CREATE_GROUP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        String action = getIntent().getAction();

        if (ACTION_CREATE.equals(action))
            editMode = false;
        else if (ACTION_EDIT.equals(action))
            editMode = true;

        fragmentManager = getSupportFragmentManager();
        fragment = R.id.fragment;
        camera = new Camera(this);

        if (editMode) {
            page1 = CreateGroupPage1Fragment.newInstance((Group) getIntent().getExtras().getParcelable("edit"));
            page2 = CreateGroupPage2Fragment.newInstance((Group) (getIntent().getExtras().getParcelable("edit")));
        } else {
            page1 = CreateGroupPage1Fragment.newInstance(null);
            page2 = CreateGroupPage2Fragment.newInstance(null);
        }

        startPage1();
    }

    protected void startPage1() {
        fragmentManager.beginTransaction()
                .replace(fragment, page1)
                .commitAllowingStateLoss();
    }

    @Override
    public void onPage1Interaction(CreateGroupPage1Fragment.RequestCode code) {
        switch (code) {
            case DISPATCH_CAMERA:
                page1.onPictureTaken();
                camera.dispatch();
                break;
            case DONE:
                finalizePage1();
                startPage2();
                break;
            default:
                throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    protected void finalizePage1() {
        page1Bundle = page1.pullInfo();
    }

    protected void startPage2() {
        fragmentManager.beginTransaction()
                .replace(fragment, page2)
                .commitAllowingStateLoss();
    }

    @Override
    public void onPage2Interaction(CreateGroupPage2Fragment.RequestCode code) {
        switch (code) {
            case DONE:
                finalizePage2();
                buildGroup();
                break;
            default:
                throw new IllegalArgumentException("Illegal request code received.");
        }
    }

    protected void finalizePage2() {
        page2Bundle = page2.pullInfo();
    }

    protected void buildGroup() {
        String name = page1Bundle.getString("name");
        String bio = page1Bundle.getString("bio");

        List<String> interests = page2Bundle.getStringArrayList("interests");

        GroupFactory factory = new GroupFactory();

        Group group = factory.withName(name)
                .withBio(bio)
                .withLeaderId(App.getApp().me().getUid())
                .withLeaderName(App.getApp().me().getDisplayName())
                .withInterests(interests)
                .withPic(camera.getCapturedImagePath())
                .build();

        if (editMode) group.fetchLocationAndSave(this, SaveKeys.UPDATE);
        else group.fetchLocationAndSave(this, SaveKeys.CREATE);
        finish();
    }
}