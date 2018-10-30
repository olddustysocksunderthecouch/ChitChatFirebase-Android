package com.chit.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chit.chat.models.UserModel
import com.chit.chat.viewholders.ChatPreviewViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_new_group.*

class NewGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        val mRef = FirebaseUtil.database.reference
        val mUserRef = mRef.child("users")

        val listOfSelectedContacts = mutableListOf<String>()
        if(listOfSelectedContacts.isNotEmpty()){
            selected_users.text = listOfSelectedContacts.toString()
        }

        pick_contacts_layout.visibility = View.VISIBLE
        add_details_layout.visibility = View.GONE

        next_button.setOnClickListener {
            pick_contacts_layout.visibility = View.GONE
            add_details_layout.visibility = View.VISIBLE
            next_button.visibility = View.GONE
        }

        finish_create_group_button.setOnClickListener{
            createGroupCloudFunction(listOfSelectedContacts, group_name_edittext.text.toString() )
        }

        contactsRecyclerView.layoutManager = LinearLayoutManager(this)

        val options = FirebaseRecyclerOptions.Builder<UserModel>().setLifecycleOwner { this.lifecycle }
                .setQuery(mUserRef, UserModel::class.java)
                .build()

        val mRecyclerAdapter = object : FirebaseRecyclerAdapter<UserModel, ChatPreviewViewHolder>(options) {
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
                    val selectedUserUID = getRef(position).key
                    if(listOfSelectedContacts.contains(selectedUserUID)){
                        listOfSelectedContacts.remove(selectedUserUID)
                    }
                    else{
                        listOfSelectedContacts.add(selectedUserUID!!)
                    }
                    selected_users.text = listOfSelectedContacts.toString()

                }

                Glide.with(this@NewGroupActivity)
                        .load(model.profile_pic)
                        .apply(RequestOptions().circleCrop())
                        .into(viewHolder.profileImage)
            }
        }

        contactsRecyclerView.adapter = mRecyclerAdapter
    }

    private fun createGroupCloudFunction(selectedContacts: MutableList<String>, groupName: String) {
        // Create the arguments to the callable function.
        val data = HashMap<String, Any?>()
        data["uids"] = selectedContacts
        data["group_name"] = groupName
        data["profile_picture"] = "https://firebasestorage.googleapis.com/v0/b/chitchat-2faa0.appspot.com/o/1_sgte14nnEGB1cwlDbjbBrw.png?alt=media&token=1d39cad9-1030-45ab-b5a6-c1c4f8e1c8c5"

        FirebaseFunctions.getInstance()
                .getHttpsCallable("createGroup")
                .call(data)
                .continueWith { task ->
                    task.result.data as String
                }
    }



}
