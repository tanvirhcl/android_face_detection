package com.hcl.detection.ui.main;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.airbnb.lottie.LottieAnimationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.hcl.detection.R;

public class IdentificationDialog extends DialogFragment {

    private String message;
    private String name;

    private boolean check;
    public IdentificationDialog(String message, String name,boolean check){
        this.message= message;
        this.name= name;
        this.check=check;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_indentification,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Calendar.getInstance().getTime();
        TextView tvResponse = view.findViewById(R.id.tvResponse);
        LottieAnimationView animationView = view.findViewById(R.id.animationView);
        if(check){
            animationView.setAnimation(R.raw.success);
        }
        else {
            animationView.setAnimation(R.raw.failure);
        }
        tvResponse.setText(message+" \n" +
                            "User "+name+" \n" +
                            new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(Calendar.getInstance().getTime()));
        view.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
               requireActivity().finish();
            }
        });

    }


    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}