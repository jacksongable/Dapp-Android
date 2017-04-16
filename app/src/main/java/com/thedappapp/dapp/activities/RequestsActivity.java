package com.thedappapp.dapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.thedappapp.dapp.R;
import com.thedappapp.dapp.adapters.InvitationRecyclerAdapter;
import com.thedappapp.dapp.app.App;
import com.thedappapp.dapp.objects.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends DappActivity {

    private RecyclerView mRecycler;
    private boolean hasInvites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        List<Request> invites = new ArrayList<>();//App.getApp().getRequestStorage().getIncomingAsList();
        TextView noContent = (TextView) findViewById(R.id.no_content_message);
        noContent.setText("You have no new requests, so take charge! Start requesting people to chat!");

        mRecycler = (RecyclerView) findViewById(R.id.recyclerview);


        if (invites.isEmpty()) {
            noContent.setVisibility(View.VISIBLE);
            hasInvites = false;
        }

        else {
            hasInvites = true;
            populateCards(invites);
        }
    }


    public void populateCards (List<Request> invitations) {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(new InvitationRecyclerAdapter(this, invitations));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (hasInvites) {
            List<Request> accepted = ((InvitationRecyclerAdapter) mRecycler.getAdapter()).getAcceptedInvites();

            if (!accepted.isEmpty())
                for (Request r : accepted);
                    //App.getApp().getRequestStorage().onOutgoingInviteAccepted(r);
        }


    }
}
