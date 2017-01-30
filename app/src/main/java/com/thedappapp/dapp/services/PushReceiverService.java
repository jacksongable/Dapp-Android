package com.thedappapp.dapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thedappapp.dapp.activities.ChatThreadActivity;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.R;

import java.util.Map;

/**
 * Created by jackson on 1/23/17.
 */

public class PushReceiverService extends FirebaseMessagingService {

    private static final String TAG = PushReceiverService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();

        if (data.get("type").equals("message"))
            doMessage(data.get("conversationId"));
        else if (data.get("type").equals("invite"))
            doInvite(data.get("from"));


    }

    private void doMessage (String roomId) {
        App.getApp().CONVO_ROOT.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(PushReceiverService.this, ChatThreadActivity.class);
                Conversation conversation = dataSnapshot.getValue(Conversation.class);

                intent.putExtra("conversation", conversation);
                PendingIntent pendingIntent = PendingIntent.getActivity(PushReceiverService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(PushReceiverService.this);
                builder.setContentTitle("USERNAME HERE");
                builder.setContentText("MESSAGE HERE");
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setSmallIcon(R.mipmap.ic_notification);

                notificationManager.notify(0, builder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void doInvite (String groupId) {
        App.getApp().GROUP_ROOT.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(PushReceiverService.this, GroupDetailsActivity.class);
                Group group = dataSnapshot.getValue(Group.class);

                intent.putExtra("group", group);
                PendingIntent pendingIntent = PendingIntent.getActivity(PushReceiverService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(PushReceiverService.this);
                builder.setContentTitle("New Request");
                builder.setContentText("blank dapped you up!");
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setSmallIcon(R.mipmap.ic_notification);

                notificationManager.notify(0, builder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
