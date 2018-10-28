///*
// * Copyright Google Inc. All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.voting.group.dev.googel.chitchat
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.support.design.widget.Snackbar
//import android.support.v7.app.AppCompatActivity
//import android.text.TextUtils
//import android.util.Log
//import android.view.View
//import android.view.inputmethod.InputMethodManager
//import android.widget.Button
//import android.widget.EditText
//
//import com.firebase.ui.auth.AuthUI
//import com.firebase.ui.auth.IdpResponse
//import com.google.android.gms.tasks.Continuation
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.android.gms.tasks.Task
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.functions.FirebaseFunctions
//import com.google.firebase.functions.FirebaseFunctionsException
//import com.google.firebase.functions.HttpsCallableResult
//import com.google.samples.quickstart.functions.R
//import com.voting.group.dev.googel.picupchatapp.R
//
//import java.util.Collections
//import java.util.HashMap
//
///**
// * This activity demonstrates the Android SDK for Callable Functions.
// *
// * For more information, see the documentation for Cloud Functions for Firebase:
// * https://firebase.google.com/docs/functions/
// */
//class sample : AppCompatActivity(), View.OnClickListener {
//
//    // Add number views
//    private var mFirstNumberField: EditText? = null
//    private var mSecondNumberField: EditText? = null
//    private var mAddResultField: EditText? = null
//    private var mCalculateButton: Button? = null
//
//    // Add message views
//    private var mMessageInputField: EditText? = null
//    private var mMessageOutputField: EditText? = null
//    private var mAddMessageButton: Button? = null
//    private var mSignInButton: Button? = null
//
//    // [START define_functions_instance]
//    private var mFunctions: FirebaseFunctions? = null
//    // [END define_functions_instance]
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        mFirstNumberField = findViewById(R.id.fieldFirstNumber)
//        mSecondNumberField = findViewById(R.id.fieldSecondNumber)
//        mAddResultField = findViewById(R.id.fieldAddResult)
//        mCalculateButton = findViewById(R.id.buttonCalculate)
//        mCalculateButton!!.setOnClickListener(this)
//
//        mMessageInputField = findViewById(R.id.fieldMessageInput)
//        mMessageOutputField = findViewById(R.id.fieldMessageOutput)
//        mAddMessageButton = findViewById(R.id.buttonAddMessage)
//        mSignInButton = findViewById(R.id.buttonSignIn)
//        mAddMessageButton!!.setOnClickListener(this)
//        mSignInButton!!.setOnClickListener(this)
//
//        // [START initialize_functions_instance]
//        mFunctions = FirebaseFunctions.getInstance()
//        // [END initialize_functions_instance]
//    }
//
//    // [START function_add_numbers]
//    private fun addNumbers(a: Int, b: Int): Task<Int> {
//        // Create the arguments to the callable function, which are two integers
//        val data = HashMap<String, Any>()
//        data["firstNumber"] = a
//        data["secondNumber"] = b
//
//        // Call the function and extract the operation from the result
//        return mFunctions!!
//                .getHttpsCallable("addNumbers")
//                .call(data)
//                .continueWith { task ->
//                    // This continuation runs on either success or failure, but if the task
//                    // has failed then getResult() will throw an Exception which will be
//                    // propagated down.
//                    val result = task.result.data as Map<String, Any>
//                    result["operationResult"] as Int
//                }
//    }
//    // [END function_add_numbers]
//
//    // [START function_add_message]
//    private fun addMessage(text: String): Task<String> {
//        // Create the arguments to the callable function.
//        val data = HashMap<String, Any>()
//        data["text"] = text
//        data["push"] = true
//
//        return mFunctions!!
//                .getHttpsCallable("addMessage")
//                .call(data)
//                .continueWith { task ->
//                    // This continuation runs on either success or failure, but if the task
//                    // has failed then getResult() will throw an Exception which will be
//                    // propagated down.
//                    task.result.data as String
//                }
//    }
//    // [END function_add_message]
//
//    private fun onCalculateClicked() {
//        val firstNumber: Int
//        val secondNumber: Int
//
//        hideKeyboard()
//
//        try {
//            firstNumber = Integer.parseInt(mFirstNumberField!!.text.toString())
//            secondNumber = Integer.parseInt(mSecondNumberField!!.text.toString())
//        } catch (e: NumberFormatException) {
//            showSnackbar("Please enter two numbers.")
//            return
//        }
//
//        // [START call_add_numbers]
//        addNumbers(firstNumber, secondNumber)
//                .addOnCompleteListener(OnCompleteListener { task ->
//                    if (!task.isSuccessful) {
//                        val e = task.exception
//                        if (e is FirebaseFunctionsException) {
//                            val ffe = e as FirebaseFunctionsException?
//
//                            // Function error code, will be INTERNAL if the failure
//                            // was not handled properly in the function call.
//                            val code = ffe!!.code
//
//                            // Arbitrary error details passed back from the function,
//                            // usually a Map<String, Object>.
//                            val details = ffe.details
//                        }
//
//                        // [START_EXCLUDE]
//                        Log.w(TAG, "addNumbers:onFailure", e)
//                        showSnackbar("An error occurred.")
//                        return@OnCompleteListener
//                        // [END_EXCLUDE]
//                    }
//
//                    // [START_EXCLUDE]
//                    val result = task.result
//                    mAddResultField!!.setText(result.toString())
//                    // [END_EXCLUDE]
//                })
//        // [END call_add_numbers]
//    }
//
//    private fun onAddMessageClicked() {
//        val inputMessage = mMessageInputField!!.text.toString()
//
//        if (TextUtils.isEmpty(inputMessage)) {
//            showSnackbar("Please enter a message.")
//            return
//        }
//
//        // [START call_add_message]
//        addMessage(inputMessage)
//                .addOnCompleteListener(OnCompleteListener { task ->
//                    if (!task.isSuccessful) {
//                        val e = task.exception
//                        if (e is FirebaseFunctionsException) {
//                            val ffe = e as FirebaseFunctionsException?
//                            val code = ffe!!.code
//                            val details = ffe.details
//                        }
//
//                        // [START_EXCLUDE]
//                        Log.w(TAG, "addMessage:onFailure", e)
//                        showSnackbar("An error occurred.")
//                        return@OnCompleteListener
//                        // [END_EXCLUDE]
//                    }
//
//                    // [START_EXCLUDE]
//                    val result = task.result
//                    mMessageOutputField!!.setText(result)
//                    // [END_EXCLUDE]
//                })
//        // [END call_add_message]
//    }
//
//    private fun onSignInClicked() {
//        if (FirebaseAuth.getInstance().currentUser != null) {
//            showSnackbar("Signed in.")
//            return
//        }
//
//        signIn()
//    }
//
//    private fun showSnackbar(message: String) {
//        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
//    }
//
//    private fun signIn() {
//        val signInIntent = AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
//                .setIsSmartLockEnabled(false)
//                .build()
//
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//    private fun hideKeyboard() {
//        val view = this.currentFocus
//        if (view != null) {
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(view.windowToken, 0)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            if (resultCode == Activity.RESULT_OK) {
//                showSnackbar("Signed in.")
//            } else {
//                showSnackbar("Error signing in.")
//
//                val response = IdpResponse.fromResultIntent(data)
//                Log.w(TAG, "signIn", response.getError())
//            }
//        }
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.buttonCalculate -> onCalculateClicked()
//            R.id.buttonAddMessage -> onAddMessageClicked()
//            R.id.buttonSignIn -> onSignInClicked()
//        }
//    }
//
//    companion object {
//
//        private val TAG = "MainActivity"
//
//        private val RC_SIGN_IN = 9001
//    }
//}