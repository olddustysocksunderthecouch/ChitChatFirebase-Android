package com.chit.chat.fileupload

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.chit.chat.R
import com.chit.chat.utils.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_manange_images.*
import kotlinx.android.synthetic.main.manangeimages_toolbar.*
import java.io.File
import java.lang.System.currentTimeMillis
import java.util.*


/**
 * Activity to upload and download photos from Firebase Storage.
 */
class ManagePhotosActivity : BaseActivity(), View.OnClickListener {

    private var mBroadcastReceiver: BroadcastReceiver? = null
    private var mProgressDialog: ProgressDialog? = null

    private lateinit var mRef: DatabaseReference
    private var role: String = ""
    private lateinit var mProfileRef: DatabaseReference
    private lateinit var mPhotosRef: DatabaseReference

    private var mDownloadUrl: Uri? = null
    private var mFileUri: Uri? = null
    private var whichPic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manangeimages_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPref = this.getSharedPreferences("role_ref", Context.MODE_PRIVATE)
        role = sharedPref.getString("role_ref", "") ?: ""

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        mRef = FirebaseUtil.database.reference
        mProfileRef = mRef.child(role).child(uid)
        mPhotosRef = mRef.child("photos_$role").child(uid)

        // Click listeners
        profile_uploadphotoimageview.setOnClickListener(this)
        cover_uploadphotoimageview.setOnClickListener(this)
        tl_uploadphotoimageview.setOnClickListener(this)
        tr_uploadphotoimageview.setOnClickListener(this)
        bl_uploadphotoimageview.setOnClickListener(this)
        br_uploadphotoimageview.setOnClickListener(this)

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI)
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL)
        }
        onNewIntent(intent)

        // Local broadcast receiver
        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive:$intent")
                hideProgressDialog()

                when (intent.action) {
                    MyUploadService.UPLOAD_COMPLETED, MyUploadService.UPLOAD_ERROR -> onUploadResultIntent(intent)
                }
            }
        }

        mPhotosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
//                    val photos = dataSnapshot.getValue(PhotosModel::class.java)
//                    loadImages(photos!!.phototl, photos.phototr, photos.photobl, photos.photobr)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        mProfileRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
//                    val user = dataSnapshot.getValue(UserInfoModel::class.java)
//                    loadProfileCoverPics(user!!.getprofilePic(), user.coverPic)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadProfileCoverPics(ProfilePicUrl: String, CoverPicUrl: String) {
        val optionsCentre = RequestOptions()
                .placeholder(R.drawable.profilepic_placeholder)
                .error(R.drawable.profilepic_placeholder)
                .dontAnimate().circleCrop()
                .priority(Priority.HIGH)

        Glide.with(this).load(ProfilePicUrl).apply(optionsCentre).into(profilepic_imageview)

        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.profilepic_placeholder)
                .error(R.drawable.profilepic_placeholder)
                .priority(Priority.HIGH)

        Glide.with(this).load(CoverPicUrl).apply(options).into(coverpic_imageview)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        updateUI()

        // Register receiver for uploads and downloads
        val manager = LocalBroadcastManager.getInstance(this)
        // manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver!!, MyUploadService.intentFilter)
    }

    public override fun onStop() {
        super.onStop()
        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver!!)
    }

    public override fun onSaveInstanceState(out: Bundle) {
        out.putParcelable(KEY_FILE_URI, mFileUri)
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl)
        super.onSaveInstanceState(out)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            BaseActivity.REQUEST_STORAGE_READ_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult:$requestCode:$resultCode:$data")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    startCropActivity(data.data)
                } else {
                    Toast.makeText(this@ManagePhotosActivity, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data!!)
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data!!)
        }
    }

    private fun loadImages(pictl: String, pictr: String, picbl: String, picbr: String) {

        val options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.profilepic_placeholder)
                .error(R.drawable.profilepic_placeholder)
                .dontAnimate()
                .priority(Priority.HIGH)

        Glide.with(this).load(pictl).apply(options).into(tl_additionalpic)
        Glide.with(this).load(pictr).apply(options).into(tr_additionalpic)
        Glide.with(this).load(picbl).apply(options).into(bl_additionalpic)
        Glide.with(this).load(picbr).apply(options).into(br_additionalpic)
    }

    private fun startCropActivity(fileUri: Uri?) {
        var destinationFileName = currentTimeMillis().toString()
        destinationFileName += ".jpg"

        var uCrop = UCrop.of(fileUri!!, Uri.fromFile(File(cacheDir, destinationFileName)))
        uCrop = configUCrop(uCrop)
        uCrop.start(this@ManagePhotosActivity)
    }

    private fun configUCrop(uCrop: UCrop): UCrop {
        uCrop.withAspectRatio(1f, 1f)
        uCrop.withMaxResultSize(1080, 1080)

        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.setCompressionQuality(70)
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        return uCrop.withOptions(options)
    }


    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        mFileUri = resultUri
        if (resultUri != null) {
            updateUI()
            mDownloadUrl = null
            // Start MyUploadService to upload the file, so that the file is uploaded
            // even if this Activity is killed or put in the background
            startService(Intent(this, MyUploadService::class.java)
                    .putExtra(MyUploadService.EXTRA_FILE_URI, resultUri)
                    .setAction(MyUploadService.ACTION_UPLOAD))

            // Show loading spinner
            showProgressDialog(getString(R.string.progress_uploading))
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError)
            Toast.makeText(this@ManagePhotosActivity, cropError.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@ManagePhotosActivity, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        Log.d(TAG, "launchCamera")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    BaseActivity.REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_SELECT_PICTURE)
        }
    }

    private fun onUploadResultIntent(intent: Intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL)
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI)

        updateUI()
    }

    private fun updateUI() {
        if (mDownloadUrl != null) {
            Log.d("which pic ", "number $whichPic")
            when (whichPic) {
                "1" -> addUrlToProfileDatabase("profilePic", mDownloadUrl)
                "2" -> addUrlToProfileDatabase("coverPic", mDownloadUrl)
                "3" -> addUrlToPicturesDatabase("phototl", mDownloadUrl)
                "4" -> addUrlToPicturesDatabase("phototr", mDownloadUrl)
                "5" -> addUrlToPicturesDatabase("photobl", mDownloadUrl)
                "6" -> addUrlToPicturesDatabase("photobr", mDownloadUrl)
            }
            whichPic = ""
            mDownloadUrl = null

        }
    }

    private fun addUrlToProfileDatabase(path: String, downloadUrl: Uri?) {
        if (downloadUrl != null) {
            val profileUpdates = HashMap<String, Any>()
            profileUpdates[path] = downloadUrl.toString()

            mProfileRef.updateChildren(profileUpdates) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to update profile", databaseError.toException())
                    Toast.makeText(this, "Error :( Your profile was not updated", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "$path was updated!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addUrlToPicturesDatabase(path: String, downloadUrl: Uri?) {
        val profileUpdates = HashMap<String, Any>()
        profileUpdates[path] = downloadUrl.toString()

        mPhotosRef.updateChildren(profileUpdates) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to update profile", databaseError.toException())
                Toast.makeText(this, "Error :( Your profile was not updated", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "$path was updated!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showProgressDialog(caption: String) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.setMessage(caption)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    override fun onClick(v: View) {
        val i = v.id
        whichPic = when (i) {
            R.id.profile_uploadphotoimageview -> "1"
            R.id.cover_uploadphotoimageview -> "2"
            R.id.tl_uploadphotoimageview -> "3"
            R.id.tr_uploadphotoimageview -> "4"
            R.id.bl_uploadphotoimageview -> "5"
            R.id.br_uploadphotoimageview -> "6"
            else -> ""
        }
        launchCamera()
    }

    companion object {
        private const val TAG = "Storage#MainActivity"

        private val KEY_FILE_URI = "key_file_uri"
        private val KEY_DOWNLOAD_URL = "key_download_url"
        private val REQUEST_SELECT_PICTURE = 0x01
    }
}
