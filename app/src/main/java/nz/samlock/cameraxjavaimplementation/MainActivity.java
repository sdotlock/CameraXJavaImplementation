package nz.samlock.cameraxjavaimplementation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private static final String[] WRITE_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 11;

    private static final String[] READ_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 12;

    ImageView displayImage;
    Button openCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCameraButton();

        try {
            checkForIntents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialises the camera button
     */
    private void initCameraButton() {
        openCameraButton = findViewById(R.id.main_btn_startcamera);
        openCameraButton.setOnClickListener(this);
    }


    /**
     * Opens camera activity
     */
    private void enableCamera() {
        Intent intent = new Intent(this, CameraX.class);
        startActivity(intent);
    }

    /**
     * Checks for required permissions.
     * N.B. ensure you have added permissions to your manifest(!)
     */
    private void takePhotoFlow() {
        if (hasCameraPermission()) {
            if (hasWriteExternalStoragePermission()){
                if (hasReadExternalStoragePermission()) {
                    enableCamera();
                } else {
                    requestReadExternalStoragePermission();
                }
            } else {
                requestWriteExternalStoragePermission();
            }
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Creates and displays a thumbnail from a URI String.
     * @param uriString
     * @throws IOException
     */
    private void displayMostRecentImageInView(String uriString) throws IOException {
            String[] newString = uriString.split("file://");
            Bitmap thumbnail = ThumbnailUtils.createImageThumbnail(newString[1], MediaStore.Images.Thumbnails.MINI_KIND);
            displayImage = findViewById(R.id.main_imageview);
            displayImage.setImageBitmap(thumbnail);
    }




    /**
     * Checks for a URI String being passed back from the Camera Activity.
     * Runs a thumbnail flow one is present.
     * @throws IOException
     */
    private void checkForIntents() throws IOException {
        Intent intent = getIntent();
        String incomingString = intent.getStringExtra("IMAGE_LOCATION");

        if (incomingString == null) {
            //do nothing, no image associated
        } else {
            String stringExtra = "file://" + intent.getStringExtra("IMAGE_LOCATION");
            Uri uri = Uri.parse(stringExtra);
            displayMostRecentImageInView(stringExtra);
        }

    }

    /**
     * Checks for camera permissions
     * @return
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks for read external storage permissions
     * @return
     */
    private boolean hasReadExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks for writing external storage permissions
     * @return
     */
    private boolean hasWriteExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Asks for camera permissions
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    /**
     * Asks for write storage permissions
     */
    private void requestWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                WRITE_PERMISSION,
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
        );
    }

    /**
     * Asks for read external storage permission
     */
    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                READ_PERMISSION,
                READ_EXTERNAL_STORAGE_REQUEST_CODE
        );
    }

    /**
     * Event trigger from button click.
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_startcamera:
                takePhotoFlow();
                break;
        }


    }
}