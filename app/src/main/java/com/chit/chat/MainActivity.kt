package com.voting.group.dev.googel.chitchat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.voting.group.dev.googel.chitchat.models.ChatPreviewModel
import com.voting.group.dev.googel.chitchat.viewholders.ViewHolderChatPreview
import com.voting.group.dev.googel.picupchatapp.R

/**
 * Created by Adrian Bunge on 2018/09/08.
 */

class MainActivity : AppCompatActivity() {
    private var mRef: DatabaseReference? = null
    private var currUid: String? = null
    private lateinit var mRecyclerAdapter: FirebaseRecyclerAdapter<ChatPreviewModel, ViewHolderChatPreview>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val user = FirebaseAuth.getInstance().currentUser
        currUid = user!!.uid

        mRef = FirebaseUtil.database.reference
        val mUserRef = mRef!!.child("chat_preview").child(currUid!!)
        //val orderedByTimeStamp = mUserRef.orderByChild("timestamp")

        Log.d("something", "with a log")
        val mRecyclerView = findViewById<RecyclerView>(R.id.my_recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val options = FirebaseRecyclerOptions.Builder<ChatPreviewModel>().setLifecycleOwner { this.lifecycle }
                .setQuery(mUserRef, ChatPreviewModel::class.java)
                .build()


        mRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatPreviewModel, ViewHolderChatPreview>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderChatPreview {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                Log.e("something", "something onCreateViewHolder")

                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list, parent, false)

                return ViewHolderChatPreview(view)
            }

            override fun onBindViewHolder(viewHolder: ViewHolderChatPreview, position: Int, model: ChatPreviewModel) {
                Log.e("something", model.message)
                viewHolder.vFirst.text = model.getName()
                viewHolder.messagePreview.text = model.getMessage()
                val chatid = getRef(position).key

                viewHolder.card.setOnClickListener { startChatActivity(chatid!!, "Chat Title") }

            }
        }

        mRecyclerView.adapter = mRecyclerAdapter
//
//        GlideUtil.loadCircleProfileIcon(student.getprofilePic(), viewHolder.profileImage)
//
//        val status = student.getStatus()
//        if (status != null && status == "unread") {
//            viewHolder.vNumberMessages.setVisibility(View.VISIBLE)
//            viewHolder.vFirst.setTypeface(null, Typeface.BOLD)
//            viewHolder.messagePreview.setTypeface(null, Typeface.BOLD)
//        } else {
//            viewHolder.vNumberMessages.setVisibility(View.GONE)
//            viewHolder.vFirst.setTypeface(null, Typeface.NORMAL)
//            viewHolder.messagePreview.setTypeface(null, Typeface.NORMAL)
//        }
//
//        if (student.getCount() > 0) {
//                    val numMessages = String.valueOf(student.getCount())
//                    viewHolder.vNumberMessages.setText(numMessages)
//        }
//
//        var timestamp: Long? = null
//        timestamp = student.timestampMessage
//        if (timestamp != null) {
//            val mydate = Calendar.getInstance()
//            mydate.timeInMillis = student.timestampMessage
//            val hourOfDay = mydate.get(Calendar.HOUR_OF_DAY)
//            val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
//            val minute = mydate.get(Calendar.MINUTE)
//            val minuteString = if (minute < 10) "0$minute" else "" + minute
//
//            val dateString = hourString + ":" + minuteString + "  (" + mydate.get(Calendar.DAY_OF_MONTH) + "." + (mydate.get(Calendar.MONTH) + 1) + ")"
//            viewHolder.vTime.setText(dateString)
//        }
//
//                viewHolder.card.setOnClickListener(View.OnClickListener {
//                    showUserChat(chatid, student.getUid(), student.getName())
//                    statusUpdate(chatid)
//                })
//                viewHolder.profileImage.setOnClickListener(View.OnClickListener { showUserDetailedProfile(student.getUid(), student.getReceiverrole()) })





    }



    private fun startChatActivity(chat_id: String, chat_title: String) {
        val userDetailIntent = Intent(this, ChatOpenActivity::class.java)
        val extras = Bundle()
        extras.putString("CHAT_ID", chat_id)
        extras.putString("CHAT TITLE", chat_title)
        this.startActivity(userDetailIntent.putExtras(extras))
    }
//
//    private fun showUserDetailedProfile(profileUid: String, receiverRole: String) {
//
//        val showUserIntent = Intent(c, TutorFinderDetail::class.java)
//        val extras = Bundle()
//        extras.putString("PROFILE_ROLE", receiverRole)
//        extras.putString("PROFILE_UID", profileUid)
//        c.startActivity(showUserIntent.putExtras(extras))
//    }
//
//    fun statusUpdate(chatID: String) {
//        val mChatPreview = mRef.child("chat_preview").child(currUid).child(chatID)
//
//        val profileUpdates = HashMap<String, Any>()
//        profileUpdates["status"] = "read"
//        profileUpdates["count"] = 0
//
//        mChatPreview.updateChildren(profileUpdates) { databaseError, databaseReference ->
//            if (databaseError != null) {
//                Log.e(TAG, "Failed to updateCounter", databaseError.toException())
//
//            }
//        }
//    }

}
