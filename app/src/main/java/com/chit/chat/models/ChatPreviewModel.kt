package com.chit.chat.models

data class ChatPreviewModel(val last_message: String = "",
                            val sender_name: String = "",
                            val sender_uid: String = "",
                            val timestamp: Long = 0L,
                            val status: String = "",
                            val unread_message_count: Int = 0)


