package com.hcl.detection.utils.interfaces;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import com.hcl.detection.utils.common.FrameMetadata;
import com.hcl.detection.utils.common.GraphicOverlay;

public interface FrameReturn{
    void onFrame(
            Bitmap image ,
            FirebaseVisionFace face ,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}