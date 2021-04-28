package com.sust.sustcast;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sust.sustcast.authentication.LoginActivity;
import com.sust.sustcast.authentication.SignUpActivity;
import com.sust.sustcast.databinding.ActivityLandingBinding;
import com.sust.sustcast.utils.ConnectionLiveData;
import com.sust.sustcast.utils.FontHelper;
import com.sust.sustcast.utils.FirebaseMessageReceiver;

import static com.sust.sustcast.data.Constants.CHECKNET;

public class LandingActivity extends AppCompatActivity {

    private static final String TAG = "LandingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLandingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_landing);
        binding.setLandingActivity(this);

        ConnectionLiveData connectionLiveData = new ConnectionLiveData(this);
        connectionLiveData.observe(this, aBoolean -> {
            if (!aBoolean) {
                Log.d(TAG,  "No internet");
                Toast.makeText(getApplicationContext(), CHECKNET, Toast.LENGTH_LONG).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FontHelper.adjustFontScale(this, getResources().getConfiguration());
        }
    }

    public void startSignUp() {
        startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
    }

    public void startLogin() {
        startActivity(new Intent(LandingActivity.this, LoginActivity.class));
    }

}
