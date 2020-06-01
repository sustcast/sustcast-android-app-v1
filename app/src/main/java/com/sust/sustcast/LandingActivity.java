package com.sust.sustcast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLogin();
            }
        });
        button_sigup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignUp();
            }
        });
    }

    private void startSignUp() {
        startActivity(new Intent(LandingActivity.this, FragmentHolder.class));
        finish();
    }

    private void startLogin() {
        startActivity(new Intent(LandingActivity.this, FragmentHolder.class));
        finish();
    }


}
