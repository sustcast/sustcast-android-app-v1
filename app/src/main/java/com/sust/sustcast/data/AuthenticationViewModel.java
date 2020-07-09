package com.sust.sustcast.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AuthenticationViewModel extends AndroidViewModel {
    public MutableLiveData<User> authenticatedUserLiveData;
    private AuthenticationRepository authRepo;

    public AuthenticationViewModel(@NonNull Application application) {
        super(application);
        authenticatedUserLiveData = new MutableLiveData<>();
        authRepo = new AuthenticationRepository() {
            @Override
            void setUser(User user) {
                authenticatedUserLiveData.setValue(user);
            }
        };
    }

    public void signIn(String emailAddress, String password) {
        authRepo.firebaseSignIn(emailAddress, password);
    }

    public void signUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
        authRepo.firebaseSignUp(userName, emailAddress, password, phoneNumber, department);
    }

    public LiveData<User> getAuthenticatedUser() {
        return authenticatedUserLiveData;
    }
}
