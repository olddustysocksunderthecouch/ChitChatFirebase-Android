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
import android.widget.EditText
import android.widget.ImageView
import com.chit.chat.models.Message
import com.chit.chat.models.UserModel
import com.chit.chat.viewholders.ChatHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.android.synthetic.main.activity_chat.*


class ChatOpenActivity : AppCompatActivity() {

    private var context: Context? = null
    lateinit var mAuth: FirebaseAuth
    lateinit var mFunctions: FirebaseFunctions
    lateinit var mRef: DatabaseReference
    private var mCurrChatPreviewRef: DatabaseReference? = null

    private var sendImageView: ImageView? = null
    private var mMessageEdit: EditText? = null

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

        var chatId: String? = null
        var receiverUID: String? = null


        val extras = intent.extras
        if (extras != null) {
            if (extras.getString("ROUTE") == "CHAT_FRAGMENT"){
                chatId = extras.getString("CHAT_ID")
                attachRecyclerViewAdapter(chatId)
            }
            else{
                receiverUID = extras.getString("RECEIVER_UID")
                val existingChat = mRef.child("existing_chats").child(uid!!).child(receiverUID)
                Log.e("OpenChatActivity", "receiverUID" + receiverUID )
                existingChat.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            chatId = dataSnapshot.getValue(String::class.java)
                            attachRecyclerViewAdapter(chatId!!)
                            Log.e("chatId", "chatId" + chatId )

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


        //supportActionBar!!.title = recieverName

//        sharedPrefMessageDraft = context!!.getSharedPreferences("CHATDRAFTMESSAGE", Context.MODE_PRIVATE)
//        val draftMessage = sharedPrefMessageDraft!!.getString(chatId, "")
//        Log.d("draft message", draftMessage)



        sendImageView = findViewById<View>(R.id.sendButton) as ImageView
        mMessageEdit = findViewById<View>(R.id.messageEdit) as EditText
//        mMessageEdit!!.setText(draftMessage)



//
//        mMessageRef = mRef!!.child("messages").child(chatId!!)
//        mCurrChatPreviewRef = mRef!!.child("chat_preview").child(uid!!).child(chatId!!)
//
//        val mUserRef = mRef!!.child("users").child(uid!!)
//
//        mUserRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                userModel = dataSnapshot.getValue(UserModel::class.java)!!
//                Log.e("User Name: ", userModel!!.display_name)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
//            }
//        })
//
//

        sendImageView!!.setOnClickListener {
            if (chatId != null) {
                addMessageDatabase(mMessageEdit!!.text.toString(), uid!!, chatId!!)
            }

            addMessageCloudFunction(mMessageEdit!!.text.toString(), chatId, receiverUID)
        }
    }


    private fun attachRecyclerViewAdapter(chatId: String) {
        mManager = LinearLayoutManager(this)
        mMessages = findViewById<View>(R.id.messagesList) as RecyclerView
        val mMessageRef = mRef.child("messages").child(chatId)


        val lastFifty = mMessageRef.limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(lastFifty, Message::class.java)
                .setLifecycleOwner(this)
                .build()

        adapter = object : FirebaseRecyclerAdapter<Message, ChatHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.message, parent, false)

                return ChatHolder(view)
            }

            override fun onBindViewHolder(chatView: ChatHolder, position: Int, message: Message) {
                chatView.setText(message.message)
//                //Get the push id for the item which will then be used to track the session throughout
//                val mSessionId = adapter!!.getRef(position)
//                val sessionId = mSessionId.key
//                Log.d("item key", mSessionId.key)
//
//                if (getItemViewType(position) == R.layout.message) {
//                    val status = chat.getStatus()
//                    chatView.setText(chat.getText())
//
//                    if (chat.getUid() != uid && status != "read") {
//                        //  readUpdate(sessionId)
//                    }

//                    val mydate = Calendar.getInstance()
//                    mydate.timeInMillis = chat.timestampCreatedLong
//                    val hourOfDay = mydate.get(Calendar.HOUR_OF_DAY)
//                    val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
//                    val minute = mydate.get(Calendar.MINUTE)
//                    val minuteString = if (minute < 10) "0$minute" else "" + minute
//
//                    val dateString = hourString + ":" + minuteString + "  (" + mydate.get(Calendar.DAY_OF_MONTH) + "." + (mydate.get(Calendar.MONTH) + 1) + ")"
//                    chatView.setTime(dateString)
//
//
                if (message.sender_uid == uid) {
                    chatView.setIsSender(true)
                } else {
                    chatView.setIsSender(false)
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


//    fun readUpdate(messageId: String?) {
//        val mMessageRef = mRef!!.child("messages").child(chatId!!).child(messageId!!).child("status")
//        mMessageRef.setValue("read") { databaseError, databaseReference -> }
//        val mNotification = mRef!!.child("notification_messages").child(uid!!).child(receiverUID!!).child(messageId)
//        mNotification.removeValue { databaseError, databaseReference ->
//            if (databaseError != null) {
//                Log.e(TAG, "Failed to remove notification", databaseError.toException())
//            }
//        }
//
//        val mChatPreviewCounterRead = mRef!!.child("chat_preview").child(uid!!).child(chatId!!)
//        val CounterReadUpdate = HashMap<String, Any>()
//        CounterReadUpdate["count"] = 0
//        CounterReadUpdate["status"] = "read"
//
//        mChatPreviewCounterRead.updateChildren(CounterReadUpdate) { databaseError, databaseReference ->
//            if (databaseError != null) {
//                Log.e(TAG, "Failed to update CounterReadUpdate", databaseError.toException())
//            }
//        }
//
//    }

    private fun addMessageDatabase(text: String, uid: String, chatId: String) {
        Log.e("addMessageDatabase", "Chatid" +chatId)
        val mMessageRef = mRef.child("messages").child(chatId)
        val data = HashMap<String, Any?>()
        data["message"] = text
        data["sender_uid"] = uid
        mMessageRef.push().setValue(data)

    }


    private fun addMessageCloudFunction(message: String, chatId: String?, recipientUID: String?) {
        // Create the arguments to the callable function.
        Log.e("addMessageCloudFunction", "Chatid" +chatId)
        val data = HashMap<String, Any?>()
        data["message"] = message
        data["chat_id"] = chatId
        data["recipient_uid"] = recipientUID

        mFunctions
                .getHttpsCallable("sendMessage")
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
                        addMessageDatabase(message, uid!!, chatID)
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
                        Log.w(TAG, "addNumbers:onFailure", e)
                        showSnackbar("An error occurred.")
                        return@OnCompleteListener
                        // [END_EXCLUDE]
                    }
                })
    }


    override fun onPause() {
        super.onPause()
//
//        val editor = sharedPrefMessageDraft!!.edit()
//        editor.putString(chatId, mMessageEdit!!.text.toString())
//        editor.apply()

    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        val TAG = "OpenChatActivity"
    }


}
