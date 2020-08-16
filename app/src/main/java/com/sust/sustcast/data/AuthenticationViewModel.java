package com.sust.sustcast.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthenticationViewModel extends AndroidViewModel {
    public MutableLiveData<User> authenticatedUserLiveData;
    public MutableLiveData<Boolean> authenticationError;
    private AuthenticationRepository authRepo;

    public AuthenticationViewModel(@NonNull Application application) {
        super(application);
        authenticatedUserLiveData = new MutableLiveData<>();
        authenticationError = new MutableLiveData<>();
        authRepo = new AuthenticationRepository() {
            @Override
            void setUser(User user) {
                authenticatedUserLiveData.setValue(user);
            }

            @Override
            void setSignError(Boolean status) {
                authenticationError.setValue(status);
            }
        };
    }

    public void signIn(String emailAddress, String password) {
        authRepo.firebaseSignIn(emailAddress, password);
    }

    public void signUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
        authRepo.firebaseSignUp(userName, emailAddress, password, phoneNumber, department);
    }

    public LiveData<Boolean> getSignError() {
        return authenticationError;
    }

    public LiveData<User> getAuthenticatedUser() {
        return authenticatedUserLiveData;
    }
}
