package com.thedappapp.dapp.objects.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.Application;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.objects.AbstractFirebaseObject;
import com.thedappapp.dapp.objects.Metadata;
import com.thedappapp.dapp.objects.group.Group;

import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class Conversation extends AbstractFirebaseObject {

    private List<Message> messages;

    public Conversation() {}

    private Conversation(Parcel in) {
        super.meta = in.readParcelable(Metadata.class.getClassLoader());
        in.readList(messages, Message.class.getClassLoader());
    }

    @Exclude
    @Override
    public void saveToFirebase (@NonNull DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DO_NOTHING)
            return;

        if (meta == null && code != DatabaseOperationCodes.CREATE)
            throw new IllegalStateException("You must create the group object in the database before deleting or updating it.");

        else if (code == DatabaseOperationCodes.DELETE) {
            FirebaseDatabase.getInstance().getReference("groups").child(meta.getUid()).setValue(null);
            return;
        } else {
            DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("groups");
            switch (code) {
                case CREATE:
                    super.meta = new Metadata(groupReference.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
                    groupReference.push().setValue(this);
                    break;
                case UPDATE:
                    super.meta.setUpdated(ServerValue.TIMESTAMP);
                    groupReference.child(super.meta.getUid()).setValue(this);
                    break;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeParcelable(super.meta, 0);
        out.writeTypedList(messages);
    }

    public static final Parcelable.Creator<Conversation> CREATOR = new Parcelable.Creator<Conversation>() {
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
