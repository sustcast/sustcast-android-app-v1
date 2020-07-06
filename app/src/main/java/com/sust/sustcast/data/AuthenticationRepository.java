package com.sust.sustcast.data;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.sust.sustcast.utils.Constants.USERS;

public class AuthenticationRepository {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestore.collection(USERS);

    MutableLiveData<User> firebaseSignIn(String emailAddress, String password) {
        MutableLiveData<User> authenticatedUserMutableLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    collectionReference.document(uid).get().addOnCompleteListener(dataTask -> {
                        if (dataTask.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = dataTask.getResult();
                            assert documentSnapshot != null;
                            if (documentSnapshot.exists()) {
                                User user = documentSnapshot.toObject(User.class);
                                user.setAuthenticated(true);
                                authenticatedUserMutableLiveData.setValue(user);
                            }
                        }
                    });
                }
            }
        });
        return authenticatedUserMutableLiveData;
    }

    MutableLiveData<User> firebaseSignUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
        MutableLiveData<User> authenticatedUserMutableLiveData = new MutableLiveData<>();
        User user = new User(userName, emailAddress, phoneNumber, department);
        firebaseAuth.createUserWithEmailAndPassword(emailAddress, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                user.uid = Objects.requireNonNull(Objects.requireNonNull(authTask.getResult()).getUser()).getUid();
                DocumentReference documentReference = collectionReference.document(user.uid);
                documentReference.get().addOnCompleteListener(dataTask -> {
                    if (dataTask.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = dataTask.getResult();
                        assert documentSnapshot != null;
                        if (!documentSnapshot.exists()) {
                            documentReference.set(user).addOnCompleteListener(userCreationTask -> {
                                if (userCreationTask.isSuccessful()) {
                                    authenticatedUserMutableLiveData.setValue(user);
                                }
                            });
                        }
                    }
                });
            }
        });
        return authenticatedUserMutableLiveData;
    }
}