package com.sust.sustcast.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.FragmentHolder;
import com.sust.sustcast.R;
import com.sust.sustcast.data.AuthenticationViewModel;
import com.sust.sustcast.databinding.ActivityLoginBinding;

import static com.sust.sustcast.utils.Constants.DATAERROR;
import static com.sust.sustcast.utils.Constants.USERS;

public class LoginActivity extends AppCompatActivity {
    private AuthenticationViewModel authViewModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setLoginActivity(this);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);
        authViewModel.getAuthenticatedUser().observe(this, authenticatedUser -> {
            startActivity(new Intent(LoginActivity.this, FragmentHolder.class).putExtra(USERS, authenticatedUser));
            finish();
        });
    }

    public void signIn(String email, String password) {
        if (email.length() != 0 && password.length() != 0)
            authViewModel.signIn(email, password);
        else
            Toast.makeText(LoginActivity.this, DATAERROR, Toast.LENGTH_SHORT).show();
    }
}
