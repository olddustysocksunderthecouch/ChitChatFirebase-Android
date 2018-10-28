package com.chit.chat.viewholders;

import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chit.chat.R;


/**
 * Created by Adria on 20-Aug-17.
 */

public class ChatHolder extends RecyclerView.ViewHolder {
    public View mView;

    public ChatHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setIsSender(Boolean isSender) {
        FrameLayout left_arrow = (FrameLayout) mView.findViewById(R.id.left_arrow);
        FrameLayout right_arrow = (FrameLayout) mView.findViewById(R.id.right_arrow);
        RelativeLayout messageContainer = (RelativeLayout) mView.findViewById(R.id.message_container);
        ConstraintLayout message = (ConstraintLayout) mView.findViewById(R.id.message);
        ImageView readsent = (ImageView) mView.findViewById(R.id.readsent);
        // TextView field = (TextView) mView.findViewById(R.id.startlessontitle_text);


        int color;
        if (isSender) {
            color = ContextCompat.getColor(mView.getContext(), R.color.material_green_300);
            left_arrow.setVisibility(View.GONE);
            right_arrow.setVisibility(View.VISIBLE);
            messageContainer.setGravity(Gravity.END);
            readsent.setVisibility(View.VISIBLE);



        } else {
            color = ContextCompat.getColor(mView.getContext(), R.color.material_gray_300);
            left_arrow.setVisibility(View.VISIBLE);
            right_arrow.setVisibility(View.GONE);
            messageContainer.setGravity(Gravity.START);
            readsent.setVisibility(View.GONE);
        }

        ((GradientDrawable) message.getBackground()).setColor(color);
        ((RotateDrawable) left_arrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
        ((RotateDrawable) right_arrow.getBackground()).getDrawable()
                .setColorFilter(color, PorterDuff.Mode.SRC);
    }


    public void setText(String text) {
        TextView field = (TextView) mView.findViewById(R.id.startlessontitle_text);
        field.setText(text);
        field.setGravity(Gravity.START);
    }
    public void setTime(String text) {
        TextView field = (TextView) mView.findViewById(R.id.time_text);
        field.setText(text);
    }
    public void setSentRead(int resourceId){
        ImageView readsent = (ImageView) mView.findViewById(R.id.readsent);
        readsent.setImageResource(resourceId);

    }
}
