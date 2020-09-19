package com.sust.sustcast.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.sust.sustcast.R;
import com.sust.sustcast.databinding.AlertSimpleBinding;

public abstract class SimpleAlertDialog extends Dialog {

    private static final String TAG = "SimpleAlertDialog";
    private AlertSimpleBinding binding;
    private Context context;
    private int iconResource;
    private String buttonText;
    private String alertMessage;
    private boolean cancelable;

    public SimpleAlertDialog(@NonNull Context context, int iconResource, String buttonText, String alertMessage) {
        super(context);
        this.context = context;
        this.alertMessage = alertMessage;
        this.buttonText = buttonText;
        this.iconResource = iconResource;
        this.cancelable = true;
    }

    public SimpleAlertDialog(@NonNull Context context, int iconResource, String buttonText, String alertMessage, boolean cancelable) {
        super(context);
        this.context = context;
        this.alertMessage = alertMessage;
        this.buttonText = buttonText;
        this.iconResource = iconResource;
        this.cancelable = cancelable;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.alert_simple, null, false);
        setContentView(binding.getRoot());

        this.setCancelable(cancelable);


        binding.button.setText(buttonText);
        binding.ivIcon.setImageResource(iconResource);
        binding.tvAlertMessage.setText(alertMessage);

        binding.button.setOnClickListener(view -> {
            buttonAction();
        });
    }

    public abstract void buttonAction();
}
