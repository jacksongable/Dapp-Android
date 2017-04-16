package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.adapters.ChatThreadAdapter;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.chat.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatThreadActivity extends DappActivity {

    private static final String TAG = ChatThreadActivity.class.getSimpleName();

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatThreadAdapter adapter;
    private TextView nocontent;

    private String conversationKey;
    private Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_thread);

        conversationKey = getIntent().getExtras().getString("key");
        listener = new Listener();
        setTitle("[PLACEHOLDER]");

        nocontent = (TextView) findViewById(R.id.no_content_message);
        messagesContainer = (ListView) findViewById(R.id.message_list);
        messageET = (EditText) findViewById(R.id.edit_message);
        sendBtn = (Button) findViewById(R.id.send_message);

    }

    @Override
    protected void onStart() {
        super.onStart();

        loadHistory();
        messagesContainer.setDivider(null);

        FirebaseDatabase.getInstance().getReference("chats").child(conversationKey).child("messages").addChildEventListener(listener);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message(messageET.getText().toString(),
                                              App.getApp().me().getUid(),
                                              App.getApp().me().getDisplayName());

                messageET.setText("");
                sendMessage(message);
            }
        });
    }

    private void loadHistory(){
        nocontent.setText("Start chatting!");
        nocontent.setVisibility(View.VISIBLE);
        List<Message> messages = new ArrayList<>();

        adapter = new ChatThreadAdapter(this, messages);
        messagesContainer.setAdapter(adapter);
        scroll();
    }

    private void displayMessage(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void sendMessage (Message message) {
        if (nocontent.getVisibility() == View.VISIBLE)
            nocontent.setVisibility(View.GONE);
        message.intoConversation(conversationKey).save(SaveKeys.CREATE);
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference("chats").child(conversationKey).child("messages").removeEventListener(listener);
    }

    private class Listener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (nocontent.getVisibility() == View.VISIBLE)
                nocontent.setVisibility(View.GONE);

            Message msg = dataSnapshot.getValue(Message.class);
            displayMessage(msg);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Child changed at messaging node.");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.w(TAG, "Child removed at messaging node.");

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.w(TAG, "Child moved at messaging node.");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, Log.getStackTraceString(databaseError.toException()));
        }
    }

}