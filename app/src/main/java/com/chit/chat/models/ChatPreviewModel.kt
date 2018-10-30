package com.chit.chat.models

import com.chit.chat.DateUtil

data class ChatPreviewModel(
        @field:JvmField val is_group: Boolean = false,
        val last_message: String = "",
        val sender_name: String = "",
        val sender_uid: String = "",
        val recipient_uid: String = "",
        val status: String = "",
        val unread_message_count: Int = 0,
        val timestamp: Long = 0L) {

    val timeDateString: String
        get() = DateUtil.dateFromMiliSecSinceEpoch(timestamp)
}


