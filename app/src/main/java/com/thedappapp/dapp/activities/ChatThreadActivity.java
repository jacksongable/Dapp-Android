package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thedappapp.dapp.adapters.ChatThreadAdapter;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.app.SaveKeys;
import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.chat.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatThreadActivity extends DappActivity {

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private Conversation mConversation;
    private ChatThreadAdapter adapter;
    private TextView nocontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_thread);
        nocontent = (TextView) findViewById(R.id.no_content_message);
        messagesContainer = (ListView) findViewById(R.id.message_list);
        messageET = (EditText) findViewById(R.id.edit_message);
        sendBtn = (Button) findViewById(R.id.send_message);
        mConversation = getIntent().getExtras().getParcelable("conversation");

        setTitle(mConversation.getOtherUser());
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadHistory();

        messagesContainer.setDivider(null);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageET.getText().toString();
                String from = App.getApp().me().getDisplayName();

                Message message = new Message(msg, from);

                messageET.setText("");
                displayMessage(message);
                sendMessage(message);
            }
        });
    }

    private void loadHistory(){
        List<Message> messages = App.getApp().getChatStorage().getMessages(mConversation);
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
        message.intoConversation(mConversation.getMeta().getUid()).save(SaveKeys.CREATE);
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

}