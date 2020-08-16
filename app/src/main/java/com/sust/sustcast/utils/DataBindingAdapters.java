package com.sust.sustcast.utils;

import android.os.Build;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.databinding.BindingAdapter;

public class DataBindingAdapters {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @BindingAdapter("android:animatedVisibility")
    public static void setAnimatedVisibility(View view, boolean isVisible) {
        TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView());
        if (isVisible)
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.GONE);
    }
}
