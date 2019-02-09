package com.chit.chat.fileupload

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.chit.chat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * Service to handle uploading files to Firebase Storage.
 */
class MyUploadService : MyBaseTaskService() {

    private var uid: String = ""
    // [START declare_ref]
    private lateinit var mStorageRef: StorageReference

    override fun onCreate() {
        super.onCreate()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        mStorageRef = FirebaseStorage.getInstance().reference
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if (ACTION_UPLOAD == intent.action) {
            val fileUri = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)
            uploadFromUri(fileUri)
        }

        return Service.START_REDELIVER_INTENT
    }

    // [START upload_from_uri]
    private fun uploadFromUri(fileUri: Uri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString())

        // [START_EXCLUDE]
        taskStarted()
        showProgressNotification(getString(R.string.progress_uploading), 0, 0)
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        val photoRef = mStorageRef.child(uid).child(fileUri.lastPathSegment!!)
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.path)
        photoRef.putFile(fileUri).addOnProgressListener { taskSnapshot ->
            showProgressNotification(getString(R.string.progress_uploading),
                    taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount)
        }.addOnFailureListener { exception ->
            // Upload failed
            Log.w(TAG, "uploadFromUri:onFailure", exception)

            // [START_EXCLUDE]
            broadcastUploadFinished(null, fileUri)
            showUploadFinishedNotification(null, fileUri)
            taskCompleted()
            // [END_EXCLUDE]
        }.addOnCompleteListener {
            photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.d(TAG, "onSuccess: uri= " + downloadUri.toString())

                broadcastUploadFinished(downloadUri, fileUri)
                showUploadFinishedNotification(downloadUri, fileUri)
                taskCompleted()
            }
        }

    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUrl != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(broadcast)
    }

    /**
     * Show a notification for a finished upload.
     */
    private fun showUploadFinishedNotification(downloadUrl: Uri?, fileUri: Uri?) {
        // Hide the progress notification
        dismissProgressNotification()

        // Make Intent to MainActivity
        val intent = Intent(this, ManagePhotosActivity::class.java)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        val caption = if (success) getString(R.string.upload_success) else getString(R.string.upload_failure)
        showFinishedNotification(caption, intent, success)
    }

    companion object {

        private val TAG = "MyUploadService"

        /** Intent Actions  */
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        /** Intent Extras  */
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }

}
