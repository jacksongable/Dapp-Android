package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.chat.ChatMetaShell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class ChatSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private List<ChatMetaShell> conversations;
    private List<String> convoUids;

    public ChatSelectorAdapter (Context context, List<ChatMetaShell> list) {
        mContext = context;
        conversations = list;
        convoUids = new ArrayList<>();

        for (ChatMetaShell shell : list)
            convoUids.add(shell.getChat_id());
    }

    @Override
    public int getCount() {
        if (conversations == null) return 0;
        return conversations.size();
    }

    @Override
    public Object getItem(int i) {
        if (conversations == null) return null;
        return conversations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (view == null) {
            view = inflater.inflate(R.layout.content_chat_label, null);
            holder = createViewHolder(view);
            holder.otherUser.setText(conversations.get(i).getGroup_name());
            holder.lastMessage.setText(conversations.get(i).getLast_message());

            if (conversations.get(i).getUnread() != 0)
                holder.unreadBadge.setText(String.valueOf(conversations.get(i).getUnread()));
            else holder.unreadBadge.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private ViewHolder createViewHolder (View view) {
        ViewHolder holder = new ViewHolder();
        holder.otherUser = (TextView) view.findViewById(R.id.group_name);
        holder.lastMessage = (TextView) view.findViewById(R.id.last);
        holder.unreadBadge = (TextView) view.findViewById(R.id.unread_count);
        return holder;
    }

    public boolean add (ChatMetaShell shell) {
        if (convoUids.contains(shell.getChat_id()))
            return false;
        conversations.add(shell);
        convoUids.add(shell.getChat_id());
        notifyDataSetChanged();
        return true;
    }

    private static class ViewHolder {
        private TextView otherUser;
        private TextView lastMessage;
        private TextView unreadBadge;
    }

}
