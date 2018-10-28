package com.chit.chat

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

/**
 * Created by Adrian Bunge on 2018/09/08.
 */

class MainActivity : AppCompatActivity() {

    lateinit var mRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRef = FirebaseUtil.database.reference


        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        setupViewPager(viewPager)
        // Set Tabs inside Toolbar
        val tabs = findViewById<TabLayout>(R.id.tab_layout)
        tabs.setupWithViewPager(viewPager)

        addToken()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(supportFragmentManager)
        adapter.addFragment(ChatFragment(), "Chat")
        adapter.addFragment(ContactsFragment(), "Contacts")
        viewPager.adapter = adapter
        viewPager.currentItem = 1 // Sets the default viewpage
    }



    private fun addToken(): Task<String> {
        // Create the arguments to the callable function.

        val data = HashMap<String, Any?>()
        data["token"] = FirebaseInstanceId.getInstance().token

        return FirebaseFunctions.getInstance()
                .getHttpsCallable("addDeviceToken")
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

    internal class Adapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }


}
