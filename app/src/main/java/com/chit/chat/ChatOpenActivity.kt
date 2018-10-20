package com.voting.group.dev.googel.chitchat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.voting.group.dev.googel.chitchat.models.MessageModel
import com.voting.group.dev.googel.chitchat.models.UserModel
import com.voting.group.dev.googel.chitchat.viewholders.ChatHolder
import java.util.*
import com.google.firebase.functions.FirebaseFunctionsException
import com.voting.group.dev.googel.picupchatapp.R


class ChatOpenActivity : AppCompatActivity() {

    private var context: Context? = null
    lateinit var mAuth: FirebaseAuth
    lateinit var mFunctions: FirebaseFunctions
    private var mRef: DatabaseReference? = null
    private var mCurrChatPreviewRef: DatabaseReference? = null

    private var mMessageRef: DatabaseReference? = null
    private var sendImageView: ImageView? = null
    private var mMessageEdit: EditText? = null

    private var chatID: String? = null
    private var uid: String? = null
    private var CurrUserDisplayName: String? = null

    lateinit var userModel: UserModel

    private var sharedPrefMessageDraft: SharedPreferences? = null

    private var mMessages: RecyclerView? = null
    private var mManager: LinearLayoutManager? = null
    private var adapter: FirebaseRecyclerAdapter<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mFunctions = FirebaseFunctions.getInstance()
        //
        //        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Back button

        context = this@ChatOpenActivity

//        val sharedPref = context!!.getSharedPreferences("role_ref", Context.MODE_PRIVATE)
//        role = sharedPref.getString("role_ref", "")
//        grade = sharedPref.getString("grade_ref", "")

        val extras = intent.extras
        if (extras != null) {
            chatID = extras.getString("CHAT_ID")
        } else {
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            context!!.startActivity(mainActivityIntent)
        }
        //supportActionBar!!.title = recieverName

        sharedPrefMessageDraft = context!!.getSharedPreferences("CHATDRAFTMESSAGE", Context.MODE_PRIVATE)
        val draftMessage = sharedPrefMessageDraft!!.getString(chatID, "")
        Log.d("draft message", draftMessage)

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser!!.uid
        CurrUserDisplayName = mAuth.currentUser!!.displayName

        sendImageView = findViewById<View>(R.id.sendButton) as ImageView
        mMessageEdit = findViewById<View>(R.id.messageEdit) as EditText
        mMessageEdit!!.setText(draftMessage)

        mRef = FirebaseUtil.database.reference
        mMessageRef = mRef!!.child("messages").child(chatID!!)
        mCurrChatPreviewRef = mRef!!.child("chat_preview").child(uid!!).child(chatID!!)


        val mUserRef = mRef!!.child("users").child(uid!!)

        mUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel::class.java)!!
                Log.e("User Name: ", userModel!!.name)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohhhhh shiiiitttt: ", "Failed to read value.", error.toException())
            }
        })


        sendImageView!!.setOnClickListener {
            Log.e("chat_id", chatID!!)
            addMessage(mMessageEdit!!.text.toString(), chatID!!, userModel.name, uid!!).addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val ffe = e as FirebaseFunctionsException?
                        val code = ffe!!.code
                        val details = ffe.details
                        Log.w(TAG, "addMessage:onFailure" + ffe.details + "..............." + e.localizedMessage)
                    }


                    // [START_EXCLUDE]
                    Log.w(TAG, "addMessage:onFailure" + e!!.cause + "..............." + e.localizedMessage)
                    //showSnackbar("An error occurred.")
                    return@OnCompleteListener
                    // [END_EXCLUDE]
                }

                // [START_EXCLUDE]
                val result = task.result
                Log.w(TAG, "addMessage:onFailure result" + result)
                // [END_EXCLUDE]
            })
        }
//            val TimeStampMap = HashMap<String, Any>()
//            TimeStampMap["timestamp"] = ServerValue.TIMESTAMP
//            Log.d("timestamp:", TimeStampMap.toString())
//
//            val pushRef = mMessageRef!!.push()
//            val mMessagePushID = pushRef.key
//
//            mMessageRef = mRef!!.child("messages").child(chatID!!)
//
//            val chat = MessageModel(CurrUserDisplayName, uid, mMessageEdit!!.text.toString(), "message", "", "sent")
//            pushRef.setValue(chat) { databaseError, reference ->
//                if (databaseError != null) {
//                    Log.e(TAG, "Failed to write message", databaseError.toException())
//                }
//            }
//            mCurrChatPreviewRef!!.child("message").setValue("Me: " + mMessageEdit!!.text.toString())
//            //                mRecieverChatPreviewRef.child("message").setValue(mMessageEdit.getText().toString());
//            //                mRecieverChatPreviewRef.child("status").setValue("unread");
//
//
//            var messageText = mMessageEdit!!.text.toString()
//            if (messageText.length > 141) {
//                messageText = messageText.substring(0, 140) + "..."
//            }
//            //
//
//            mMessageEdit!!.setText("")
//            mCurrChatPreviewRef!!.updateChildren(TimeStampMap) { databaseError, databaseReference ->
//                if (databaseError != null) {
//                    Log.e(TAG, "Failed to update profile", databaseError.toException())
//                }
//            }
//            //                mRecieverChatPreviewRef.updateChildren(TimeStampMap, new DatabaseReference.CompletionListener() {
//            //                    @Override
//            //                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//            //                        if (databaseError != null) {
//            //                            Log.e(TAG, "Failed to update profile", databaseError.toException());
//            //                        }
//            //                    }
//            //                });
//            mCurrChatPreviewRef!!.child("timestampmessage").updateChildren(TimeStampMap) { databaseError, databaseReference ->
//                if (databaseError != null) {
//                    Log.e(TAG, "Failed to update profile", databaseError.toException())
//                }
//            }
//            //                mRecieverChatPreviewRef.child("timestampmessage").updateChildren(TimeStampMap, new DatabaseReference.CompletionListener() {
//            //                    @Override
//            //                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//            //                        if (databaseError != null) {
//            //                            Log.e(TAG, "Failed to update profile", databaseError.toException());
//            //                        }
//            //                    }
//            //                });
//        }

    }


    private fun attachRecyclerViewAdapter() {
        mManager = LinearLayoutManager(this)
        mMessages = findViewById<View>(R.id.messagesList) as RecyclerView
        val lastFifty = mMessageRef!!.limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(lastFifty, MessageModel::class.java)
                .setLifecycleOwner(this)
                .build()

        adapter = object : FirebaseRecyclerAdapter<MessageModel, ChatHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.message, parent, false)

                return ChatHolder(view)
            }

            override fun onBindViewHolder(chatView: ChatHolder, position: Int, chat: MessageModel) {
                // Bind the Chat object to the ChatHolder

                //                MessageModel(String name, String uid, String message, String type,
                //                        String url, HashMap<String, Object> token,  HashMap<String, Object>  timestamp)

                //Get the push id for the item which will then be used to track the session throughout
                val mSessionId = adapter!!.getRef(position)
                val sessionId = mSessionId.key
                Log.d("item key", mSessionId.key)

                if (getItemViewType(position) == R.layout.message) {
                    val status = chat.getStatus()
                    chatView.setText(chat.getText())

                    if (chat.getUid() != uid && status != "read") {
                      //  readUpdate(sessionId)
                    }

                    val mydate = Calendar.getInstance()
                    mydate.timeInMillis = chat.timestampCreatedLong
                    val hourOfDay = mydate.get(Calendar.HOUR_OF_DAY)
                    val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
                    val minute = mydate.get(Calendar.MINUTE)
                    val minuteString = if (minute < 10) "0$minute" else "" + minute

                    val dateString = hourString + ":" + minuteString + "  (" + mydate.get(Calendar.DAY_OF_MONTH) + "." + (mydate.get(Calendar.MONTH) + 1) + ")"
                    chatView.setTime(dateString)


                    if (chat.getUid() == uid) {
                        if (status == "sent") {
                            chatView.setSentRead(R.mipmap.sent_icon)
                            chatView.setIsSender(true)

                        } else {
                            chatView.setSentRead(R.mipmap.read_icon)
                            chatView.setIsSender(true)
                        }
                    } else {
                        chatView.setIsSender(false)
                    }
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
//        val mMessageRef = mRef!!.child("messages").child(chatID!!).child(messageId!!).child("status")
//        mMessageRef.setValue("read") { databaseError, databaseReference -> }
//        val mNotification = mRef!!.child("notification_messages").child(uid!!).child(receiverUID!!).child(messageId)
//        mNotification.removeValue { databaseError, databaseReference ->
//            if (databaseError != null) {
//                Log.e(TAG, "Failed to remove notification", databaseError.toException())
//            }
//        }
//
//        val mChatPreviewCounterRead = mRef!!.child("chat_preview").child(uid!!).child(chatID!!)
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


    private fun addMessage(text: String, chatId: String, userName: String, uid: String): Task<String> {
        // Create the arguments to the callable function.
        val data = HashMap<String, Any>();
        data["message"] = text
        data["sender_name"] = userName
        data["sender_uid"] = uid
        data["chat_id"] = chatId
        data["push"] = true
        data["timestamp"] = ServerValue.TIMESTAMP

        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith { task ->
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                   // Log.e("error message",task.exception?.message)
                    task.result.data as String


                }
//                .addOnCompleteListener{ task ->
//                        if (!task.isSuccessful()) {
//                            val e = task.exception
//                            if (e is FirebaseFunctionsException) {
//                                Log.e("FirebaseFunException", "true")
//                                val code = e.code
//                                val details = e.details
//                                Log.e("error message", details.toString())
//                            }
//                        }
//
//                        // ...
//                    }

//                .addOnFailureListener { task -> Log.e("Message Error: ", task.)}

    }

    override fun onStart() {
        super.onStart()
        attachRecyclerViewAdapter()
    }

    override fun onPause() {
        super.onPause()

        val editor = sharedPrefMessageDraft!!.edit()
        editor.putString(chatID, mMessageEdit!!.text.toString())
        editor.apply()

    }

    companion object {
        val TAG = "OpenChatActivity"
    }


}
