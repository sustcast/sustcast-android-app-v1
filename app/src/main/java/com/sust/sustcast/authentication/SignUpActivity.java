package com.sust.sustcast.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.R;
import com.sust.sustcast.data.AuthenticationViewModel;
import com.sust.sustcast.databinding.ActivitySignUpBinding;
import com.sust.sustcast.fragment.FragmentHolder;

import static com.sust.sustcast.utils.Constants.DATAERROR;
import static com.sust.sustcast.utils.Constants.USERS;

public class SignUpActivity extends AppCompatActivity {
    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySignUpBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        binding.setSignUpActivity(this);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        authViewModel.getAuthenticatedUser().observe(this, authenticatedUser -> {
            startActivity(new Intent(SignUpActivity.this, FragmentHolder.class).putExtra(USERS, authenticatedUser));
            finish();
        });
    }

    public void signUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
        if (userName.length() != 0 && password.length() != 0 && emailAddress.length() != 0 && phoneNumber.length() != 0 && department.length() != 0)
            authViewModel.signUp(userName, emailAddress, password, phoneNumber, department);
        else
            Toast.makeText(SignUpActivity.this, DATAERROR, Toast.LENGTH_SHORT).show();
    }
}
