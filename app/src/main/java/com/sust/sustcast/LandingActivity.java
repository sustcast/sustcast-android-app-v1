package com.sust.sustcast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.sust.sustcast.authentication.LoginActivity;
import com.sust.sustcast.authentication.SignUpActivity;

public class LandingActivity extends AppCompatActivity {
    String TAG = "LandingActivity";
    private Button button_login;
    private Button button_sigup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        button_login = findViewById(R.id.tvLogin);
        button_sigup = findViewById(R.id.tvSignUp);

        button_login.setOnClickListener(view -> startLogin());
        button_sigup.setOnClickListener(view -> startSignUp());
    }

    private void startSignUp() {
        startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
    }

    private void startLogin() {
        startActivity(new Intent(LandingActivity.this, LoginActivity.class));
    }


}
