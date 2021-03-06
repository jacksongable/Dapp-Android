package com.thedappapp.dapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.objects.group.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jackson on 12/18/16.
 */
public class InterestGridAdapter extends BaseAdapter {

    private Context mContext;

    private boolean[] interested = {false, false, false, false, false, false};

    private Integer[] offPics = {
            R.drawable.gcreate_food_off, R.drawable.gcreate_events_off,
            R.drawable.gcreate_music_off, R.drawable.gcreate_gaming_off,
            R.drawable.gcreate_sports_off, R.drawable.gcreate_party_off
    };

    private Integer[] onPics = {
            R.drawable.gcreate_food_on, R.drawable.gcreate_events_on,
            R.drawable.gcreate_music_on, R.drawable.gcreate_gaming_on,
            R.drawable.gcreate_sports_on, R.drawable.gcreate_party_on
    };

    private String[] interests = {
            "Food", "Entertainment", "Music", "Gaming", "Sports", "Party"
    };

    public InterestGridAdapter (Context c) {
        mContext = c;
    }

    public InterestGridAdapter (Context c, boolean[] interests) {
        this(c);
        this.interested = interests;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public Object getItem(int i) {
        return offPics[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageButton button;
        final TextView textView;
        final LinearLayout layout;

        if (convertView == null) {
            button = new ImageButton(mContext);
            textView = new TextView(mContext);
            button.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));


            if (!interested[position]) {
                button.setImageResource(offPics[position]);
                textView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
            }
            else {
                button.setImageResource(onPics[position]);
                textView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            }

            textView.setText(interests[position]);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);



            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!interested[position]) {
                        interested[position] = true;
                        button.setImageResource(onPics[position]);
                        textView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                    }
                    else {
                        interested[position] = false;
                        button.setImageResource(offPics[position]);
                        textView.setTextColor(mContext.getResources().getColor(R.color.gray_text));
                    }
                }
            });

            layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setPadding(8, 8, 8, 8);
            layout.addView(button);
            layout.addView(textView);
        }
        else layout = (LinearLayout) convertView;

        return layout;
    }

    public List<String> getInterests () {
        List<String> interests = new ArrayList<>();
        for (int index = 0; index < interested.length; index++)
            if (interested[index])
                interests.add(this.interests[index]);
        return interests;
    }

    public boolean hasInterest (String interest) {
        if (interest.equalsIgnoreCase("food"))
            return interested[0];
        else if (interest.equalsIgnoreCase("entertainment"))
            return interested[1];
        else if (interest.equalsIgnoreCase("music"))
            return interested[2];
        else if (interest.equalsIgnoreCase("gaming"))
            return interested[3];
        else if (interest.equalsIgnoreCase("sports"))
            return interested[4];
        else if (interest.equalsIgnoreCase("party"))
            return interested[5];
        else return false;
    }



}
