package com.media2359.nickel.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.media2359.nickel.R;
import com.media2359.nickel.camera.CameraOrientationListener;
import com.media2359.nickel.camera.CameraPreview;
import com.media2359.nickel.camera.IDCardOverlay;
import com.media2359.nickel.utils.BitmapUtils;
import com.media2359.nickel.utils.Const;
import com.media2359.nickel.utils.PreferencesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * The general steps for creating a custom camera interface for your application are as follows:
 * <p/>
 * Detect and Access Camera - Create code to check for the existence of cameras and request access.
 * Create a Preview Class - Create a camera preview class that extends SurfaceView and implements the SurfaceHolder interface. This class previews the live images from the camera.
 * Build a Preview Layout - Once you have the camera preview class, create a view layout that incorporates the preview and the user interface controls you want.
 * Setup Listeners for Capture - Connect listeners for your interface controls to start image or video capture in response to user actions, such as pressing a button.
 * Capture and Save Files - Setup the code for capturing pictures or videos and saving the output.
 * Release the Camera - After using the camera, your application must properly release it for use by other applications.
 */
public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = "CaptureActivity";
    private static final int MY_PERMISSION_CAMERA = 9;

    public static final String EXTRA_IMAGE_TYPE = "image_type";
    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final int IMAGE_PROFILE = 11;
    public static final int IMAGE_RECEIPT = 12;
    private static final int IMAGE_COMPRESSION_QUALITY = 100;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button captureButton, cancelButton;
    private FrameLayout preview;
    private int imageType = -1;
    private int requestCode = -1;
    private IDCardOverlay idCardOverlay;
    private int rotation;
    private TextView tvTop;

    private CameraHandlerThread mThread = null;

    private ProgressDialog progressDialog;

    SaveProfileImageAsync saveProfileImageAsync;
    CameraOrientationListener orientationListener;


    public static void startCapturingIDCard(Activity activity, int requestCode) {
        Intent i = new Intent(activity, CaptureActivity.class);
        i.putExtra(EXTRA_IMAGE_TYPE, IMAGE_PROFILE);
        i.putExtra(EXTRA_REQUEST_CODE, requestCode);
        activity.startActivityForResult(i, requestCode);
    }

    public static void startCapturingIDCard(Fragment fragment, int requestCode) {
        Intent i = new Intent(fragment.getActivity(), CaptureActivity.class);
        i.putExtra(EXTRA_IMAGE_TYPE, IMAGE_PROFILE);
        i.putExtra(EXTRA_REQUEST_CODE, requestCode);
        fragment.startActivityForResult(i, requestCode);
    }

    public static void startCapturingReceipt(Activity activity, int requestCode) {
        Intent i = new Intent(activity, CaptureActivity.class);
        i.putExtra(EXTRA_IMAGE_TYPE, IMAGE_RECEIPT);
        i.putExtra(EXTRA_REQUEST_CODE, requestCode);
        activity.startActivityForResult(i, requestCode);
    }

    public static void startCapturingReceipt(Fragment fragment, int requestCode) {
        Intent i = new Intent(fragment.getActivity(), CaptureActivity.class);
        i.putExtra(EXTRA_IMAGE_TYPE, IMAGE_RECEIPT);
        i.putExtra(EXTRA_REQUEST_CODE, requestCode);
        fragment.startActivityForResult(i, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        initViews();

        imageType = getIntent().getIntExtra(EXTRA_IMAGE_TYPE, IMAGE_PROFILE);
        requestCode = getIntent().getIntExtra(EXTRA_REQUEST_CODE, -1);

        Log.d(TAG, "onResume: imageType " + imageType);
        if (imageType == IMAGE_PROFILE) {
            // for ID card overlay
            idCardOverlay.setVisibility(View.VISIBLE);
            tvTop.setText("Please place your ID inside the frame");
        } else {
            // for receipt overlay
            idCardOverlay.setVisibility(View.GONE);
            tvTop.setText("Please place your Receipt inside the frame");
        }

        if (checkCameraHardware(getApplicationContext()))
            checkCameraPermission();
        else {
            Toast.makeText(getApplicationContext(), "Sorry, this device does not have a camera", Toast.LENGTH_SHORT).show();
            finish();
        }

        orientationListener = new CameraOrientationListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        orientationListener.enable();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        imageType = getIntent().getIntExtra(EXTRA_IMAGE_TYPE, IMAGE_PROFILE);
        requestCode = getIntent().getIntExtra(EXTRA_REQUEST_CODE, -1);

        Log.d(TAG, "onNewIntent: imageType " + imageType);
        if (imageType == IMAGE_PROFILE) {
            // for ID card overlay
            idCardOverlay.setVisibility(View.VISIBLE);
            tvTop.setText("Please place your ID inside the frame");
        } else {
            // for receipt overlay
            idCardOverlay.setVisibility(View.GONE);
            tvTop.setText("Please place your Receipt inside the frame");
        }
    }

    private void checkCameraPermission() {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        // Here, thisActivity is the current activity
        if (permissionCheck
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSION_CAMERA);

        } else {
            newOpenCamera();
            //initViews();
            //oldOpenCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //initViews();
                    newOpenCamera();
                    //oldOpenCamera();

                } else {
                    //TODO
                    Toast.makeText(getApplicationContext(), "Camera permission is not granted", Toast.LENGTH_SHORT).show();
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        newReleaseCamera();
        orientationListener.disable();

        if (saveProfileImageAsync != null) {
            saveProfileImageAsync.cancel(false);
            saveProfileImageAsync = null;
        }

        if (mThread != null) {
            CameraHandlerThread mock = mThread;
            mThread = null;
            mock.interrupt();
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //releaseCamera();
//        newReleaseCamera();
//
//        if (saveProfileImageAsync != null) {
//            saveProfileImageAsync.cancel(false);
//            saveProfileImageAsync = null;
//        }
//
//        if (mThread != null) {
//            CameraHandlerThread mock = mThread;
//            mThread = null;
//            mock.interrupt();
//        }
//
//        this.finish();

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    private void initViews() {

        idCardOverlay = (IDCardOverlay) findViewById(R.id.overlay_IDCard);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        tvTop = (TextView) findViewById(R.id.tvTop);
        cancelButton = (Button) findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void oldOpenCamera() {
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(CaptureActivity.this, mCamera);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preview.addView(mCameraPreview, 0);
            }
        });

    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }


    private void newReleaseCamera() {
        if (mThread == null)
            return;

        synchronized (mThread) {
            mThread.closeCamera();
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.d(TAG, "camera is null");
            throw e;
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }


        void openCamera() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera();
                }
            });

        }

        void closeCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    releaseCamera();
                }
            });
        }
    }


    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(CaptureActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Processing...");

                    rotation = mCameraPreview.getCameraRotation();
                    orientationListener.rememberOrientation();

                    Log.d(TAG, "run: rotation " + rotation + " remember " + orientationListener.getRememberedOrientation());

                    saveProfileImageAsync = new SaveProfileImageAsync();
                    saveProfileImageAsync.execute(data);
                }
            });

            mCamera.stopPreview();
        }
    };


    private class SaveProfileImageAsync extends AsyncTask<byte[], Void, Void> {

        private WeakReference<ProgressDialog> progressDialogWeakReference;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogWeakReference = new WeakReference<ProgressDialog>(progressDialog);
            if (progressDialogWeakReference.get() != null) {
                progressDialogWeakReference.get().show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialogWeakReference.get().dismiss();
            finish();
        }

        @Override
        protected Void doInBackground(byte[]... params) {

            byte[] data = params[0];

            try {

                if (isCancelled())
                    return null;

                // crop Image
                Bitmap finalImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                int actualRotation = (
                        rotation
                                + orientationListener.getRememberedOrientation()
                ) % 360;

                // if the image is saved as landscape mode
                if (finalImage.getHeight() < finalImage.getWidth()) {
                    finalImage = BitmapUtils.rotateImage(finalImage, actualRotation);
                }

                if (imageType == IMAGE_PROFILE) {
                    finalImage = BitmapUtils.cropCenter(finalImage);
                }

                File imageFile;
                if (imageType == IMAGE_PROFILE) {
                    imageFile = new File(getFilesDir(), getIntent().getIntExtra(EXTRA_REQUEST_CODE, 1001) + "_nickel.png");
                } else {
                    imageFile = new File(getFilesDir(), new Date().getTime() + getIntent().getIntExtra(EXTRA_REQUEST_CODE, 1002) + "_nickel.png");
                }

                if (imageFile.exists()) {
                    imageFile.delete();
                }

                if (imageFile.createNewFile()) {
                    // save image into gallery
                    FileOutputStream fos = openFileOutput(imageFile.getName(), MODE_PRIVATE);
                    finalImage.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, fos);
                    fos.flush();
                    fos.close();
                    finalImage.recycle();

                    if (imageType == IMAGE_PROFILE) {
                        if (requestCode == Const.REQUEST_PICTURE_FROM_CAMERA_FRONT) {
                            PreferencesUtils.saveIDFront(getApplicationContext(), Uri.fromFile(imageFile).toString());
                        } else if (requestCode == Const.REQUEST_PICTURE_FROM_CAMERA_BACK) {
                            PreferencesUtils.saveIDBack(getApplicationContext(), Uri.fromFile(imageFile).toString());
                        } else if (requestCode == Const.REQUEST_CODE_RECEIPT_PHOTO) {
                            // TODO save the receipt photo for that transaction
                        }
                    }

                    Intent intent = new Intent();
                    intent.putExtra(Const.DATA_PHOTO_FILE, imageFile.getPath());
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
