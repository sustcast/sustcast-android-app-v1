package com.sust.sustcast.authentication;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sust.sustcast.R;
import com.sust.sustcast.databinding.ActivityTocBinding;
import com.sust.sustcast.utils.FontHelper;

import static com.sust.sustcast.data.Constants.TOC;

public class TocActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTocBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_toc);
        binding.setTocActivity(this);
        binding.tvToc.setText(TOC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FontHelper.adjustFontScale(this, getResources().getConfiguration());
        }

    }
}