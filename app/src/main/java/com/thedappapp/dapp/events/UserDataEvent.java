package com.thedappapp.dapp.events;

/**
 * Created by jackson on 1/14/17.
 */

public class UserDataEvent {

    public enum Attributes {
        CURRENT_GROUP,
        VERIFICATION,
        DISPLAY_NAME
    }

    private Attributes attribute;
    private String newValue;

    public UserDataEvent(Attributes attribute, String newValue) {
        this.attribute = attribute;
        this.newValue = newValue;
    }

    public Attributes getChangedAttribute () {
        return attribute;
    }

    public String getAttributeValue () {
        return newValue;
    }
}
