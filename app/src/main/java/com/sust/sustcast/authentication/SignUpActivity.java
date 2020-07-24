package com.sust.sustcast.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sust.sustcast.R;
import com.sust.sustcast.data.AuthenticationViewModel;
import com.sust.sustcast.databinding.ActivitySignUpBinding;
import com.sust.sustcast.fragment.FragmentHolder;
import com.sust.sustcast.utils.StringValidationRules;

import static com.sust.sustcast.utils.Constants.DATAERROR;
import static com.sust.sustcast.utils.Constants.DEPARTMENTS;
import static com.sust.sustcast.utils.Constants.INVALIDEMAIL;
import static com.sust.sustcast.utils.Constants.INVALIDNAME;
import static com.sust.sustcast.utils.Constants.INVALIDPASSWORD;
import static com.sust.sustcast.utils.Constants.INVALIDPHONE;
import static com.sust.sustcast.utils.Constants.USERS;

public class SignUpActivity extends AppCompatActivity {
    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySignUpBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.dropdown_department_items, DEPARTMENTS);

        binding.setSignUpActivity(this);
        binding.Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.EMAIL.validate(editable)) {
                    binding.Email.setError(INVALIDEMAIL);
                }
            }
        });
        binding.Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.PASSWORD.validate(editable)) {
                    binding.Password.setError(INVALIDPASSWORD);
                }
            }
        });
        binding.Name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.NOT_EMPTY.validate(editable)) {
                    binding.Name.setError(INVALIDNAME);
                }
            }
        });
        binding.PhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.PHONE.validate(editable)) {
                    binding.PhoneNumber.setError(INVALIDPHONE);
                }
            }
        });
        binding.Department.setAdapter(departmentAdapter);
        binding.tocText.setMovementMethod(LinkMovementMethod.getInstance());

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
