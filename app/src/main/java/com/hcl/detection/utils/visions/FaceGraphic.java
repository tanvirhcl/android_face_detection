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

package com.hcl.detection.utils.visions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import com.hcl.detection.utils.interfaces.FaceDetectStatus;
import com.hcl.detection.utils.common.GraphicOverlay;
import com.hcl.detection.utils.models.RectModel;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private int facing;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;

    private final Bitmap overlayBitmap;

    FaceDetectStatus faceDetectStatus = null;


    FaceGraphic(GraphicOverlay overlay, FirebaseVisionFace face, int facing, Bitmap overlayBitmap) {
        super(overlay);

        firebaseVisionFace = face;
        this.facing = facing;
        this.overlayBitmap = overlayBitmap;
        final int selectedColor = Color.GREEN;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        // An offset is used on the Y axis in order to draw the circle, face id and happiness level in the top area
        // of the face's bounding box
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());

    //    canvas.drawCircle(x, y, 8.0f, facePositionPaint);


        // Draws a bounding box around the face.
        float left = x - scale(face.getBoundingBox().width() / 2.0f);
        float top = y - scale(face.getBoundingBox().height() / 2.0f);
        float right = x + scale(face.getBoundingBox().width() / 2.0f);
        float bottom = y + scale(face.getBoundingBox().height() / 2.0f);
      //  canvas.drawRect(left, top, right, bottom, boxPaint);

        Log.e("left",""+left);
        Log.e("top",""+top);
        Log.e("right",""+right);
        Log.e("bottom",""+bottom);




        if (left < 190 && top < 450 && right > 850 && bottom > 1050)
        { if (faceDetectStatus != null && hasLandMarks()) {
            if(hasEyeOpen()) faceDetectStatus.onFaceLocated(new RectModel(left, top, right, bottom));
            else faceDetectStatus.onFaceNotLocated();
        }
        }
        else
        {if (faceDetectStatus != null) {faceDetectStatus.onFaceNotLocated();}}

    }

    public  Boolean hasLandMarks(){
       return  firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)!=null &&
               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)!=null &&

               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK)!=null &&
               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK)!=null &&

               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)!=null &&
               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)!=null &&

               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT)!=null &&
               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)!=null &&
               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT)!=null &&

               firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)!=null;
    }

    public  Boolean hasEyeOpen(){
        return !(firebaseVisionFace.getRightEyeOpenProbability() < 0.4) &&  !(firebaseVisionFace.getLeftEyeOpenProbability() < 0.4);
    }

}
