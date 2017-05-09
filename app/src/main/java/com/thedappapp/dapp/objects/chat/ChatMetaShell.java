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


public class ChatMetaShell {

    private String chat_id, group_name, last_message, to;
    private int unread;

    public ChatMetaShell() {

    }

    public ChatMetaShell(String chat_id, String otherGroupName, String toId) {
        this.chat_id = chat_id;
        this.group_name = otherGroupName;
        this.unread = 0;
        this.to = toId;
    }

    public String getLast_message () {
        return last_message;
    }

    public void setLast_message (String msg) {
        last_message = msg;
    }

    public String getChat_id() {
        return chat_id;
    }

    public String getGroup_name () {
        return group_name;
    }

    public void incrementUnread () {
        unread++;
    }

    public void setUnread (int unread) {
        this.unread = unread;
    }

    public void resetUnreadCount () {
        this.unread = 0;
    }

    public int getUnread () {
        return unread;
    }

    public String getTo () {
        return this.to;
    }

    public void save(@NonNull SaveKeys code, String userUid) {
        if (code == SaveKeys.DO_NOTHING) return;
        else if (code == SaveKeys.DELETE) {
            FirebaseDatabase.getInstance().getReference("users").child(userUid).child("active_chats").child(chat_id).setValue(null);
        }
        else if (code == SaveKeys.CREATE){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("active_chats").child(chat_id);
            ref.setValue(this);
        }
        else if (code == SaveKeys.UPDATE) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("active_chats").child(chat_id);
            ref.setValue(this);
        }
    }
}
