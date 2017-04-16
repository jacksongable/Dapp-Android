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

    private String text, sender_id, sender_name;

    @Exclude
    private transient String convoId;

    public Message () {}

    public Message (String text, String senderId, String senderName) {
        this.text = text;
        this.sender_id = senderId;
        this.sender_name = senderName;
        convoId = null;
    }

    private Message (Parcel in) {
        text = in.readString();
        sender_id = in.readString();
        sender_name = in.readString();
    }

    public String getText() {
        return text;
    }

    public String getSender_id() {
        return sender_id;
    }

    public String getSender_name() {
        return sender_name;
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
        parcel.writeString(sender_id);
        parcel.writeString(sender_name);
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
