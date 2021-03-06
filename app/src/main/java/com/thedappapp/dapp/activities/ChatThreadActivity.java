package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedappapp.dapp.adapters.ChatThreadAdapter;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.chat.ChatMetaShell;
import com.thedappapp.dapp.objects.chat.Message;
import java.util.ArrayList;

public class ChatThreadActivity extends DappActivity {

    private static final String TAG = ChatThreadActivity.class.getSimpleName();

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatThreadAdapter adapter;
    private TextView nocontent;

    private String conversationKey;
    private Listener listener;
    private String toId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_thread);

        conversationKey = getIntent().getExtras().getString("key");
        toId = getIntent().getExtras().getString("to");
        listener = new Listener();
        setTitle(getIntent().getExtras().getString("name"));

        nocontent = (TextView) findViewById(R.id.no_content_message);
        nocontent.setText("");

        messagesContainer = (ListView) findViewById(R.id.message_list);
        messageET = (EditText) findViewById(R.id.edit_message);
        sendBtn = (Button) findViewById(R.id.send_message);

        adapter = new ChatThreadAdapter(this, new ArrayList<Message>());
        messagesContainer.setAdapter(adapter);
        scroll();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //loadHistory();
        messagesContainer.setDivider(null);

        FirebaseDatabase.getInstance().getReference("chats").child(conversationKey).addChildEventListener(listener);

        messageET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scroll();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageET.getText().toString().isEmpty()) {
                    Toast.makeText(ChatThreadActivity.this, "Oops! You can't send an empty message!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Message message = new Message(messageET.getText().toString(),
                        App.me().getUid(),
                        App.me().getDisplayName(),
                        toId);

                messageET.setText("");
                sendMessage(message);
                scroll();
            }
        });
    }

    private void displayMessage(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void sendMessage (final Message message) {
        if (nocontent.getVisibility() == View.VISIBLE)
            nocontent.setVisibility(View.GONE);
        message.intoConversation(conversationKey).save(SaveKeys.CREATE);

        FirebaseDatabase.getInstance().getReference("users").child(message.getTo()).child("chat_unread").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int unread = dataSnapshot.getValue(Integer.class);
                unread++;
                dataSnapshot.getRef().setValue(unread);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                App.handleDbErr(databaseError);
            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(message.getTo()).child("active_chats").child(conversationKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatMetaShell shell = dataSnapshot.getValue(ChatMetaShell.class);
                shell.setLast_message(message.getText());
                shell.incrementUnread();
                shell.save(SaveKeys.UPDATE, message.getTo());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                App.handleDbErr(databaseError);
            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(App.me().getUid()).child("active_chats").child(conversationKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatMetaShell shell = dataSnapshot.getValue(ChatMetaShell.class);
                shell.setLast_message(message.getText());
                shell.save(SaveKeys.UPDATE, message.getTo());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                App.handleDbErr(databaseError);
            }
        });

    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseDatabase.getInstance().getReference("chats").child(conversationKey).removeEventListener(listener);
    }

    private class Listener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (nocontent.getVisibility() == View.VISIBLE)
                nocontent.setVisibility(View.GONE);

            displayMessage(dataSnapshot.getValue(Message.class));
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
            App.handleDbErr(databaseError);
        }
    }

}