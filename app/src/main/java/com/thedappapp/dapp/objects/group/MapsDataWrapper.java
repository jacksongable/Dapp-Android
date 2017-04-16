package com.thedappapp.dapp.objects.group;

import com.google.firebase.database.Exclude;

import java.util.Map;

/**
 * Created by jackson on 4/3/17.
 */

public class MapsDataWrapper {

    private String key, ownerId, groupName, groupBio;
    private Map<String, Boolean> interests;
    private long latitude, longitude;

    public MapsDataWrapper () {}

    public MapsDataWrapper (String key, String ownerId, String groupName, String groupBio,
                            Map<String, Boolean> interests, long latitude, long longitude) {
        this.key = key;
        this.ownerId = ownerId;
        this.groupName = groupName;
        this.groupBio = groupBio;
        this.interests = interests;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getKey () {
        return key;
    }

    public String getOwnerId () {
        return ownerId;
    }

    public String getGroupName () {
        return this.groupName;
    }

    public String getGroupBio () {
        return this.groupBio;
    }

    public long getLatitude () {
        return latitude;
    }

    public long getLongitude () {
        return longitude;
    }

    public Map<String, Boolean> getInterests () {
        return this.interests;
    }

    @Exclude
    public boolean hasInterest (String interest) {
        return interests.containsKey(interest);
    }
}
