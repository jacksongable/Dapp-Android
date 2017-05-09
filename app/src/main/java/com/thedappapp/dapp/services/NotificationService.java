package com.thedappapp.dapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.activities.ChatThreadActivity;
import com.thedappapp.dapp.activities.GroupDetailsActivity;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.Drawer;
import com.thedappapp.dapp.objects.Notification;

public class NotificationService extends Service {

    private static final String TAG = NotificationService.class.getSimpleName();

    private static MenuItem bell;
    private NotificationBellListener bellListener;
    private DrawerNotificationListener dListener;
    private static int chatInt;

    public static void setBell (MenuItem bell) {
        NotificationService.bell = bell;
    }

    public NotificationService() {
        bellListener = new NotificationBellListener();
        dListener = new DrawerNotificationListener();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference("notifications").child(App.me().getUid())
                    .addChildEventListener(bellListener);

            FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid())
                    .child("chat_unread").addValueEventListener(dListener);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference("notifications").child(App.me().getUid())
                .removeEventListener(bellListener);
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid())
                .child("chat_unread").removeEventListener(dListener);
    }

    public static int getChatInt () {
        return chatInt;
    }

    private class DrawerNotificationListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                chatInt = 0;
                dataSnapshot.getRef().setValue(0);
            }
            else chatInt = dataSnapshot.getValue(Integer.class);

            Drawer.updateChatBadge(chatInt);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            App.handleDbErr(databaseError);
        }
    }

    public static void setUnreadChatInt (int i) {
        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("chat_unread").setValue(i);
    }

    private class NotificationBellListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Notification notification = dataSnapshot.getValue(Notification.class);
            if (!notification.isRead()) {
                notify(notification);
            }
            if (!notification.isRead() && bell != null) {
                bell.setIcon(R.drawable.ic_notifications_red_24dp);
            }


        }

        private void notify (Notification n) {
            if (n.getType() == Notification.Types.NEW_REQUEST) {
                Intent intent = new Intent(NotificationService.this, GroupDetailsActivity.class);
                intent.putExtra("gid", n.getMeta().get("from_group").toString());

                PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this);
                builder.setContentTitle("New Request");
                builder.setContentText(n.getMessage());
                builder.setAutoCancel(true);
                builder.setContentIntent(pendingIntent);
                builder.setSmallIcon(R.mipmap.ic_notification);

                notificationManager.notify(0, builder.build());
            }
            else if (n.getType() == Notification.Types.REQUEST_ACCEPTED) {
                Intent intent = new Intent(NotificationService.this, ChatThreadActivity.class);
                intent.putExtra("key", n.getMeta().get("conversation_key").toString());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            App.handleDbErr(databaseError);
        }
    }
}
