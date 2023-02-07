// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.hcl.detection.ui.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.IOException;
import java.util.List;

import com.hcl.detection.R;

import com.hcl.detection.utils.base.BaseActivity;
import com.hcl.detection.utils.common.CameraSource;
import com.hcl.detection.utils.common.CameraSourcePreview;
import com.hcl.detection.utils.interfaces.FaceDetectStatus;
import com.hcl.detection.utils.common.FrameMetadata;
import com.hcl.detection.utils.interfaces.FrameReturn;
import com.hcl.detection.utils.common.GraphicOverlay;
import com.hcl.detection.utils.base.PublicMethods;
import com.hcl.detection.utils.models.RectModel;
import com.hcl.detection.utils.tensor.Detector;
import com.hcl.detection.utils.tensor.TFLiteObjectDetectionAPIModel;
import com.hcl.detection.utils.visions.FaceDetectionProcessor;


@KeepName
public final class CameraActivity extends BaseActivity implements OnRequestPermissionsResultCallback, FrameReturn, FaceDetectStatus, View.OnClickListener {
    private static final String FACE_DETECTION = "Face Detection";
    private static final String TAG = "MLKitTAG";

    Bitmap originalImage = null;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private ImageView faceFrame;
    private Bitmap croppedImage = null;

    private Detector deductors;

    private MaterialButton btnVerify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        preview = findViewById(R.id.firePreview);
        faceFrame = findViewById(R.id.faceFrame);
        btnVerify = findViewById(R.id.btnVerify);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);


        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        } else {
            PublicMethods.getRuntimePermissions(this);
        }

        btnVerify.setOnClickListener(this);

        try {
            deductors = TFLiteObjectDetectionAPIModel.create(getApplicationContext(),"human-face.tflite","label.txt",320,true);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            Log.e(TAG, "Can not create image processor: " + FACE_DETECTION, e);
            Toast.makeText(getApplicationContext(), "Can not create image processor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void startCameraSource() {
        if (cameraSource != null) {
            try {
               // cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
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
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PublicMethods.allPermissionsGranted(this)) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //calls with each frame includes by face
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
        faceFrame.setVisibility(View.GONE);
        cameraSource.release();
        boolean isFaceMatch = false;
        List<Detector.Recognition> list = deductors.recognizeImage(originalImage);
        for(Detector.Recognition recognition: list) {
            Log.e("confidence",""+recognition.getConfidence());
            if(recognition.getConfidence() > 0.60) {
                isFaceMatch = true;
                showDialog("Authentication Successful!",recognition.getTitle(),true);
                break;
            }
        }

        if(!isFaceMatch) showDialog("No one matched!","Unknown",false);
       // showDialog("Authentication Successful!","Tanvir",true);
    }

    private void showDialog(String message, String name, boolean check){
        new IdentificationDialog(message,name,check).show(getSupportFragmentManager(),"al");
    }
}
