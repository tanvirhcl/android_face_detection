package com.hcl.detection.ui.main;

import static com.hcl.detection.utils.common.FilePath.PREF_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.hcl.detection.R;
import com.hcl.detection.utils.common.FilePath;

public class SectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);


        findViewById(R.id.cardView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SectionActivity.this, RegistrationActivity.class));
            }
        });


        findViewById(R.id.cardView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SectionActivity.this, CameraActivity.class));
            }
        });


        findViewById(R.id.cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getSharedPreferences(PREF_NAME,MODE_PRIVATE).getBoolean(FilePath.CHECK_IN,false)){
                    startActivity(new Intent(SectionActivity.this,CheckoutActivity.class));
                }else{
                    Toast.makeText(SectionActivity.this,"Please check-in first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}