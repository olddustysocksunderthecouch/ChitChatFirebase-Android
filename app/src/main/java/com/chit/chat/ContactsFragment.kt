package com.chit.chat


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chit.chat.models.UserModel
import com.chit.chat.utils.FirebaseUtil
import com.chit.chat.viewholders.ContactsViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ContactsFragment : Fragment() {

    lateinit var vu: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        vu = inflater.inflate(R.layout.fragment_chat, container, false)

        val mRef = FirebaseUtil.database.reference
        val mUserRef = mRef!!.child("users")

        val recyclerView = vu.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val options = FirebaseRecyclerOptions.Builder<UserModel>().setLifecycleOwner { this.lifecycle }
                .setQuery(mUserRef, UserModel::class.java)
                .build()

        val mRecyclerAdapter = object : FirebaseRecyclerAdapter<UserModel, ContactsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.viewholder_message for each item

                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.viewholder_contacts, parent, false)

                return ContactsViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ContactsViewHolder, position: Int, model: UserModel) {
                viewHolder.name.text = model.display_name
                viewHolder.cardView.setOnClickListener {
                    showUserChat(getRef(position).key)
                }

                Glide.with(this@ContactsFragment)
                        .load(model.profile_pic)
                        .apply(RequestOptions().circleCrop())
                        .into(viewHolder.profileImage)
            }
        }
        recyclerView.adapter = mRecyclerAdapter

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



