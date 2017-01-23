package com.thedappapp.dapp.events;

import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 1/14/17.
 */

public class GroupDataEvent {

    private Group newGroup;

    public GroupDataEvent(Group newGroup) {
        this.newGroup = newGroup;
    }

    public Group getNewGroup () {
        return this.newGroup;
    }
}
