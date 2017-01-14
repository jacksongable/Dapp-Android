package com.thedappapp.dapp.objects.chat;


import com.thedappapp.dapp.objects.group.Group;

/**
 * Created by jackson on 8/19/16.
 */
public class Message {

    private String message, sender, receiver, conversation;

    public Message () {}

    Message (String message, String sender, String receiver, String conversation) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.conversation = conversation;
    }

    public String getMessage () {
        return message;
    }

    public String getConversation () {
        return conversation;
    }

    public String getSender () {
        return sender;
    }

    public String getReceiver () {
        return receiver;
    }

    public static ChatMessage from (String id) throws ParseException {
        ParseQuery<ChatMessage> query = ParseQuery.getQuery(ChatMessage.class);
        return query.get(id);
    }

}
