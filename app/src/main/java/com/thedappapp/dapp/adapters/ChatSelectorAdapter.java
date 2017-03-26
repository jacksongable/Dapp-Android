package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.chat.ActiveChatShell;
import com.thedappapp.dapp.objects.chat.Conversation;
import java.util.List;

/**
 * Created by jackson on 8/20/16.
 */
public class ChatSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private List<ActiveChatShell> conversations;

    public ChatSelectorAdapter (Context context, List<ActiveChatShell> list) {
        mContext = context;
        conversations = list;
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
            //String msg = conversations.get(i).getLastMessage();
            holder = createViewHolder(view);
            holder.otherUser.setText(conversations.get(i).getGroup_name());
            //holder.lastMessage.setText(msg);
        }

        return view;
    }

    private ViewHolder createViewHolder (View view) {
        ViewHolder holder = new ViewHolder();
        holder.otherUser = (TextView) view.findViewById(R.id.group_name);
        //holder.lastMessage = (TextView) view.findViewById(R.id.last_message);
        return holder;
    }

    public void add (ActiveChatShell shell) {
        conversations.add(shell);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView otherUser;
        //public TextView lastMessage;
    }

}
