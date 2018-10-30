package com.chit.chat


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chit.chat.models.ChatPreviewModel
import com.chit.chat.viewholders.ChatPreviewViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_chat.view.*

class ChatFragment : Fragment() {

    lateinit var vu: View
    private var mRef: DatabaseReference? = null
    private var currUid: String? = null
    private var mRecyclerAdapter: FirebaseRecyclerAdapter<ChatPreviewModel, ChatPreviewViewHolder>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        vu = inflater.inflate(R.layout.fragment_chat, container, false)


        val user = FirebaseAuth.getInstance().currentUser

        mRef = FirebaseUtil.database.reference
        val mChatPreviewRef = mRef!!.child("chat_preview").child(user!!.uid).orderByChild("timestamp")

        val mRecyclerView = vu.findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context)

        val options = FirebaseRecyclerOptions.Builder<ChatPreviewModel>().setLifecycleOwner { this.lifecycle }
                .setQuery(mChatPreviewRef, ChatPreviewModel::class.java)
                .build()

        mRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatPreviewModel, ChatPreviewViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPreviewViewHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list, parent, false)

                return ChatPreviewViewHolder(view)
            }

            override fun onBindViewHolder(chatPreviewViewHolder: ChatPreviewViewHolder, position: Int, chatPreviewModel: ChatPreviewModel) {
                val chatid = getRef(position).key
                chatPreviewViewHolder.onBind(chatPreviewModel)

                if (chatPreviewModel.is_group) {
                    chatPreviewViewHolder.fetchAndBindGrouprData(mRef!!.child("groups").child(chatid!!))
                } else {
                    chatPreviewViewHolder.fetchAndBindUserData(mRef!!.child("users").child(chatPreviewModel.recipient_uid))
                }

                chatPreviewViewHolder.cardView.setOnClickListener { showUserChat(chatid, chatPreviewModel.is_group) }

            }
        }
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = mRecyclerAdapter
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true


//
//        GlideUtil.loadCircleProfileIcon(student.getprofilePic(), viewHolder.profileImage)
//
//        val status = student.getStatus()
//        if (status != null && status == "unread") {
//            viewHolder.unreadMessageCount.setVisibility(View.VISIBLE)
//            viewHolder.nameOrTitle.setTypeface(null, Typeface.BOLD)
//            viewHolder.message.setTypeface(null, Typeface.BOLD)
//        } else {
//            viewHolder.unreadMessageCount.setVisibility(View.GONE)
//            viewHolder.nameOrTitle.setTypeface(null, Typeface.NORMAL)
//            viewHolder.message.setTypeface(null, Typeface.NORMAL)
//        }
//
//        if (student.getCount() > 0) {
//                    val numMessages = String.valueOf(student.getCount())
//                    viewHolder.unreadMessageCount.setText(numMessages)
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
//            viewHolder.timeDate.setText(dateString)
//        }
//
//                viewHolder.cardView.setOnClickListener(View.OnClickListener {
//                    showUserChat(chatid, student.getUid(), student.getName())
//                    statusUpdate(chatid)
//                })
//                viewHolder.profileImage.setOnClickListener(View.OnClickListener { showUserDetailedProfile(student.getUid(), student.getReceiverrole()) })


        vu.signOutButton.setOnClickListener {
            signOut()
        }

        return vu
    }


    private fun showUserChat(chatId: String?, isGroup: Boolean) {
        val userDetailIntent = Intent(activity?.baseContext, ChatOpenActivity::class.java)
        val extras = Bundle()
        extras.putString("CHAT_ID", chatId)

        extras.putString("ROUTE", "CHAT_FRAGMENT")
        extras.putBoolean("IS_GROUP", isGroup)


        userDetailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.baseContext?.startActivity(userDetailIntent.putExtras(extras))
    }

//
//    fun statusUpdate(chatID: String?) {
//        val mChatPreview = mRef!!.child("chat_preview").child(currUid!!).child(chatID!!)
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

    private fun signOut() {
        // Sign out of Firebase
        FirebaseAuth.getInstance().signOut()
        // Define an intent to AuthenticationActivity and start it (go to the other activity)
        val startAuthenticationActivity = Intent(this.activity, AuthenticationActivity::class.java)
        this.activity?.startActivity(startAuthenticationActivity)
        // Destroy the current activity to prevent users from being able to click back and opening the MainActivity again
        this.activity?.finish()
    }

    companion object {

        val TAG = "ChatFragment"
    }
}


//
//
//val user = FirebaseAuth.getInstance().currentUser
//currUid = user!!.uid
//
//mRef = FirebaseUtil.database.reference
//val mUserRef = mRef!!.child("chat_preview").child(currUid!!)
////val orderedByTimeStamp = mUserRef.orderByChild("timestamp")
//
//Log.d("something", "with a log")
//val mRecyclerView = findViewById<RecyclerView>(R.id.my_recycler_view)
//mRecyclerView.layoutManager = LinearLayoutManager(this)
//
//val options = FirebaseRecyclerOptions.Builder<ChatPreviewModel>().setLifecycleOwner { this.lifecycle }
//        .setQuery(mUserRef, ChatPreviewModel::class.java)
//        .build()
//
//
//mRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatPreviewModel, ChatPreviewViewHolder>(options) {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPreviewViewHolder {
//        // Create a new instance of the ViewHolder, in this case we are using a custom
//        // layout called R.layout.message for each item
//        Log.e("something", "something onCreateViewHolder")
//
//        val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.item_list, parent, false)
//
//        return ChatPreviewViewHolder(view)
//    }
//
//    override fun onBindViewHolder(viewHolder: ChatPreviewViewHolder, position: Int, model: ChatPreviewModel) {
//        Log.e("something", model.message)
//        viewHolder.nameOrTitle.text = model.getName()
//        viewHolder.message.text = model.getMessage()
//        val chatid = getRef(position).key
//
//        viewHolder.cardView.setOnClickListener { startChatActivity(chatid!!, "Chat Title") }
//
//    }
//}
//
//mRecyclerView.adapter = mRecyclerAdapter
////
////        GlideUtil.loadCircleProfileIcon(student.getprofilePic(), viewHolder.profileImage)
////
////        val status = student.getStatus()
////        if (status != null && status == "unread") {
////            viewHolder.unreadMessageCount.setVisibility(View.VISIBLE)
////            viewHolder.nameOrTitle.setTypeface(null, Typeface.BOLD)
////            viewHolder.message.setTypeface(null, Typeface.BOLD)
////        } else {
////            viewHolder.unreadMessageCount.setVisibility(View.GONE)
////            viewHolder.nameOrTitle.setTypeface(null, Typeface.NORMAL)
////            viewHolder.message.setTypeface(null, Typeface.NORMAL)
////        }
////
////        if (student.getCount() > 0) {
////                    val numMessages = String.valueOf(student.getCount())
////                    viewHolder.unreadMessageCount.setText(numMessages)
////        }
////
////        var timestamp: Long? = null
////        timestamp = student.timestampMessage
////        if (timestamp != null) {
////            val mydate = Calendar.getInstance()
////            mydate.timeInMillis = student.timestampMessage
////            val hourOfDay = mydate.get(Calendar.HOUR_OF_DAY)
////            val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
////            val minute = mydate.get(Calendar.MINUTE)
////            val minuteString = if (minute < 10) "0$minute" else "" + minute
////
////            val dateString = hourString + ":" + minuteString + "  (" + mydate.get(Calendar.DAY_OF_MONTH) + "." + (mydate.get(Calendar.MONTH) + 1) + ")"
////            viewHolder.timeDate.setText(dateString)
////        }
////
////                viewHolder.cardView.setOnClickListener(View.OnClickListener {
////                    showUserChat(chatid, student.getUid(), student.getName())
////                    statusUpdate(chatid)
////                })
////                viewHolder.profileImage.setOnClickListener(View.OnClickListener { showUserDetailedProfile(student.getUid(), student.getReceiverrole()) })
//
//
//findViewById<Button>(R.id.signOutButton).setOnClickListener{
//    signOut()
//}
//
//
//
//}
//
//private fun signOut() {
//    // Sign out of Firebase
//    FirebaseAuth.getInstance().signOut()
//    // Define an intent to AuthenticationActivity and start it (go to the other activity)
//    val startAuthenticationActivity = Intent(this@MainActivity, AuthenticationActivity::class.java)
//    this@MainActivity.startActivity(startAuthenticationActivity)
//    // Destroy the current activity to prevent users from being able to click back and opening the MainActivity again
//    finish()
//}
//
//
//private fun startChatActivity(chat_id: String, chat_title: String) {
//    val userDetailIntent = Intent(this, ChatOpenActivity::class.java)
//    val extras = Bundle()
//    extras.putString("CHAT_ID", chat_id)
//    extras.putString("CHAT TITLE", chat_title)
//    this.startActivity(userDetailIntent.putExtras(extras))
//}
////
////    private fun showUserDetailedProfile(profileUid: String, receiverRole: String) {
////
////        val showUserIntent = Intent(c, TutorFinderDetail::class.java)
////        val extras = Bundle()
////        extras.putString("PROFILE_ROLE", receiverRole)
////        extras.putString("PROFILE_UID", profileUid)
////        c.startActivity(showUserIntent.putExtras(extras))
////    }
////
////    fun statusUpdate(chatID: String) {
////        val mChatPreview = mRef.child("chat_preview").child(currUid).child(chatID)
////
////        val profileUpdates = HashMap<String, Any>()
////        profileUpdates["status"] = "read"
////        profileUpdates["count"] = 0
////
////        mChatPreview.updateChildren(profileUpdates) { databaseError, databaseReference ->
////            if (databaseError != null) {
////                Log.e(TAG, "Failed to updateCounter", databaseError.toException())
////
////            }
////        }
////    }