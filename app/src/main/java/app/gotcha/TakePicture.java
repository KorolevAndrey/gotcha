package app.gotcha;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TakePicture extends Activity implements SurfaceHolder.Callback {
    // a variable to store a reference to the Image View at the main.xml file
    // private ImageView iv_image;
    // a variable to store a reference to the Surface View at the main.xml file
    private SurfaceView sv;

    // a bitmap to display the captured image
    private Bitmap bmp;
    FileOutputStream fo;

    // Camera variables
    // a surface holder
    private SurfaceHolder sHolder;
    // a variable to control the camera
    private Camera mCamera;
    // the camera parameters
    private Camera.Parameters parameters;
    private String FLASH_MODE;
    private boolean isFrontCamRequest = false;
    private Camera.Size pictureSize;
    GotchaService gotchaService;

    /**
     * Called when the activity is first created.
     */


    private File image;
//    private MediaRecorder myAudioRecorder;
//    private File soundFile;


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
//        myAudioRecorder = new MediaRecorder();
//        File soundsFolder = new File(Environment.getExternalStorageDirectory(), "GotchaSounds");
//        String fileName = System.currentTimeMillis() + "";
//        soundFile = new File(soundsFolder,
//                fileName + ".3gp");
//        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
//        myAudioRecorder.setOutputFile(soundFile.getAbsolutePath());
//        try {
//            myAudioRecorder.prepare();
//            myAudioRecorder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        // check if this device has a camera
        if (checkCameraHardware(getApplicationContext())) {
            // get the Image View at the main.xml file
            // iv_image = (ImageView) findViewById(R.id.imageView);

            // get the Surface View at the main.xml file
            Bundle extras = getIntent().getExtras();
            String flash_mode = extras.getString("FLASH");
            FLASH_MODE = flash_mode;
            boolean front_cam_req = extras.getBoolean("Front_Request");
            isFrontCamRequest = front_cam_req;

            sv = (SurfaceView) findViewById(R.id.camera_preview);

            // Get a surface
            sHolder = sv.getHolder();

            // add the callback interface methods defined below as the Surface
            // View
            // callbacks
            sHolder.addCallback(this);

            // tells Android that this surface will have its data constantly
            // replaced
            if (Build.VERSION.SDK_INT < 11)
                sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        } else {
        }

    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Check if this device has front camera
     */
    private boolean checkFrontCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has front camera
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // get camera parameters
        if (mCamera != null) {
            parameters = mCamera.getParameters();
            if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                FLASH_MODE = "auto";
            }
            parameters.setFlashMode(FLASH_MODE);
            pictureSize = getBiggesttPictureSize(parameters);
            if (pictureSize != null)
                parameters
                        .setPictureSize(pictureSize.width, pictureSize.height);
            // set camera parameters
            mCamera.setParameters(parameters);

            mCamera.startPreview();

            // sets what code should be executed after the picture is taken
            final Camera.PictureCallback mCall = new Camera.PictureCallback() {


                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    // decode the data obtained by the camera into a Bitmap
                    Log.d("ImageTaking", "Done");

                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    if (bmp != null)
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    File imagesFolder = new File(
                            Environment.getExternalStorageDirectory(),
                            "Gotcha");
                    if (!imagesFolder.exists())
                        imagesFolder.mkdirs(); // <----


                    String fileName = System.currentTimeMillis() + "";
                    image = new File(imagesFolder,
                            fileName + ".jpg");

                    // write the bytes in file
                    try {
                        fo = new FileOutputStream(image);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                    }
                    try {
                        fo.write(bytes.toByteArray());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                    }

                    // remember close de FileOutput
                    try {
                        fo.close();

                        PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).edit().putString("path", image.getAbsolutePath()).commit();
                        BackgroundMail.newBuilder(TakePicture.this)
                                .withUsername(getString(R.string.AppMail))
                                .withPassword(getString(R.string.AppMailPassword))
                                .withMailto(PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getString("email", ""))
                                .withSubject("Gotcha Lock Alert")
                                .withBody("You can find your phone in this location  " +
                                        "http://maps.google.com/maps?q="
                                        + PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getString("lat", "")
                                        + ","
                                        + PreferenceManager.getDefaultSharedPreferences(AppController.getInstance()).getString("lon", ""))
                                .withAttachments(image.getAbsolutePath())
                                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                                    @Override
                                    public void onSuccess() {
                                        //do some magic
                                    }
                                })
                                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                                    @Override
                                    public void onFail() {
                                        //do some magic

                                    }
                                })
                                .send();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent mediaScanIntent = new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(image); //out is your output file
                            mediaScanIntent.setData(contentUri);
                            sendBroadcast(mediaScanIntent);
                        } else {
                            sendBroadcast(new Intent(
                                    Intent.ACTION_MEDIA_MOUNTED,
                                    Uri.parse("file://"
                                            + Environment.getExternalStorageDirectory())));
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                    }
                    if (mCamera != null) {
                        mCamera.stopPreview();
                        // release the camera
                        mCamera.release();
//                        myAudioRecorder.stop();
//                        myAudioRecorder.release();
//                        myAudioRecorder = null;
                    }
                    if (bmp != null) {
                        bmp.recycle();
                        bmp = null;
                        System.gc();
                    }
                    TakePicture.this.finish();

                }
            };

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCamera.takePicture(null, null, mCall);
                }
            }, 1000);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw the preview.
        if (isFrontCamRequest) {

            // set flash 0ff
            FLASH_MODE = "off";
            // only for gingerbread and newer versions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                mCamera = openFrontFacingCameraGingerbread();
                try {
                    mCamera.setPreviewDisplay(holder);

                } catch (IOException exception) {
                    mCamera = null;
                    TakePicture.this.finish();
                }
            } else {
                if (checkFrontCamera(getApplicationContext())) {
                    mCamera = openFrontFacingCameraGingerbread();
                    try {
                        mCamera.setPreviewDisplay(holder);

                    } catch (IOException exception) {
                        mCamera = null;
                        TakePicture.this.finish();
                    }
                }/*
             * else { // API dosen't support front camera or no front camera
             * Log.d("Camera",
             * "API dosen't support front camera or no front camera");
             * Toast.makeText( getApplicationContext(),
             * "API dosen't support front camera or no front camera",
             * Toast.LENGTH_LONG).show();
             *
             * finish(); }
             */

            }
        } else {
            mCamera = getCameraInstance();
            try {
                mCamera.setPreviewDisplay(holder);

            } catch (Exception exception) {
                mCamera = null;
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop the preview
    /*
     * mCamera.stopPreview(); // release the camera mCamera.release();
     */
        // unbind the camera from this object
        mCamera = null;
    }

    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Camera",
                            "Camera failed to open: " + e.getLocalizedMessage());
                }
                break; // musha
            }
        }
        return cam;
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

    private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }


}