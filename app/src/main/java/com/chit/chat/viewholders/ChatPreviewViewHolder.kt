package com.chit.chat.viewholders

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chit.chat.models.ChatPreviewModel
import com.chit.chat.models.GroupModel
import com.chit.chat.models.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

import kotlinx.android.synthetic.main.item_list.view.*


/**
 * Created by Adrian Bunge on 30/10/2018.
 */

class ChatPreviewViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var nameOrTitle: TextView = view.list_title
    var message: TextView = view.list_desc
    var profileImage: ImageView = view.list_avatar
    var unreadMessageCount: TextView = view.numnewmessages
    var timeDate: TextView = view.timeofmessage
    var cardView: ConstraintLayout = view.list_card

    fun onBind(chatPreviewModel: ChatPreviewModel){
        message.text = chatPreviewModel.last_message
        timeDate.text = chatPreviewModel.timeDateString

        val unreadMessageCountInt = chatPreviewModel.unread_message_count
        if(unreadMessageCountInt > 0) {
            unreadMessageCount.visibility = View.VISIBLE
            unreadMessageCount.text = unreadMessageCountInt.toString()
        } else{
            unreadMessageCount.visibility = View.GONE
        }
    }


    fun fetchAndBindUserData(senderDatabaseReference: DatabaseReference) {
        senderDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userObject = dataSnapshot.getValue(UserModel::class.java)!!
                    nameOrTitle.text = userObject.display_name
                    Glide.with(view.context)
                            .load(userObject.profile_pic)
                            .apply(RequestOptions().circleCrop())
                            .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
            }
        })
    }

    fun fetchAndBindGrouprData(groupDatabaseReference: DatabaseReference) {
        groupDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userObject = dataSnapshot.getValue(GroupModel::class.java)!!
                    Log.e("fetchAndBindGrouprData",  dataSnapshot.key)
                    nameOrTitle.text = userObject.title
                    Glide.with(view.context)
                            .load(userObject.profile_picture)
                            .apply(RequestOptions().circleCrop())
                            .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
            }
        })
    }
}


