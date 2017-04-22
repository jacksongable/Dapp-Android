package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.ChatSelectorAdapter;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.chat.ActiveChatShell;
import com.thedappapp.dapp.objects.chat.Conversation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChatSelectorActivity extends DappActivity {

    private static final String TAG =  ChatSelectorActivity.class.getSimpleName();

    private ListView vListSelector;
    private ChatSelectorAdapter mAdapter;
    private Listener listener;
    private TextView nocontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selector);
        listener = new Listener();
    }

    @Override
    protected void onStart() {
        super.onStart();

        vListSelector = (ListView) findViewById(R.id.chat_selector_list);

        mAdapter = new ChatSelectorAdapter(this, new ArrayList<ActiveChatShell>());
        vListSelector.setAdapter(mAdapter);
        vListSelector.setFooterDividersEnabled(true);
        vListSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ChatSelectorActivity.this, ChatThreadActivity.class);
                intent.putExtra("key", ((ActiveChatShell) mAdapter.getItem(i)).getChat_id());
                intent.putExtra("name", ((ActiveChatShell) mAdapter.getItem(i)).getGroup_name());
                startActivity(intent);
            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(App.getApp().me().getUid()).child("active_chats").addChildEventListener(listener);

        if (mAdapter.isEmpty()) {
            nocontent = (TextView) findViewById(R.id.no_content_message);
            nocontent.setText("Start dapping up other groups to chat!");
            nocontent.setVisibility(View.VISIBLE);
        }
    }

    private class Listener implements ChildEventListener {

        private List<ActiveChatShell> shells = new ArrayList<>();

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (nocontent.getVisibility() == View.VISIBLE)
                nocontent.setVisibility(View.GONE);

            mAdapter.add(dataSnapshot.getValue(ActiveChatShell.class));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "Child changed.");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "Child removed.");

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "Child moved.");

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Cancelled.");

        }
    }
}
