package com.sust.sustcast.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.LandingActivity;
import com.sust.sustcast.fragment.FragmentHolder;

public class SplashActivity extends AppCompatActivity {
    SplashViewModel splashViewModel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        splashViewModel.checkAuthentication();
        splashViewModel.checkAuthenticationLiveData.observe(this, user -> {
            if (!user.getAuthenticated()) {
                startActivity(new Intent(SplashActivity.this, LandingActivity.class));
                finish();
            } else {
                splashViewModel.getData(user.uid);
                splashViewModel.userData.observe(this, userData -> {
                    startActivity(new Intent(SplashActivity.this, FragmentHolder.class));
                    finish();
                });
            }
        });


    }
}
