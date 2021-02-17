package com.sust.sustcast.authentication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.R;
import com.sust.sustcast.data.AuthenticationViewModel;
import com.sust.sustcast.databinding.ActivityLoginBinding;
import com.sust.sustcast.fragment.FragmentHolder;
import com.sust.sustcast.utils.FontHelper;

import static com.sust.sustcast.data.Constants.DATAERROR;
import static com.sust.sustcast.data.Constants.LOGINERROR;
import static com.sust.sustcast.data.Constants.USERS;

public class LoginActivity extends AppCompatActivity {
    public ObservableBoolean visibility = new ObservableBoolean(false);
    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setLoginActivity(this);


        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);
        authViewModel.getAuthenticatedUser().observe(this, authenticatedUser -> {
            startActivity(new Intent(LoginActivity.this, FragmentHolder.class).putExtra(USERS, authenticatedUser));
            finish();
        });

        authViewModel.getSignError().observe(this, errorObserver -> {
            if (errorObserver) {
                visibility.set(false);
                Toast.makeText(LoginActivity.this, LOGINERROR, Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FontHelper.adjustFontScale(this, getResources().getConfiguration());
        }

    }

    public void signIn(String email, String password) {

        if (email.length() != 0 && password.length() != 0 && password.length() > 5) {
            visibility.set(true);
            authViewModel.signIn(email, password);
        } else {
            Toast.makeText(LoginActivity.this, DATAERROR, Toast.LENGTH_SHORT).show();
        }
    }

    public void startForget() {
        startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
    }
}
