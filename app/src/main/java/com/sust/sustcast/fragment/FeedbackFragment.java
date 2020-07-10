package com.sust.sustcast.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sust.sustcast.LandingActivity;
import com.sust.sustcast.R;
import com.sust.sustcast.databinding.FragmentFeedbackBinding;

import static com.sust.sustcast.utils.Constants.FACEBOOKAPP;
import static com.sust.sustcast.utils.Constants.FACEBOOKAPPLINK;
import static com.sust.sustcast.utils.Constants.FACEBOOKLINK;
import static com.sust.sustcast.utils.Constants.MAILADDRESS;
import static com.sust.sustcast.utils.Constants.MAILBODY;
import static com.sust.sustcast.utils.Constants.MAILERROR;
import static com.sust.sustcast.utils.Constants.MAILSUBJECT;


public class FeedbackFragment extends Fragment implements FirebaseAuth.AuthStateListener {
    private FirebaseAuth firebaseAuth;

    public FeedbackFragment() {
    }

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentFeedbackBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feedback, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        binding.setFeedbackFragment(this);
        return binding.getRoot();
    }

    public void giveRate() {
    }

    public Intent visitFacebook() {
        try {
            getContext().getPackageManager().getPackageInfo(FACEBOOKAPP, 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOKAPPLINK));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOKLINK));
        }
    }

    public void giveFeedback() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + MAILADDRESS));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, MAILSUBJECT);
        emailIntent.putExtra(Intent.EXTRA_TEXT, MAILBODY);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), MAILERROR, Toast.LENGTH_SHORT).show();
        }
    }

    public void logOut() {
        firebaseAuth.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(getContext(), LandingActivity.class));
            getActivity().finish();
        }
    }
}
