package com.thedappapp.dapp.objects.chat;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.DappObject;
import com.thedappapp.dapp.objects.Metadata;

/**
 * Created by jackson on 8/19/16.
 */
public class Message extends DappObject {

    private String text, senderId, senderName;

    @Exclude
    private transient String convoId;

    public Message () {}

    public Message (String text, String senderId, String senderName) {
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        convoId = null;
    }

    private Message (Parcel in) {
        text = in.readString();
        senderId = in.readString();
        senderName = in.readString();
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName () {
        return senderName;
    }

    public Message intoConversation (String id) {
        convoId = id;
        return this;
    }

    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.UPDATE)
            throw new IllegalArgumentException("Cannot update a sent chat message.");

        else if (convoId == null)
            throw new IllegalStateException("Call intoConversation(String) first so we know what conversation to save the message to.");

        else if (code == SaveKeys.DELETE) {
            FirebaseDatabase.getInstance().getReference("chat").child(convoId).child("messages").child(super.meta.getUid()).setValue(null);
        }
        else {
            DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference("chats").child(convoId).child("messages").push();
            super.meta = new Metadata(msgRef.getKey(), ServerValue.TIMESTAMP, null);
            msgRef.setValue(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(senderId);
        parcel.writeString(senderName);
    }

    public static Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel parcel) {
            return new Message(parcel);
        }

        @Override
        public Message[] newArray(int i) {
            return new Message[0];
        }
    };
}
