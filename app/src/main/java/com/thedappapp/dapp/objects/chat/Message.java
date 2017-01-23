package com.thedappapp.dapp.objects.chat;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.objects.AbstractFirebaseObject;
import com.thedappapp.dapp.objects.Metadata;
import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 8/19/16.
 */
public class Message extends AbstractFirebaseObject {

    private String message, sender;

    @Exclude
    private transient String convoId;

    public Message () {}

    public Message (String message, String sender) {
        this.message = message;
        this.sender = sender;
        convoId = null;
    }

    private Message (Parcel in) {
        message = in.readString();
        sender = in.readString();
    }

    public String getMessage () {
        return message;
    }

    public String getSender () {
        return sender;
    }

    public Message intoConversation (String id) {
        convoId = id;
        return this;
    }

    @Override
    public void saveToFirebase(@NonNull DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DO_NOTHING)
            return;
        else if (code == DatabaseOperationCodes.UPDATE)
            throw new IllegalArgumentException("Cannot update a sent chat message.");

        else if (convoId == null)
            throw new IllegalStateException("Call intoConversation(String) first so we know what conversation to save the message to.");

        else if (code == DatabaseOperationCodes.DELETE) {
            FirebaseDatabase.getInstance().getReference("chat").child(convoId).child("messages").child(super.meta.getUid()).setValue(null);
            return;
        }
        else {
            DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference("chat").child(convoId).child("messages");
            super.meta = new Metadata(msgRef.getKey(), ServerValue.TIMESTAMP, null);
            msgRef.push().setValue(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(sender);
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
