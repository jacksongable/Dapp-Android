package com.thedappapp.dapp.objects.chat;

/**
 * Created by jackson on 10/12/16.
 */
public class MessageFactory {

    private String message, sender, receiver, conversation;

    public MessageFactory withMessage (String text) {
        message = text;
        return this;
    }

    public MessageFactory from (String sender) {
        this.sender = sender;
        return this;
    }

    public MessageFactory inConversation(String conversation) {
        this.conversation = conversation;
        return this;
    }

    public MessageFactory to (String receiver) {
        this.receiver = receiver;
        return this;
    }

    public Message build () {
        return new Message(message, sender, receiver, conversation);
    }

}
