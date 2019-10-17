package com.junger.cameraapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private Button capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    private static int cameraCurStatu = 0;
    
    static String TAG = "AndroidCameraExample";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        Log.d("ChenMinCam", TAG + " " + "onCreate 42");
        initialize();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d("ChenMinCam", TAG + " " + "findFrontFacingCamera 49 numberOfCameras = " + numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                cameraCurStatu = 1;
                break;
            }
        }
        Log.d("ChenMinCam", TAG + " " + "findFrontFacingCamera 59 cameraId = " + cameraId);
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d("ChenMinCam", TAG + " " + "findBackFacingCamera 68 numberOfCameras = " + numberOfCameras);
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                cameraCurStatu = 0;
                break;
            }
        }
        Log.d("ChenMinCam", TAG + " " + "findFrontFacingCamera 79 cameraId = " + cameraId);
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        Log.d("ChenMinCam", TAG + " " + "onResume 86");
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
        	Log.d("ChenMinCam", TAG + " " + "onResume 94");
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }        
            Log.d("ChenMinCam", TAG + " " + "onResume 98");
            
            mCamera = Camera.open(0);// 
            //mCamera = Camera.open(2);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }

    public void initialize() {
    	Log.d("ChenMinCam", TAG + " " + "initialize 105");
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(captrureListener);
        capture.setVisibility(View.GONE);

        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        switchCamera.setOnClickListener(switchCameraListener);
    }

    OnClickListener switchCameraListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	Log.d("ChenMinCam", TAG + " " + "switchCameraListener 120");
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
            	Log.d("ChenMinCam", TAG + " " + "switchCameraListener 130");
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    public void chooseCamera() {
    	Log.d("ChenMinCam", TAG + " " + "chooseCamera 138 cameraFront = " + cameraFront);
    	Log.d("ChenMinCam", TAG + " " + "chooseCamera 147 cameraCurStatu = " + cameraCurStatu);
        //if the camera preview is the front
/*        if (cameraFront) {
        	Log.d("ChenMinCam", TAG + " " + "chooseCamera 141");
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);                
                mPicture = getPictureCallback();            
                mPreview.refreshCamera(mCamera);
            }
        } else {
        	Log.d("ChenMinCam", TAG + " " + "chooseCamera 153 camerathree = " + camerathree);
        	if (!camerathree) {
                int cameraId = findFrontFacingCamera();
                if (cameraId >= 0) {
                    //open the backFacingCamera
                    //set a picture callback
                    //refresh the preview

                    mCamera = Camera.open(cameraId);
                    mPicture = getPictureCallback();
                    mPreview.refreshCamera(mCamera);
                }   		      		
        	}

        }*/
    	
        int cameraId = ((cameraCurStatu +1) % 3);//findFrontFacingCamera();
        if (cameraId >= 0) {
            //open the backFacingCamera
            //set a picture callback
            //refresh the preview

            mCamera = Camera.open(cameraId);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        } 
        
        cameraCurStatu++;
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ChenMinCam", TAG + " " + "onPause 170");
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
    	Log.d("ChenMinCam", TAG + " " + "hasCamera 176");
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                File pictureFile = getOutputMediaFile();
                Log.d("ChenMinCam", TAG + " " + "getPictureCallback 192");
                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
                    toast.show();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    OnClickListener captrureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	Log.d("ChenMinCam", TAG + " " + "captrureListener 218");
            mCamera.takePicture(null, null, mPicture);
        }
    };

    //make picture and save to a folder
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "JCG Camera");
        Log.d("ChenMinCam", TAG + " " + "getOutputMediaFile 227");
        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void releaseCamera() {
    	Log.d("ChenMinCam", TAG + " " + "releaseCamera 246");
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}