package com.hcl.detection.utils.interfaces;

import com.hcl.detection.utils.models.RectModel;

public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);
    void onFaceNotLocated();
}
