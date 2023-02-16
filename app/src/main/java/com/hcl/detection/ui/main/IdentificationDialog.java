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

import com.google.android.material.button.MaterialButton;
import com.hcl.detection.R;
import com.hcl.detection.utils.interfaces.CameraCallback;

public class IdentificationDialog extends DialogFragment implements View.OnClickListener {

    private MaterialButton btnOK,btnRetry;
    private CameraCallback listner;

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_indentification,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        initCtrl();
    }

    private void init(View view){
        btnOK = view.findViewById(R.id.btnOK);
        btnRetry = view.findViewById(R.id.btnRetry);
    }
    private void initCtrl(){
        btnOK.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
    }

    public  void setCallBack(CameraCallback listner){
        this.listner = listner;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()){
           case R.id.btnOK : requireActivity().finish(); break;
           case  R.id.btnRetry : listner.restartCamera();  break;
        }
    }

}