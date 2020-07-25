package com.sust.sustcast.authentication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.R;
import com.sust.sustcast.data.AuthenticationViewModel;
import com.sust.sustcast.databinding.ActivityLoginBinding;
import com.sust.sustcast.fragment.FragmentHolder;
import com.sust.sustcast.utils.FontHelper;
import com.sust.sustcast.utils.StringValidationRules;

import static com.sust.sustcast.utils.Constants.DATAERROR;
import static com.sust.sustcast.utils.Constants.INVALIDEMAIL;
import static com.sust.sustcast.utils.Constants.INVALIDPASSWORD;
import static com.sust.sustcast.utils.Constants.USERS;

public class LoginActivity extends AppCompatActivity {
    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setLoginActivity(this);
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.EMAIL.validate(editable)) {
                    binding.etEmail.setError(INVALIDEMAIL);
                }
            }
        });
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.PASSWORD.validate(editable)) {
                    binding.etPassword.setError(INVALIDPASSWORD);
                }
            }
        });

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);
        authViewModel.getAuthenticatedUser().observe(this, authenticatedUser -> {
            startActivity(new Intent(LoginActivity.this, FragmentHolder.class).putExtra(USERS, authenticatedUser));
            finish();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FontHelper.adjustFontScale(this, getResources().getConfiguration());
        }

    }

    public void signIn(String email, String password) {
        if (email.length() != 0 && password.length() != 0)
            authViewModel.signIn(email, password);
        else
            Toast.makeText(LoginActivity.this, DATAERROR, Toast.LENGTH_SHORT).show();
    }

    public void startForget() {
        startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
    }
}
