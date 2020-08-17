package com.sust.sustcast.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.sust.sustcast.utils.Constants.USERS;

public abstract class AuthenticationRepository {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = firebaseFirestore.collection(USERS);

    abstract void setUser(User user);
    abstract void setSignError(Boolean status);

//    abstract void setVerificationStatus(Boolean status);

    void firebaseSignIn(String emailAddress, String password) {
        firebaseAuth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//                if (firebaseUser.isEmailVerified()) {
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        collectionReference.document(uid).get().addOnCompleteListener(dataTask -> {
                            if (dataTask.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = dataTask.getResult();
                                assert documentSnapshot != null;
                                if (documentSnapshot.exists()) {
                                    setSignError(false);
                                    User user = documentSnapshot.toObject(User.class);
                                    assert user != null;
                                    user.setAuthenticated(true);
                                    setUser(user);
                                }
                            }
                        });
                    }
//                } else {
//                    setSignError(true);
//                    firebaseAuth.signOut();
//                }
            } else
                setSignError(true);
        });
    }

    void firebaseSignUp(String userName, String emailAddress, String password, String phoneNumber, String department) {
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
//                                    setVerificationStatus(true);
//                                    Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification();
//                                    firebaseAuth.signOut();
                                    user.setAuthenticated(true);
                                    setUser(user);
                                }
                            });
                        }
                    }
                });
            } else setSignError(true);
        });
    }
}
