package com.hcl.detection.ui.main;

import static com.hcl.detection.utils.common.FilePath.CHECK_IN;
import static com.hcl.detection.utils.common.FilePath.NAME;
import static com.hcl.detection.utils.common.FilePath.PREF_NAME;
import static com.hcl.detection.utils.common.FilePath.TIME;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.hcl.detection.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        TextView tvResponse = findViewById(R.id.tvResponse);

        tvResponse.setText("Authentication Successful"+" \n" +
                           "Welcome "+getIntent().getStringExtra("name")+" \n" +
                            new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(Calendar.getInstance().getTime()));
        
        
        getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit().putString(NAME,getIntent().getStringExtra("name")).apply();
        getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit().putString(TIME,""+Calendar.getInstance().getTime()).apply();
        getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit().putBoolean(CHECK_IN,true).apply();



    }

}