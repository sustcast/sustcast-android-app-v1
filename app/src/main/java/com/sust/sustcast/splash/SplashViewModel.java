package com.sust.sustcast.splash;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.sust.sustcast.data.User;

public class SplashViewModel extends AndroidViewModel {
    LiveData<User> checkAuthenticationLiveData;
    LiveData<User> userData;
    private SplashRepository splashRepository;

    public SplashViewModel(Application application) {
        super(application);
        splashRepository = new SplashRepository();
    }

    void checkAuthentication() {
        checkAuthenticationLiveData = splashRepository.checkAuthentication();
    }

    void getData(String uid) {
        userData = splashRepository.userLiveData(uid);
    }
}
