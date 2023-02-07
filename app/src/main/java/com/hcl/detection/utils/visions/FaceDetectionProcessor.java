package com.hcl.detection.utils.visions;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

import com.hcl.detection.R;
import com.hcl.detection.utils.common.CameraImageGraphic;
import com.hcl.detection.utils.interfaces.FaceDetectStatus;
import com.hcl.detection.utils.common.FrameMetadata;
import com.hcl.detection.utils.interfaces.FrameReturn;
import com.hcl.detection.utils.common.GraphicOverlay;
import com.hcl.detection.utils.common.VisionProcessorBase;
import com.hcl.detection.utils.models.RectModel;


public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> implements FaceDetectStatus {

    private static final String TAG = "FaceDetectionProcessor";
    public FaceDetectStatus faceDetectStatus = null;
    private final FirebaseVisionFaceDetector detector;

    private final Bitmap overlayBitmap;

    public FrameReturn frameHandler = null;

    public FaceDetectionProcessor(Resources resources) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        overlayBitmap = BitmapFactory.decodeResource(resources, R.drawable.clown_nose);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            if (frameHandler != null) {
                frameHandler.onFrame(originalCameraImage, face, frameMetadata, graphicOverlay);
            }
            int cameraFacing =
                    frameMetadata != null ? frameMetadata.getCameraFacing() :
                            Camera.CameraInfo.CAMERA_FACING_BACK;
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, cameraFacing, overlayBitmap);
            faceGraphic.faceDetectStatus = this;
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    @Override
    public void onFaceLocated(RectModel rectModel) {
        if (faceDetectStatus != null) faceDetectStatus.onFaceLocated(rectModel);
    }

    @Override
    public void onFaceNotLocated() {
        if (faceDetectStatus != null) faceDetectStatus.onFaceNotLocated();
    }
}