package com.thedappapp.dapp.objects;

import android.support.annotation.NonNull;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;

/**
 * Created by jackson on 4/20/17.
 */

public class Notification extends DappObject {

    public enum Types {
        NEW_REQUEST, REQUEST_ACCEPTED
    }

    private static final String TAG = Notification.class.getSimpleName();

    private String message, photo;
    private Types type;

    @Exclude
    private transient String to, fromGroup;

    private boolean read;

    public Notification () {
        read = false;
    }

    public Notification(String message, String to, Types type, String photo) {
        this.message = message;
        this.to = to;
        this.photo = photo;
        this.type = type;
        this.read = false;
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

    public String getPhoto () {
        return photo;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead (boolean read) {
        this.read = read;
    }

    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.UPDATE) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(App.me().getUid()).child(getUid());
            ref.setValue(this);
        }
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
        super.putMetadata("uid", ref.getKey());
        super.putMetadata("created", ServerValue.TIMESTAMP);
        super.putMetadata("updated", ServerValue.TIMESTAMP);
        super.putMetadata("requested_group", fromGroup);
        super.putMetadata("user_requesting", App.me().getUid());
        super.putMetadata("from_group", App.getApp().getCurrentGroupUidOffline());
        ref.setValue(this);
    }


    private void saveInternalRequestAccepted () {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(to).push();
        super.putMetadata("uid", ref.getKey());
        super.putMetadata("created", ServerValue.TIMESTAMP);
        super.putMetadata("updated", ServerValue.TIMESTAMP);
        super.putMetadata("requested_group", App.getApp().getCurrentGroupUidOffline());
        super.putMetadata("from_group", fromGroup);
        ref.setValue(this);
    }

}
