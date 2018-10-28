package com.chit.chat

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ServerValue
import com.google.firebase.functions.FirebaseFunctions
import java.util.HashMap

/**
 * Created by Adrian Bunge on 2018/09/08.
 */

class AuthenticationActivity : BaseActivity() {

    private var mEmailField: EditText? = null
    private var mPasswordField: EditText? = null

    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        // Views
        mEmailField = findViewById(R.id.fieldEmail)
        mPasswordField = findViewById(R.id.fieldPassword)

        // Buttons
        findViewById<View>(R.id.emailSignInButton).setOnClickListener { signIn(mEmailField!!.text.toString(), mPasswordField!!.text.toString()) }
        findViewById<View>(R.id.emailCreateAccountButton).setOnClickListener { createAccount(mEmailField!!.text.toString(), mPasswordField!!.text.toString()) }

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // [START create_user_with_email]
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = mAuth!!.currentUser
                        createUserAccount(user!!.uid)
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@AuthenticationActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END create_user_with_email]
    }

        private fun createUserAccount(uid: String) {
            Log.e("Create New Account", "Cloud Function Called")
            // Create the arguments to the callable function.
            val data = HashMap<String, Any>()
            data["display_name"] = "Wisani"
            data["uid"] = uid
            data["profile_pic"] = "https://firebasestorage.googleapis.com/v0/b/chitchat-2faa0.appspot.com/o/1_sgte14nnEGB1cwlDbjbBrw.png?alt=media&token=1d39cad9-1030-45ab-b5a6-c1c4f8e1c8c5"

            FirebaseFunctions.getInstance()
                    .getHttpsCallable("createAccount")
                    .call(data)
                    .continueWith { task ->
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        // Log.e("error message",task.exception?.message)
                        task.result.data as String
                    }
        }


    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // [START sign_in_with_email]
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth!!.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@AuthenticationActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        // [END sign_in_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = mEmailField!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            mEmailField!!.error = "Required."
            valid = false
        } else {
            mEmailField!!.error = null
        }

        val password = mPasswordField!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            mPasswordField!!.error = "Required."
            valid = false
        } else {
            mPasswordField!!.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            val startMainActivity = Intent(this@AuthenticationActivity, MainActivity::class.java)
            this@AuthenticationActivity.startActivity(startMainActivity)
        }
    }

    companion object {

        private val TAG = "AuthenticationActivity"
    }
}