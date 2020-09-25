package com.sust.sustcast.splash;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sust.sustcast.data.User;

import static com.sust.sustcast.data.Constants.USERS;

public class SplashRepository {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestore.collection(USERS);
    private User user = new User();

    MutableLiveData<User> checkAuthentication() {
        MutableLiveData<User> authenticatedUserMutableLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            user.setAuthenticated(false);
        } else {
            user.uid = firebaseUser.getUid();
            user.setAuthenticated(true);
        }
        authenticatedUserMutableLiveData.setValue(user);
        return authenticatedUserMutableLiveData;
    }

    MutableLiveData<User> userLiveData(String uid) {
        MutableLiveData<User> authenticatedUserMutableLiveData = new MutableLiveData<>();
        collectionReference.document(uid).get().addOnCompleteListener(dataTask -> {
            if (dataTask.isSuccessful()) {
                DocumentSnapshot documentSnapshot = dataTask.getResult();
                assert documentSnapshot != null;
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    authenticatedUserMutableLiveData.setValue(user);
                }
            }
        });
        return authenticatedUserMutableLiveData;
    }
}
