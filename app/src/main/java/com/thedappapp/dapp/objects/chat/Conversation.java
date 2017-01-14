package com.thedappapp.dapp.objects.chat;

/**
 * Created by jackson on 8/20/16.
 */
public class Conversation {

    private String user1, user2;

    public Conversation() {}

    Conversation(String me, String them) {
        user1 = me;
        user2 = them;
    }

    public String getId () {
        return getObjectId();
    }

    public Group getMyGroup () {
        try {
            Group g1 = getParseObject("groupOne").fetchIfNeeded();
            Group g2 = getParseObject("groupTwo").fetchIfNeeded();

            if (g1.getLeaderAsString().equals(User.me().getUsername())) return g1;
            else return g2;

        } catch (ParseException e) {
            Log.e("Chatroom Object", Log.getStackTraceString(e));
        }
        return null;
    }

    public Group getOtherGroup () {
        try {
            Group g1 = getParseObject("groupOne").fetchIfNeeded();
            Group g2 = getParseObject("groupTwo").fetchIfNeeded();

            if (g1.getLeaderAsString().equals(User.me().getUsername())) return g2;
            else return g1;

        } catch (ParseException e) {
            Log.e("Chatroom Object", Log.getStackTraceString(e));
        }
        return null;
    }

    public static Chatroom from (String id) throws ParseException {
        ParseQuery<Chatroom> query = ParseQuery.getQuery(Chatroom.class);
        return query.get(id);
    }
}
