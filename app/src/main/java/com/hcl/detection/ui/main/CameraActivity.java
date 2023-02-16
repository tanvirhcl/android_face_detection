
package com.hcl.detection.ui.main;

import static android.os.Build.VERSION.SDK_INT;
import static com.hcl.detection.utils.base.PublicMethods.allPermissionsGranted;
import static com.hcl.detection.utils.base.PublicMethods.getRuntimePermissions;
import static com.hcl.detection.utils.common.CameraSource.CAMERA_FACING_BACK;
import static com.hcl.detection.utils.common.CameraSource.CAMERA_FACING_FRONT;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;

import com.hcl.detection.R;

import com.hcl.detection.utils.base.BaseActivity;
import com.hcl.detection.utils.common.CameraSource;
import com.hcl.detection.utils.common.CameraSourcePreview;
import com.hcl.detection.utils.common.ImageHelper;
import com.hcl.detection.utils.interfaces.CameraCallback;
import com.hcl.detection.utils.interfaces.FaceDetectStatus;
import com.hcl.detection.utils.common.FrameMetadata;
import com.hcl.detection.utils.interfaces.FrameReturn;
import com.hcl.detection.utils.common.GraphicOverlay;
import com.hcl.detection.utils.models.RectModel;
import com.hcl.detection.utils.visions.FaceDetectionProcessor;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;


@KeepName
public final class CameraActivity extends BaseActivity implements OnRequestPermissionsResultCallback, FrameReturn, FaceDetectStatus, View.OnClickListener, CameraCallback {

    private static final String TAG = CameraActivity.class.getSimpleName();

    // View
    private MaterialButton btnVerify;
    private ImageView faceFrame,ivCamera;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;


    private ObjectDetector deductors;
    private Bitmap originalImage = null;

    private Float match = 0.60f;
    private final int REQUEST_PERMISSION = 10;

    private int facing = CAMERA_FACING_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        init();
        initCtrl();
        loadObjectDetector();

        if(checkPermission()) createCameraSource();
        else requestPermission();
    }


    private void init() {
        preview = findViewById(R.id.firePreview);
        faceFrame = findViewById(R.id.faceFrame);
        ivCamera = findViewById(R.id.ivCamera);
        btnVerify = findViewById(R.id.btnVerify);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
    }
    private void initCtrl() {
        btnVerify.setOnClickListener(this);
        ivCamera.setOnClickListener(this);
    }
    
    private boolean checkPermission() {
       boolean ret = true;
        if (SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ret = false;
            }
        }
        return ret;
    }

    private void requestPermission(){
        if (SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
    }
    private void loadObjectDetector() {
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder()
                                                                    //.setBaseOptions(BaseOptions.builder().useGpu().build())
                                                                     .setMaxResults(255)
                                                                    // .setScoreThreshold(match)
                                                                     .build();

        try {
            MappedByteBuffer buffer = FileUtil.loadMappedFile(this,"human-face.tflite");
            deductors = ObjectDetector.createFromBufferAndOptions(buffer, options);
        }catch (Exception e) {
            Log.e("error",e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            FaceDetectionProcessor processor = new FaceDetectionProcessor(getResources());
            processor.frameHandler = this;
            processor.faceDetectStatus = this;
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " , e);
            Toast.makeText(getApplicationContext(), "Can not create image processor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void startCameraSource(int facing) {
        if (cameraSource != null) {
            try {
                cameraSource.setFacing(facing);
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource(facing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
        deductors.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
        deductors.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_PERMISSION:
                boolean isPermissionDenied=false;
                for (int grantResult : grantResults) {
                    if (!(grantResult == PackageManager.PERMISSION_GRANTED)) {
                        isPermissionDenied = true;
                        break;
                    }
                }

                if(isPermissionDenied)  Toast.makeText(this,"Please grant permissions",Toast.LENGTH_LONG).show();
                else createCameraSource();
                break;
        }
    }

    @Override
    public void onFrame(Bitmap image, FirebaseVisionFace face, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) {
        originalImage = image;
    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        btnVerify.setEnabled(true);
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.green));
    }

    @Override
    public void onFaceNotLocated() {
        btnVerify.setEnabled(false);
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.red));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivCamera:
                switch (cameraSource.getCameraFacing()){
                    case CAMERA_FACING_FRONT: changeCamera(CAMERA_FACING_BACK); break;
                    case CAMERA_FACING_BACK: changeCamera(CAMERA_FACING_FRONT); break;
                }
                break;

            case R.id.btnVerify: authFace(); break;
        }
    }

    private void changeCamera(int facing){
        this.facing=facing;
        cameraSource.release();
        createCameraSource();
        startCameraSource(facing);
    }

    private void authFace() {
        faceFrame.setVisibility(View.GONE);
        cameraSource.stop();

        Float currentScore = match;
        String username ="";

        Boolean isMatch= false;


        List<Detection> list = deductors.detect(TensorImage.fromBitmap(originalImage));
        for(Detection detection: list) {

            float score = detection.getCategories().get(0).getScore();
            String name = detection.getCategories().get(0).getLabel();

            if(username.isEmpty()) username = name;


            Log.e("Categories",""+score+" - "+name);

            if(score > currentScore  && score > match) {
                isMatch = true;
                currentScore = score;
                username = name;
            }
        }



        if(isMatch) {
            startActivity(new Intent(this,WelcomeActivity.class).putExtra("name",username));
            finish();
        }
        else {
            IdentificationDialog dialog = new IdentificationDialog();
            dialog.setCallBack(this);
            dialog.show(getSupportFragmentManager(),"");
        }
    }

    @Override
    public void restartCamera() {
        faceFrame.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        faceFrame.setColorFilter(ContextCompat.getColor(this, R.color.red));
        startCameraSource(facing);
    }
}
