package com.chit.chat.fileupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.chit.chat.R;
import com.chit.chat.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


/**
 * Activity to upload and download photos from Firebase Storage.
 * <p>
 * See {@link MyUploadService} for upload example.
 */
public class ManagePhotosActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Storage#MainActivity";

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private static final int REQUEST_SELECT_PICTURE = 0x01;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    final private String RoleRef = "role";
    private Context context;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String role;
    private DatabaseReference mRoleRef;
    private DatabaseReference mProfileRef;
    private DatabaseReference mPhotosRef;
    private String uid;

    private RequestManager mRequestManager;

    private ImageView ProfilePic;


    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String whichPic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manangeimages_toolbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getBaseContext();
        mRequestManager = Glide.with(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        uid = mAuth.getCurrentUser().getUid();
        mRef = FirebaseUtil.INSTANCE.getDatabase().getReference();
        String role = "";
        mProfileRef = mRef.child(role).child(user.getUid());
        mPhotosRef = mRef.child("photos_" + role).child(user.getUid());

        ProfilePic = (ImageView) findViewById(R.id.profilepic_imageview);

        // Click listeners
        findViewById(R.id.profile_uploadphotoimageview).setOnClickListener(this);
        findViewById(R.id.cover_uploadphotoimageview).setOnClickListener(this);
        findViewById(R.id.tl_uploadphotoimageview).setOnClickListener(this);
        findViewById(R.id.tr_uploadphotoimageview).setOnClickListener(this);
        findViewById(R.id.bl_uploadphotoimageview).setOnClickListener(this);
        findViewById(R.id.br_uploadphotoimageview).setOnClickListener(this);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {

                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };


    }

    private void loadProfileCoverPics(String ProfilePicUrl, String CoverPicUrl) {

        RequestOptions optionsCentre = new RequestOptions()
                .placeholder(R.drawable.profilepic_placeholder)
                .error(R.drawable.profilepic_placeholder)
                .dontAnimate().circleCrop()
                .priority(Priority.HIGH);

        mRequestManager
                .load(ProfilePicUrl)
                .apply(optionsCentre)
                .into(ProfilePic);

    }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        // manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    startCropActivity(data.getData());
                } else {
                    Toast.makeText(ManagePhotosActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    private void startCropActivity(Uri fileUri) {
        Log.d(TAG, " are we here yet:" + " image crop");

        String destinationFileName = String.valueOf(currentTimeMillis());
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);

        uCrop.start(ManagePhotosActivity.this);
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop basisConfig(@NonNull UCrop uCrop) {
        uCrop = uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(1080, 1080);
        return uCrop;
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link com.yalantis.ucrop.UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(70);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        return uCrop.withOptions(options);
    }


    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        mFileUri = resultUri;
        if (resultUri != null) {
            updateUI(mAuth.getCurrentUser());
            mDownloadUrl = null;
            // Start MyUploadService to upload the file, so that the file is uploaded
            // even if this Activity is killed or put in the background
            startService(new Intent(this, MyUploadService.class)
                    .putExtra(MyUploadService.EXTRA_FILE_URI, resultUri)
                    .setAction(MyUploadService.ACTION_UPLOAD));

            // Show loading spinner
            showProgressDialog(getString(R.string.progress_uploading));
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(ManagePhotosActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ManagePhotosActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }


    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_SELECT_PICTURE);
        }
    }


    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);

        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {

        // Download URL and Download button
        if (mDownloadUrl != null) {

            //findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
            Log.d("which pic ", "number " + whichPic);

            if (whichPic.equalsIgnoreCase("1")) {
                addUrlToProfileDatabase("profilePic", mDownloadUrl);
                Log.d("profilePic ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            } else if (whichPic.equalsIgnoreCase("2")) {
                addUrlToProfileDatabase("coverPic", mDownloadUrl);
                Log.d("coverphoto ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            } else if (whichPic.equalsIgnoreCase("3")) {
                addUrlToPicturesDatabase("phototl", mDownloadUrl);
                Log.d("phototl ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            } else if (whichPic.equalsIgnoreCase("4")) {
                addUrlToPicturesDatabase("phototr", mDownloadUrl);
                Log.d("phototr ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            } else if (whichPic.equalsIgnoreCase("5")) {
                addUrlToPicturesDatabase("photobl", mDownloadUrl);
                Log.d("photobl ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            } else if (whichPic.equalsIgnoreCase("6")) {
                addUrlToPicturesDatabase("photobr", mDownloadUrl);
                Log.d("photobr ", "covering");
                whichPic = "";
                mDownloadUrl = null;
            }


           /* switch (whichPic) {
                case whichPic.equalsIgnoreCase("1"):
                    addUrlToProfileDatabase("profilePic", mDownloadUrl);
                    Log.d("profilePic ", "covering");
                case 2:
                    addUrlToProfileDatabase("coverPic", mDownloadUrl);
                    Log.d("coverphoto ", "covering");
                case 3:
                    addUrlToPicturesDatabase("phototr", mDownloadUrl);
                case 4:
                    addUrlToPicturesDatabase("phototl", mDownloadUrl);
                case 5:
                    addUrlToPicturesDatabase("photobr", mDownloadUrl);
                case 6:
                    addUrlToPicturesDatabase("photobl", mDownloadUrl);
            }*/
        }
    }


    private void addUrlToProfileDatabase(final String path, Uri downloadUrl) {
        if (downloadUrl != null) {
            Map<String, Object> profileUpdates = new HashMap<String, Object>();
            profileUpdates.put(path, downloadUrl.toString());

            mProfileRef.updateChildren(profileUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "Failed to update profile", databaseError.toException());
                        Toast.makeText(context, "Error :( Your profile was not updated", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, path + " was updated!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void addUrlToPicturesDatabase(final String path, Uri downloadUrl) {

        Map<String, Object> profileUpdates = new HashMap<String, Object>();
        profileUpdates.put(path, downloadUrl.toString());

        mPhotosRef.updateChildren(profileUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Failed to update profile", databaseError.toException());
                    Toast.makeText(context, "Error :( Your profile was not updated", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, path + " was updated!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }



    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.profile_uploadphotoimageview) {
            launchCamera();
            whichPic = "1";
            Log.e("managephotos", "hit");
        } else if (i == R.id.cover_uploadphotoimageview) {
            launchCamera();
            whichPic = "2";
        } else if (i == R.id.tl_uploadphotoimageview) {
            launchCamera();
            whichPic = "3";
        } else if (i == R.id.tr_uploadphotoimageview) {
            launchCamera();
            whichPic = "4";
        } else if (i == R.id.bl_uploadphotoimageview) {
            launchCamera();
            whichPic = "5";
        } else if (i == R.id.br_uploadphotoimageview) {
            launchCamera();
            whichPic = "6";
        }
    }
}
