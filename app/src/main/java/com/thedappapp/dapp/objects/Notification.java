package com.thedappapp.dapp.objects;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 4/20/17.
 */

public class Notification extends DappObject {

    public enum Types {
        NEW_REQUEST, REQUEST_ACCEPTED
    }

    private static final String TAG = Notification.class.getSimpleName();

    private String message;
    private Types type;

    @Exclude
    private transient String to, fromGroup;

    private boolean read;

    @Exclude
    public static String parseMessage(String leader) {
        return "Chat invite from " + leader + "!";
    }

    public Notification () {
        read = false;
    }

    public Notification(String message, String to, Types type) {
        this.message = message;
        this.to = to;
        read = false;
    }

    public Types getType () {
        return type;
    }

    public void setType (Types type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.UPDATE) throw new IllegalArgumentException("Cannot update a notification after its creation.");
        else if (code == SaveKeys.DELETE) throw new IllegalArgumentException("Cannot delete a notification after its creation.");
        else if (code == SaveKeys.CREATE) {

            switch (this.type) {
                case NEW_REQUEST:
                    saveInternalNewRequest();
                    break;
                case REQUEST_ACCEPTED:
                    saveInternalRequestAccepted();
                    break;
            }



        }
    }

    public void setFromGroup (String fromGroup) {
        if (type != Types.NEW_REQUEST) throw new IllegalStateException("call setType(Types.NEW_REQUEST) first.");
        this.fromGroup = fromGroup;
    }

    private void saveInternalNewRequest () {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(this.to).push();
        super.meta = new Metadata(ref.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
        super.meta.addMiscellaneousData("requested_group", fromGroup);
        super.meta.addMiscellaneousData("user_requesting", App.getApp().me().getUid());
        super.meta.addMiscellaneousData("from_group", App.getApp().getCurrentGroupUidOffline());
        ref.setValue(this);
    }


    private void saveInternalRequestAccepted () {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(to).push();
        super.meta = new Metadata(ref.getKey(), ServerValue.TIMESTAMP, ServerValue.TIMESTAMP);
        ref.setValue(this);
    }

}
