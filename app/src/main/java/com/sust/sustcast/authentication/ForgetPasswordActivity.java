package com.sust.sustcast.authentication;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.sust.sustcast.R;
import com.sust.sustcast.databinding.ActivityForgetPasswordBinding;
import com.sust.sustcast.utils.FontHelper;
import com.sust.sustcast.utils.StringValidationRules;

import static com.sust.sustcast.data.Constants.CHECKMAIL;
import static com.sust.sustcast.data.Constants.EMPTYMAIL;
import static com.sust.sustcast.data.Constants.FAILMAIL;
import static com.sust.sustcast.data.Constants.INVALIDEMAIL;

public class ForgetPasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityForgetPasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password);
        binding.setForgetPassActivity(this);

        binding.etEmailForget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringValidationRules.EMAIL.validate(editable)) {
                    binding.etEmailForget.setError(INVALIDEMAIL);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FontHelper.adjustFontScale(this, getResources().getConfiguration());
        }
    }

    public void resetPassword(String email) {
        mAuth = FirebaseAuth.getInstance();
        if (!TextUtils.isEmpty(email)) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgetPasswordActivity.this, CHECKMAIL, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgetPasswordActivity.this, FAILMAIL, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
        else{
            Toast.makeText(ForgetPasswordActivity.this, EMPTYMAIL, Toast.LENGTH_SHORT).show();
        }
    }
}