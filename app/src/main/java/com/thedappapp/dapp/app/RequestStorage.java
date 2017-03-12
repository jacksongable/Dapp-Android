package com.thedappapp.dapp.app;

import com.thedappapp.dapp.objects.group.Group;
import com.thedappapp.dapp.objects.Request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jackson on 1/26/17.
 */

public class RequestStorage {

    private Map<String, Request> outgoing, incoming;
    private List<String> friends;

    RequestStorage() {
        outgoing = new HashMap<>();
        incoming = new HashMap<>();
        friends = new ArrayList<>();
    }

    public void putAllOutgoingRequests(List<Request> list) {
      Iterator<Request> iterator = list.iterator();
        while (iterator.hasNext()) {
            Request r = iterator.next();
            outgoing.put(r.getTo(), r);
        }
    }

    public void putAllIncomingRequests(List<Request> requests) {
        Iterator<Request> iterator = requests.iterator();
        while (iterator.hasNext()) {
            Request r = iterator.next();
            incoming.put(r.getFrom(), r);
        }

    }

    public void putOutgoingRequest(Request request) {;
        outgoing.put(request.getTo(), request);
    }

    public void putIncomingRequest(Request request) {
        incoming.put(request.getFrom(), request);
    }

    public boolean hasDappedUp(Group group) {
        return outgoing.containsKey(group.getLeaderId());
    }

    public boolean isDappedUpBy(Group group) {
        return incoming.containsKey(group.getLeaderId());
    }

    public Iterator<Request> getOutgoingIterator () {
        return outgoing.values().iterator();
    }

    public Iterator<Request> getIncomingIterator() {
        return incoming.values().iterator();
    }

    public List<Request> getOutgoingAsList () {
        return new ArrayList<>(outgoing.values());
    }

    public List<Request> getIncomingAsList () {
        return new ArrayList<>(incoming.values());
    }

    public void onIncomingRequestAccepted (Request request) {
        incoming.remove(request);
        friends.add(request.getFrom());
    }

    public void onOutgoingInviteAccepted (Request request) {
        friends.add(request.getTo());
        outgoing.remove(request.getTo());
    }

    public Request get (String id) {
        if (outgoing.containsKey(id))
            return outgoing.get(id);
        else if (incoming.containsKey(id))
            return incoming.get(id);
        else return null;
    }

    public boolean isFriends (String userId) {
        return friends.contains(userId);
    }

    public boolean isFriends (Group group) {
        return friends.contains(group.getLeaderId());
    }


}
