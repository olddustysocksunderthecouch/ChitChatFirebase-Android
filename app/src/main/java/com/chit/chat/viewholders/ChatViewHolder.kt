package com.chit.chat.viewholders

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RotateDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import com.chit.chat.R
import kotlinx.android.synthetic.main.viewholder_message.view.*


/**
 * Created by Adrian Bunge on 30/10/2018.
 */

class ChatViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {

    fun setIsSender(isSender: Boolean?) {
        val leftArrow = mView.left_arrow
        val rightArrow = mView.right_arrow
        val messageContainer = mView.message_container
        val message = mView.message
        val readSent = mView.readsent

        val color: Int
        if (isSender!!) {
            color = ContextCompat.getColor(mView.context, R.color.material_green_300)
            leftArrow.visibility = View.GONE
            rightArrow.visibility = View.VISIBLE
            messageContainer.gravity = Gravity.END
            readSent.visibility = View.VISIBLE

        } else {
            color = ContextCompat.getColor(mView.context, R.color.material_gray_300)
            leftArrow.visibility = View.VISIBLE
            rightArrow.visibility = View.GONE
            messageContainer.gravity = Gravity.START
            readSent.visibility = View.GONE
        }

        (message.background as GradientDrawable).setColor(color)
        (leftArrow.background as RotateDrawable).drawable!!
                .setColorFilter(color, PorterDuff.Mode.SRC)
        (rightArrow.background as RotateDrawable).drawable!!
                .setColorFilter(color, PorterDuff.Mode.SRC)
    }


    fun setText(text: String) {
        val field = mView.messageTextTextView
        field.text = text
        field.gravity = Gravity.START
    }

    fun setTime(text: String) {
        mView.time_text.text = text
    }

    fun setSentRead(resourceId: Int) {
        mView.readsent.setImageResource(resourceId)
    }
}
