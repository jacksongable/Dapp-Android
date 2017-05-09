package com.thedappapp.dapp.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.chat.Conversation;


/**
 * Created by jackson on 11/25/16.
 */
public class Request extends DappObject implements Parcelable {

    private String from_id;

    private String from_name;
    private String to_id;
    private String to_name;
    private String status;

    public Request() {}

    public Request(String fromId, String toId, String fromName, String toName) {
        this(fromId, toId, fromName, toName, "pending");
    }

    public Request(String fromId, String toId, String fromName, String toName, String status) {
        this.from_id = fromId;
        this.to_id = toId;
        this.from_name = fromName;
        this.to_name = toName;
        this.status = status;
    }

    private Request(Parcel in) {
        from_id = in.readString();
        from_name = in.readString();
        to_id = in.readString();
        to_name = in.readString();
        status = in.readString();
    }

    public String getFrom_id() {
        return from_id;
    }

    public String getTo_id() {
        return to_id;
    }

    public String getTo_name() {
        return to_name;
    }

    public String getFrom_name() {
        return from_name;
    }

    public String getStatus () {
        return status;
    }

    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.DELETE) {
            //FirebaseDatabase.getInstance().getReference("requests").child(super.meta.getUid()).setValue(null);
        }
        else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(to_id).push();
            String push = ref.getKey();
            //super.meta = new Metadata(push, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
            ref.setValue(this);

            ref = FirebaseDatabase.getInstance().getReference("users").child(from_id).child("pending_requests/outgoing").child(push);
            ref.setValue(true);

            ref = FirebaseDatabase.getInstance().getReference("users").child(to_id).child("pending_requests/incoming").child(push);
            ref.setValue(true);
        }
    }

    public Conversation accept () {
        status = "accepted";
        super.save(SaveKeys.UPDATE);
        //App.getApp().getRequestStorage().onIncomingRequestAccepted(this);
        Conversation conversation = new Conversation(from_id);
        conversation.save(SaveKeys.CREATE);
        return conversation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(from_id);
        parcel.writeString(from_name);
        parcel.writeString(to_id);
        parcel.writeString(to_name);
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
