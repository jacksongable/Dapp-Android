package com.thedappapp.dapp.app;

import com.thedappapp.dapp.objects.chat.Conversation;
import com.thedappapp.dapp.objects.chat.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jackson on 1/26/17.
 */

public class ChatStorage {

    private Map<Conversation, List<Message>> map;
    private List<Conversation> conversations;

    ChatStorage() {
        map = new HashMap<>();
        conversations = new ArrayList<>();
    }

    public void createNewConversation (Conversation convo) {
        map.put(convo, new ArrayList<Message>());
        conversations.add(convo);
    }

    public void putMessage (Conversation convo, Message message) {
        map.get(convo).add(message);
    }

    public List<Message> getMessages (Conversation convo) {
        return map.get(convo);
    }

    public List<Conversation> getConversations () {
        return conversations;
    }

}
