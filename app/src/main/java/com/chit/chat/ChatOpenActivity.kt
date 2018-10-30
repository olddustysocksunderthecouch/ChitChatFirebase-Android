package com.chit.chat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chit.chat.models.MessageModel
import com.chit.chat.viewholders.ChatViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.android.synthetic.main.activity_chat.*
import kotlin.collections.HashMap


class ChatOpenActivity : AppCompatActivity() {

    private var context: Context? = null
    lateinit var mAuth: FirebaseAuth
    lateinit var mFunctions: FirebaseFunctions
    lateinit var mRef: DatabaseReference

    private var chatId: String? = null
    private var isGroup: Boolean = false
    private var chatMembers: MutableList<*> = mutableListOf<Any>()


    private var uid: String? = null
    private var CurrUserDisplayName: String? = null

    private var sharedPrefMessageDraft: SharedPreferences? = null

    private var mMessages: RecyclerView? = null
    private var mManager: LinearLayoutManager? = null
    private var adapter: FirebaseRecyclerAdapter<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mFunctions = FirebaseFunctions.getInstance()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Receipt/Group Name"

        context = this
        mRef = FirebaseUtil.database.reference
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser!!.uid
        CurrUserDisplayName = mAuth.currentUser!!.displayName

        var receiverUID: String? = null

        val extras = intent.extras
        if (extras != null) {
            if (extras.getString("ROUTE") == "CHAT_FRAGMENT"){
                isGroup = extras.getBoolean("IS_GROUP")
                // TODO isGroup is used know how to access user data.
                // TODO If it's a group first go to chat members and grab the uids then pull names from the user's node

                chatId = extras.getString("CHAT_ID")
                attachRecyclerViewAdapter(chatId!!)
                setDraftMessage(chatId!!)
                getChatMembers(chatId!!)

            }
            else{
                isGroup = false // If intent comes from Contacts Fragment it can never be a group
                receiverUID = extras.getString("RECEIVER_UID")

                val existingChat = mRef.child("existing_chats").child(uid!!).child(receiverUID)
                existingChat.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            chatId = dataSnapshot.getValue(String::class.java)
                            attachRecyclerViewAdapter(chatId!!)
                            Log.e("chatId", "chatId $chatId")
                            getChatMembers(chatId!!)
                            setDraftMessage(chatId!!)
                        }
                        else{
                            chatId = null
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
                    }
                })
            }
        }



        sendImageView.setOnClickListener {
            if (chatId != null && chatMembers.isNotEmpty()) {
                addMessageDatabase(messageEditText.text.toString(), uid!!, chatId!!, chatMembers)
                messageEditText.setText("")
            } else {
                createNewChatCloudFunction(messageEditText.text.toString(), chatId, receiverUID)
                messageEditText.setText("")
            }

        }
    }


    private fun attachRecyclerViewAdapter(chatId: String) {
        mManager = LinearLayoutManager(this)
        mMessages = findViewById<View>(R.id.messagesList) as RecyclerView
        val mMessageRef = mRef.child("messages").child(chatId)


        val lastFifty = mMessageRef.limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(lastFifty, MessageModel::class.java)
                .setLifecycleOwner(this)
                .build()

        adapter = object : FirebaseRecyclerAdapter<MessageModel, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.message, parent, false)

                return ChatViewHolder(view)
            }

            override fun onBindViewHolder(chatViewView: ChatViewHolder, position: Int, messageModel: MessageModel) {
                readAllMessages(messageModel.chat_id)
                updateMessageStatus(messageModel.chat_id, getRef(position).key!!)

                chatViewView.setText(messageModel.message)
                chatViewView.setTime(messageModel.timeDateString)
                if (messageModel.sender_uid == uid) {
                    chatViewView.setIsSender(true)
                } else {
                    chatViewView.setIsSender(false)
                }

            }

            override fun getItemViewType(position: Int): Int {
                val chat = getItem(position)
                //                if (chat.getType() != null && chat.getType().equals("board")) {
                //                    // Layout for an item with an image
                //                    return R.layout.item_message_board;
                //                }
                return R.layout.message
                //                }
            }
        }

        // Scroll to bottom on new messages
        adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {

                mManager!!.smoothScrollToPosition(mMessages!!, null, adapter!!.itemCount)
            }
        })

        // orders the list by date

        mManager!!.reverseLayout = false
        mMessages!!.layoutManager = mManager
        mMessages!!.adapter = adapter
    }

    private fun setDraftMessage(chatId: String){
        sharedPrefMessageDraft = context!!.getSharedPreferences("CHATDRAFTMESSAGE", Context.MODE_PRIVATE)
        val draftMessage = sharedPrefMessageDraft!!.getString(chatId, "")
        messageEditText.setText(draftMessage)
    }

    fun getChatMembers(chatId: String){
        val existingChat = mRef.child("chat_members").child(chatId)
        existingChat.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val map = dataSnapshot.value as Map<*, *>
                    chatMembers = map.keys.toMutableList()


                    Log.e("getChatMembers", chatMembers.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
            }
        })
    }

    fun readAllMessages(chatId: String) {
        val mUnreadMessagesRef = mRef.child("messages_unread").child(uid!!).child(chatId)
        mUnreadMessagesRef.removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to remove notification", databaseError.toException())
            }
        }
    }

    fun updateMessageStatus(chatId: String, messageId: String){
        val data = HashMap<String, String>()
        data["chat_id"] = chatId
        data["message_id"] = messageId

        FirebaseFunctions.getInstance()
                .getHttpsCallable("updateMessageStatus")
                .call(data)
                .continueWith { task ->
                    task.result.data as String
                }
    }


    private fun addMessageDatabase(text: String, uid: String, chatId: String, chatMembers: MutableList<*>) {
        Log.e("addMessageDatabase", "Chatid$chatId")
        val mMessageRef = mRef.child("messages").child(chatId)
        val data = HashMap<String, Any?>()
        data["message"] = text
        data["sender_uid"] = uid
        data["timestamp"] = ServerValue.TIMESTAMP

        val statusForChatMembers = HashMap<String, String>()
        chatMembers.forEach{
            statusForChatMembers[it.toString()] = "sent"
        }

        data["status"] = statusForChatMembers



        mMessageRef.push().setValue(data)
    }


    private fun createNewChatCloudFunction(message: String, chatId: String?, recipientUID: String?) {
        // Create the arguments to the callable function.
        Log.e("createNewChatCloud", "Chatid$chatId")
        val data = HashMap<String, Any?>()
        data["message"] = message
        data["chat_id"] = chatId
        data["recipient_uid"] = recipientUID

        mFunctions
                .getHttpsCallable("createNewChat")
                .call(data)
                .continueWith { task ->
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    // Log.e("error message",task.exception?.message)
                    val resultHashMap = task.result.data as HashMap<*, *>
                    val bodyHashMap = resultHashMap["body"] as HashMap<*, *>
                    val chatIDFromCloudFunction = bodyHashMap["chat_id"]

                    if (chatIDFromCloudFunction != null && chatId == null) {
                        val chatID = chatIDFromCloudFunction.toString()
                        addMessageDatabase(message, uid!!, chatID, chatMembers)
                        attachRecyclerViewAdapter(chatID)
                    }
                    val s = task.result.data.toString()
                    Log.e("sendMessage", s + "")
                    task.result.data
                }.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val ffe = e as FirebaseFunctionsException?

                            // Function error code, will be INTERNAL if the failure
                            // was not handled properly in the function call.
                            val code = ffe!!.code

                            // Arbitrary error details passed back from the function,
                            // usually a Map<String, Object>.
                            val details = ffe.details
                        }

                        // [START_EXCLUDE]
                        Log.w(TAG, "acreateNewChat", e)
                        showSnackbar("An error occurred.")
                        return@OnCompleteListener
                        // [END_EXCLUDE]
                    }
                })
    }


    override fun onPause() {
        super.onPause()
        Log.e("onPause",  messageEditText.text.toString())
        if (chatId != null){
            val editor = sharedPrefMessageDraft!!.edit()
            editor.putString(chatId, messageEditText.text.toString())
            editor.apply()
        }

    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        val TAG = "OpenChatActivity"
    }


}
