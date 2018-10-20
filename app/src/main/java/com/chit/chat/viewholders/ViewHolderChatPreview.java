package com.voting.group.dev.googel.chitchat.viewholders;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.voting.group.dev.googel.picupchatapp.R;


/**
 * Created by Adria on 20-Aug-17.
 */

public class ViewHolderChatPreview extends RecyclerView.ViewHolder {
    public View view;
    public TextView vFirst;
    public TextView messagePreview;
    public ImageView profileImage;
    public TextView vNumberMessages;
    public TextView vTime;
    public ConstraintLayout card;


    public ViewHolderChatPreview(View itemView) {
        super(itemView);
        view = itemView;

        vFirst = (TextView) itemView.findViewById(R.id.list_title);
        messagePreview = (TextView) itemView.findViewById(R.id.list_desc);
        profileImage = (ImageView) itemView.findViewById(R.id.list_avatar);
        vNumberMessages = (TextView) itemView.findViewById(R.id.numnewmessages);
        vTime = (TextView) itemView.findViewById(R.id.timeofmessage);
        card = (ConstraintLayout) itemView.findViewById(R.id.list_card);


    }
}
