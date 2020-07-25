package com.sust.sustcast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sust.sustcast.authentication.LoginActivity;
import com.sust.sustcast.authentication.SignUpActivity;
import com.sust.sustcast.databinding.ActivityLandingBinding;

import java.net.InetAddress;

import static com.sust.sustcast.utils.Constants.CHECKNET;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLandingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_landing);
        binding.setLandingActivity(this);

        if (!isInternetAvailable()) {
            Toast.makeText(LandingActivity.this, CHECKNET, Toast.LENGTH_LONG).show();
        }
    }

    public void startSignUp() {
        startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
    }

    public void startLogin() {
        startActivity(new Intent(LandingActivity.this, LoginActivity.class));
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}
