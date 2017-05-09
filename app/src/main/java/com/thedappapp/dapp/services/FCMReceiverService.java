package com.thedappapp.dapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
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

public class FcmReceiverService extends FirebaseMessagingService {

    private static final String TAG = FcmReceiverService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Message received: " + remoteMessage.getMessageId());
        if (data.get("type").equals("chat"))
            doMessage(data);
        else if (data.get("type").equals("NEW_REQUEST") || data.get("type").equals("REQUEST_ACCEPTED"))
            doInvite(data);


    }


    private void doMessage (final Map<String, String> message) {
        Intent intent = new Intent(FcmReceiverService.this, ChatThreadActivity.class);
        intent.putExtra("key", message.get("conversationKey"));

        PendingIntent pendingIntent = PendingIntent.getActivity(FcmReceiverService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(FcmReceiverService.this);
        builder.setContentTitle(message.get("sender_name"));
        builder.setContentText(message.get("text"));
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_notification);

        notificationManager.notify(0, builder.build());

    }

    private void doInvite (final Map<String, String> message) {
        Intent intent = new Intent(FcmReceiverService.this, GroupDetailsActivity.class);
        intent.putExtra("gid", message.get("from_group"));

        PendingIntent pendingIntent = PendingIntent.getActivity(FcmReceiverService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(FcmReceiverService.this);
        builder.setContentTitle("New Request");
        builder.setContentText(message.get("msg"));
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_notification);

        notificationManager.notify(0, builder.build());
    }
}
