package com.thedappapp.dapp.objects.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.DappObject;
import com.thedappapp.dapp.objects.Metadata;

import java.util.List;
import java.util.Map;

/**
 * Created by jackson on 8/20/16.
 */
public class Conversation extends DappObject {

    private List<Message> messages;
    private String user1Id, user2Id;

    public Conversation() {}

    public Conversation(String otherUserId) {
        user1Id = App.getApp().me().getUid();
        user2Id = otherUserId;
    }

    private Conversation(Parcel in) {
        super.meta = in.readParcelable(Metadata.class.getClassLoader());
        in.readList(messages, Message.class.getClassLoader());
    }

    @Exclude
    @Override
    protected void saveInternal(@NonNull SaveKeys code) {
        if (code == SaveKeys.DELETE) {
            FirebaseDatabase.getInstance().getReference("groups").child(meta.getUid()).setValue(null);
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

    @Exclude
    public String getOtherUser () {
        String title = null;
        Map<String, Object> map = super.meta.getMisc_data();
        String[] people = (String[]) map.values().toArray();

        for (String person : people)
            if (!(person.equals(App.getApp().me().getDisplayName())))
                title = person;
        return title;
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
