package com.hcl.detection.ui.main;

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hcl.detection.BuildConfig;
import com.hcl.detection.R;
import com.hcl.detection.utils.common.FilePath;
import com.hcl.detection.utils.common.ImageHelper;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {


    // View
    private TextInputEditText tieFullName,tieEmail,tieMobileNo;
    private MaterialButton mbSubmit;
    private ImageView ivAdd;
    private RecyclerView rvItems;

    // Image Picker
    private String path;
    private final int REQUEST_PERMISSION = 10;
    private final int REQUEST_PHOTO= 12;
    private final int REQUEST_MANAGE_EXTERNAL= 14;
    private List<String> imageList= new ArrayList<>();
    private int maxSize = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        init();
        initCtrl();
    }

    private void init(){
        tieFullName=findViewById(R.id.tieFullName);
        tieEmail=findViewById(R.id.tieEmail);
        tieMobileNo=findViewById(R.id.tieMobileNo);
        mbSubmit=findViewById(R.id.mbSubmit);
        ivAdd=findViewById(R.id.ivAdd);
        rvItems=findViewById(R.id.rvItems);

        rvItems.setAdapter(new ImageAdapter(this,imageList));
    }
    private void initCtrl() {
       ivAdd.setOnClickListener(this);
        mbSubmit.setOnClickListener(this);
    }

    private boolean checkPermission() {
        boolean ret=true;

        if (SDK_INT >= Build.VERSION_CODES.R) {
            ret = Environment.isExternalStorageManager();
        }
        else if (SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED  ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ret = false;
            }
        }

        return  ret;
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL);
            }
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivAdd: if(checkPermission())  path=new ImageHelper().takePhotoIntent(this,tieFullName.getText().toString(),REQUEST_PHOTO);
                             else requestPermission();

            break;
            case R.id.mbSubmit : if(checkValidation()) {
                Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show();
                finish();
            }  break;
        }
    }

    private boolean checkValidation(){
        boolean validate = true;

        if(TextUtils.isEmpty(tieFullName.getText().toString().trim())) {
            validate = false;
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(tieEmail.getText().toString().trim())) {
            validate = false;
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        }else if(! Patterns.EMAIL_ADDRESS.matcher(tieEmail.getText().toString()).matches()){
            validate = false;
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(tieMobileNo.getText().toString().trim())){
            validate = false;
            Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
        }
        else if(tieMobileNo.getText().toString().length()<10){
            validate = false;
            Toast.makeText(this, "Please enter 10 digit mobile number", Toast.LENGTH_SHORT).show();
        }
        else if(imageList.size()<maxSize){
            validate = false;
            Toast.makeText(this, "Please add at least "+maxSize+" images", Toast.LENGTH_SHORT).show();
        }

        return validate;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_PHOTO:
                if(resultCode == RESULT_OK) {
                    if (data != null)  { path = FilePath.getPath(this, Uri.parse(data.getDataString())); }
                    imageList.add(path);
                    int lastIndex = imageList.size() - 1;
                    rvItems.getAdapter().notifyItemChanged(lastIndex, imageList.get(lastIndex));
                }
                break;


            case REQUEST_MANAGE_EXTERNAL:
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION);
                    else Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
                else path=new ImageHelper().takePhotoIntent(this,tieFullName.getText().toString(),REQUEST_PHOTO);
                break;
        }
    }

}