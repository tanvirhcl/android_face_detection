package com.hcl.detection.utils.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.hcl.detection.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImageHelper {


    public String takePhotoIntent(Activity context, String fileName,int REQUEST_CODE) {
        String capture_dir= Environment.getExternalStorageDirectory() + "/Face/"+fileName+"/";
        File file = new File(capture_dir);
        if (!file.exists())
        {
            file.mkdirs();
        }
        String path = capture_dir + System.currentTimeMillis() + ".jpg";
        Uri imageFileUri = FileProvider.getUriForFile(Objects.requireNonNull(context.getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", new File(path));
        Intent intent = new ImageHelper().getPickIntent(context,imageFileUri);
        context.startActivityForResult(intent, REQUEST_CODE);
        return path;
    }


    public   Intent getPickIntent(Context context,Uri cameraOutputUri) {
        final List<Intent> intents = new ArrayList<Intent>();

        if (true) {
            intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }

        if (true) {
            setCameraIntents(context,intents, cameraOutputUri);
        }

        if (intents.isEmpty()) return null;
        Intent result = Intent.createChooser(intents.remove(0), null);
        if (!intents.isEmpty()) {
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[] {}));
        }
        return result;


    }

    public  void setCameraIntents(Context context, List<Intent> cameraIntents, Uri output) {
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            intent.putExtra("uri",output);
            cameraIntents.add(intent);
        }
    }


}
