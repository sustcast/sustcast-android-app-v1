package com.sust.sustcast.authentication;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sust.sustcast.R;
import com.sust.sustcast.databinding.ActivityTocBinding;
import com.sust.sustcast.utils.FontHelper;

import static com.sust.sustcast.utils.Constants.TOC;

public class TocActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FontHelper.adjustFontScale(this, getResources().getConfiguration());

        ActivityTocBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_toc);
        binding.setTocActivity(this);
        binding.tvToc.setText(TOC);
    }
}