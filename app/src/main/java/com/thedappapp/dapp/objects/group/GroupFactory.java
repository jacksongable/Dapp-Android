package com.thedappapp.dapp.objects.group;

import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.activities.DappActivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jackson on 9/20/16.
 */
public class GroupFactory {

    private String name, bio, leaderId, leaderName, pic;
    private Map<String, Boolean> interests;

    public GroupFactory withName (String name) {
        this.name = name;
        return this;
    }

    public GroupFactory withBio (String bio) {
        this.bio = bio;
        return this;
    }

    public GroupFactory withLeaderId (String id) {
        this.leaderId = id;
        return this;
    }

    public GroupFactory withLeaderName (String name) {
        this.leaderName = name;
        return this;
    }

    public GroupFactory withInterests (List<String> list) {
        interests = new HashMap<>();

        for (String key : list)
            interests.put(key.toLowerCase(), true);
        return this;
    }

    public GroupFactory withPic (String path) {
        this.pic = path;
        return this;
    }

    public Group build () {
        return new Group(name, bio, leaderId, leaderName, interests, pic);
    }








}
