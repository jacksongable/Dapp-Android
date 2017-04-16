package com.thedappapp.dapp.objects.chat;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Compressor;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.DappObject;
import com.thedappapp.dapp.objects.Metadata;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.group.OldGroup;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by jackson on 3/26/17.
 */


public class ActiveChatShell {

    private String chat_id, group_name, key;

    public ActiveChatShell () {

    }

    public ActiveChatShell (String chat_id, String otherGroupName) {
        this.chat_id = chat_id;
        this.group_name = otherGroupName;
    }

    public String getKey() {
        return key;
    }

    public String getChat_id() {
        return chat_id;
    }

    public String getGroup_name () {
        return group_name;
    }

    public void save(@NonNull SaveKeys code) {
        if (code == SaveKeys.DO_NOTHING) return;
        else if (code == SaveKeys.DELETE) {
            FirebaseDatabase.getInstance().getReference("users").child(App.getApp().me().getUid()).child("active_chats").child(key).setValue(null);
        }
        else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(App.getApp().me().getUid()).child("active_chats").push();
            key = ref.getKey();
            ref.setValue(this);
        }
    }
}
