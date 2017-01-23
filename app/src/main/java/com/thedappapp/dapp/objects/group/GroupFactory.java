package com.thedappapp.dapp.objects.group;

import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.activities.DappActivity;
import com.thedappapp.dapp.async.Locator;
import java.util.List;

/**
 * Created by jackson on 9/20/16.
 */
public class GroupFactory {

    private String name, bio, leader;
    private List<String> interests;

    private DappActivity contextForLocation;
    private boolean usingLocation = false;

    public GroupFactory withName (String name) {
        this.name = name;
        return this;
    }

    public GroupFactory withBio (String bio) {
        this.bio = bio;
        return this;
    }

    public GroupFactory withLeader (String leader) {
        this.leader = leader;
        return this;
    }

    public GroupFactory withInterests (List<String> interests) {
        this.interests = interests;
        return this;
    }

    public GroupFactory useLocation (DappActivity context) {
        contextForLocation = context;
        usingLocation = true;
        return this;
    }

    public Group build () {
        return new Group(name, bio, leader, interests);
    }








}
