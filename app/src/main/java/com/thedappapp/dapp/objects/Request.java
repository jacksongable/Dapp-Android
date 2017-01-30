package com.thedappapp.dapp.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.objects.chat.Conversation;


/**
 * Created by jackson on 11/25/16.
 */
public class Request extends DappObject {

    private String from, to, status;

    public Request() {}

    public Request(String from, String to) {
        this(from, to, "pending");
    }

    public Request(String from, String to, String status) {
        this.from = from;
        this.to = to;
        this.status = status;
    }

    private Request(Parcel in) {
        from = in.readString();
        to = in.readString();
        status = in.readString();
    }

    public String getFrom () {
        return from;
    }

    public String getTo () {
        return to;
    }

    public String getStatus () {
        return status;
    }

    @Override
    protected void saveInternal(@NonNull DatabaseOperationCodes code) {
        if (code == DatabaseOperationCodes.DO_NOTHING)
            return;
        if (code == DatabaseOperationCodes.UPDATE)
            throw new IllegalArgumentException("Cannot update a sent chat message.");
        else if (code == DatabaseOperationCodes.DELETE) {
            FirebaseDatabase.getInstance().getReference("requests").child(super.meta.getUid()).setValue(null);
        }
        else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("requests").push();
            super.meta = new Metadata(ref.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
            ref.setValue(this);
        }
    }

    public Conversation accept () {
        status = "Accepted";
        super.save(DatabaseOperationCodes.UPDATE);
        App.getApp().getRequestStorage().onIncomingRequestAccepted(this);
        Conversation conversation = new Conversation(from);
        conversation.save(DatabaseOperationCodes.CREATE);
        return conversation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(from);
        parcel.writeString(to);
        parcel.writeString(status);
    }

    public static Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel parcel) {
            return new Request(parcel);
        }

        @Override
        public Request[] newArray(int i) {
            return new Request[0];
        }
    };
}
