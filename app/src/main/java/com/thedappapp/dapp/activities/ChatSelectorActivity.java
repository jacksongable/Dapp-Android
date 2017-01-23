package com.thedappapp.dapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.ChatSelectorAdapter;
import com.thedappapp.dapp.app.Configuration;
import com.thedappapp.dapp.app.StaticChatRoomReference;
import com.thedappapp.dapp.objects.chat.Chatroom;
import com.thedappapp.dapp.objects.chat.Conversation;

import java.util.LinkedList;
import java.util.List;

public class ChatSelectorActivity extends DappActivity {

    private ListView vListSelector;
    private ChatSelectorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selector);
    }

    @Override
    protected void onStart() {
        vListSelector = (ListView) findViewById(R.id.chat_selector_list);
        List<Conversation> rooms = Configuration.getChatrooms();

        if (rooms.isEmpty()) {
            TextView empty = (TextView) findViewById(R.id.no_content_message);
            empty.setText("Start dapping up other groups to chat!");
            empty.setVisibility(View.VISIBLE);
            return;
        }

        mAdapter = new ChatSelectorAdapter(this, rooms);
        vListSelector.setAdapter(mAdapter);
        vListSelector.setFooterDividersEnabled(true);
        vListSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Conversation conversation = (Conversation) mAdapter.getItem(i);

                Intent intent = new Intent(ChatSelectorActivity.this, ChatThreadActivity.class);
                intent.putExtra("conversation", conversation);
                startActivity(intent);
            }
        });
    }

}
