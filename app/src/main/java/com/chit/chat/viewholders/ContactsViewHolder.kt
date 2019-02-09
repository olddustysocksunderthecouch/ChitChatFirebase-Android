package com.chit.chat.viewholders

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chit.chat.models.UserModel
import kotlinx.android.synthetic.main.viewholder_contacts.view.*


/**
 * Created by Adrian Bunge on 30/10/2018.
 */

class ContactsViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var name: TextView = view.nameEditText
    var profileImage: ImageView = view.profilePictureImageView
    var cardView: ConstraintLayout = view.contactsCardView


    fun onBind(usersModel: UserModel){
        name.text = usersModel.display_name
    }
}


