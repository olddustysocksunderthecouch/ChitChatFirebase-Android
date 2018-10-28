package com.chit.chat.models

data class SendMessageCallback(val message: String = "", val status: String = "", val body: Body?, val code: String = "")
data class Body(val chat_id: String = "")