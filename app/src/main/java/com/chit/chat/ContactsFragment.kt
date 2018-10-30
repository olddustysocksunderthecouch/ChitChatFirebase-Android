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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chit.chat.models.UserModel
import com.chit.chat.viewholders.ChatPreviewViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class ContactsFragment : Fragment() {

    lateinit var vu: View
    private var mRef: DatabaseReference? = null
    private var currUid: String? = null
    private var mRecyclerAdapter: FirebaseRecyclerAdapter<UserModel, ChatPreviewViewHolder>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        vu = inflater.inflate(R.layout.fragment_chat, container, false)


        val user = FirebaseAuth.getInstance().currentUser
        currUid = user!!.uid
        Log.e("UID", currUid)

        mRef = FirebaseUtil.database.reference
        val mUserRef = mRef!!.child("users")

        val mRecyclerView = vu.findViewById<RecyclerView>(R.id.recyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        val options = FirebaseRecyclerOptions.Builder<UserModel>().setLifecycleOwner { this.lifecycle }
                .setQuery(mUserRef, UserModel::class.java)
                .build()


        mRecyclerAdapter = object : FirebaseRecyclerAdapter<UserModel, ChatPreviewViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPreviewViewHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item

                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list, parent, false)

                return ChatPreviewViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ChatPreviewViewHolder, position: Int, model: UserModel) {
                viewHolder.nameOrTitle.text = model.display_name
                viewHolder.cardView.setOnClickListener {
                    showUserChat(getRef(position).key)
                }

                Glide.with(this@ContactsFragment)
                        .load(model.profile_pic)
                        .apply(RequestOptions().circleCrop())
                        .into(viewHolder.profileImage)
            }
        }

        mRecyclerView.adapter = mRecyclerAdapter


        return vu
    }

    private fun showUserChat(receiverUID: String?) {
        val userDetailIntent = Intent(activity?.baseContext, ChatOpenActivity::class.java)
        userDetailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val extras = Bundle()
        extras.putString("ROUTE", "CONTACTS_FRAGMENT")
        extras.putString("RECEIVER_UID", receiverUID)

        activity?.baseContext?.startActivity(userDetailIntent.putExtras(extras))
    }


    companion object {

        val TAG = "ChatFragment"
    }
}



