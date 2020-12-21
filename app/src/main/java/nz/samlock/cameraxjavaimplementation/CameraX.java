package nz.samlock.cameraxjavaimplementation;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
//https://medium.com/swlh/introduction-to-androids-camerax-with-java-ca384c522c5 <-- Tutorial used to setup preview.

public class CameraX extends AppCompatActivity implements View.OnClickListener {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FloatingActionButton takePhotoButton;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        initButtons();
        setButtonsOnClickListener();
        initCamera();

    }

    /**
     * Initialises and begins the camera flow.
     */
    private void initCamera() {
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    openCamera(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    /**
     * Initialises the on screen take photo button
     */
    private void initButtons() {
        takePhotoButton = findViewById(R.id.btn_camera_act_take_photo);
        takePhotoButton.setScaleType(ImageView.ScaleType.CENTER);
    }


    /**
     * Initialises the button onclick listener
     */
    private void setButtonsOnClickListener() {
        takePhotoButton.setOnClickListener(this);
    }


    /**
     * Opens the camera and takes the photo. ImageAnalysis/OrientationListener c
     * @param cameraProvider
     */
    private void openCamera(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                image.close();
            }
        });
//
//        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                // do nothing. Too scared to delete any of this code as it all works perfectly.
//            }
//        };
//        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,
                imageAnalysis, preview);

        //Builds
        imageCapture = new ImageCapture.Builder()
                //.setTargetRotation(getContext.view.getDisplay().getRotation())
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, imageAnalysis, preview);
    }


    /**
     * Makes the captured image usable. Gets the file paths back to user.
     *
     * https://stackoverflow.com/questions/60225806/outputfileresults-returned-by-onimagesavedcallback-has-an-invalid-uri
     * Using the above answer from the author to implement.
     */
    private void captureImage() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "/" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Runnable::run, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bundle params = new Bundle();
                params.putString("FILE_PATH", file.getAbsolutePath());
                goBackToMainActivity(file.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Returns you to the Apps MainActivity, including a URI String in the intent which can be used to access the photo again later.
     * @param uri
     */
    private void goBackToMainActivity(String uri) {
        Intent mainActivityBundle = new Intent(this, MainActivity.class);
        mainActivityBundle.putExtra("IMAGE_LOCATION", uri);
        this.startActivity(mainActivityBundle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_act_take_photo: ;
                captureImage();
                break;
        }
    }

}
