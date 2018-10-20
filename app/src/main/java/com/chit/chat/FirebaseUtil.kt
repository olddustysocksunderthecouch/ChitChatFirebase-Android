package com.voting.group.dev.googel.chitchat

import com.google.firebase.database.FirebaseDatabase


/**
 * Created by Adrian Bunge on 2018/09/08.
 */

object FirebaseUtil {

    private var mDatabase: FirebaseDatabase? = null

    val database: FirebaseDatabase
        get() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance()
                mDatabase!!.setPersistenceEnabled(true)
            }

            return mDatabase!!
        }
}