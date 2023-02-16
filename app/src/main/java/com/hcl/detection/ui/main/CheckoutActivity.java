package com.hcl.detection.ui.main;

import static com.hcl.detection.utils.common.FilePath.CHECK_IN;
import static com.hcl.detection.utils.common.FilePath.NAME;
import static com.hcl.detection.utils.common.FilePath.PREF_NAME;
import static com.hcl.detection.utils.common.FilePath.TIME;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hcl.detection.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        TextView tvResponse = findViewById(R.id.tvResponse);

        Date startTime = new Date(getSharedPreferences(PREF_NAME,MODE_PRIVATE).getString(TIME,""));
        Date endTime = Calendar.getInstance().getTime();


        tvResponse.setText("Checkout Successful!"+" \n" +
                           getSharedPreferences(PREF_NAME,MODE_PRIVATE).getString(NAME,"")+" \n" +
                           "Time "+printDifference(startTime,endTime));



        getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit().putBoolean(CHECK_IN,false).apply();
    }

    public String printDifference(Date startTime, Date endTime) {

        long different = endTime.getTime() - startTime.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        if(elapsedHours>0) return  elapsedHours +" h "+elapsedMinutes+" m " +elapsedSeconds +" s";
        else if(elapsedMinutes>0) return elapsedMinutes+" m " +elapsedSeconds +" s";
        else return elapsedSeconds +" s";
    }

}