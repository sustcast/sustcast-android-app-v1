package com.sust.sustcast.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthenticationViewModel extends AndroidViewModel {
    public LiveData<User> authenticatedUserLiveData;
    private AuthenticationRepository authRepo;

    public AuthenticationViewModel(@NonNull Application application) {
        super(application);
        authRepo = new AuthenticationRepository();
        authenticatedUserLiveData = new MutableLiveData<>();
    }

    public void signIn(String emailAddress, String password) {
        authenticatedUserLiveData = authRepo.firebaseSignIn(emailAddress, password);
    }

    public void signUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
        authenticatedUserLiveData = authRepo.firebaseSignUp(userName, emailAddress, password, phoneNumber, department);
    }
}
