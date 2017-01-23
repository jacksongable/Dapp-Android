package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.thedappapp.dapp.adapters.ChatThreadAdapter;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.Configuration;
import com.thedappapp.dapp.app.DatabaseOperationCodes;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.chat.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatThreadActivity extends DappActivity {

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private Conversation mRoom;
    private ChatThreadAdapter adapter;
    private TextView nocontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_thread);
        nocontent = (TextView) findViewById(R.id.no_content_message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRoom = getIntent().getExtras().getParcelable("conversation");
        setTitle(mRoom.getOtherGroup().getName());

        messagesContainer = (ListView) findViewById(R.id.message_list);
        messageET = (EditText) findViewById(R.id.edit_message);
        sendBtn = (Button) findViewById(R.id.send_message);

        loadHistory();

        messagesContainer.setDivider(null);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageET.getText().toString();
                String from = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                Message message = new Message(msg, from);

                messageET.setText("");
                displayMessage(message);
                sendMessage(message);
            }
        });
    }

    private void loadHistory(){
        List<Message> messages = Configuration.getMessagesFromRoom(mRoom);
        if (messages == null) {
            nocontent.setText("Start chatting!");
            nocontent.setVisibility(View.VISIBLE);
            messages = new ArrayList<>();
        }

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
        message.intoConversation(mRoom.getMeta().getUid()).saveToFirebase(DatabaseOperationCodes.CREATE);
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

}