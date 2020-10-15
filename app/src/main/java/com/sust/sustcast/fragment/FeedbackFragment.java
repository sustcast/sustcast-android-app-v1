package com.sust.sustcast.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sust.sustcast.LandingActivity;
import com.sust.sustcast.R;
import com.sust.sustcast.data.Config;
import com.sust.sustcast.databinding.FragmentFeedbackBinding;

import java.util.Objects;

import static com.sust.sustcast.data.Constants.MAILBODY;
import static com.sust.sustcast.data.Constants.MAILERROR;


public class FeedbackFragment extends Fragment implements FirebaseAuth.AuthStateListener {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference configRef;
    private ValueEventListener vListener;
    private String FACEBOOK_PAGE_ID;
    private String FACEBOOK_PAGE_LINK;
    private String MAILADDRESS;
    private String MAILSUBJECT;

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
        configRef = FirebaseDatabase.getInstance().getReference().child("config");
        getConfig();

        MAILADDRESS = Objects.requireNonNull(getActivity()).getString(R.string.mail_address);
        binding.setFeedbackFragment(this);
        return binding.getRoot();
    }

    public void giveRate() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sust.sustcast")));

    }

    public Intent visitFacebook() {
        try {

            int versionCode = Objects.requireNonNull(getContext()).getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) {
                //newer versions of fb app
                return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + FACEBOOK_PAGE_LINK));
            } else { //older versions of fb app
                return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + FACEBOOK_PAGE_ID));
            }

        } catch (PackageManager.NameNotFoundException e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_PAGE_LINK));
        }


//            getContext().getPackageManager().getPackageInfo(FACEBOOKAPP, 0);
//            return new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOKAPPLINK));
//        } catch (Exception e) {
//            return new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOKLINK));
//        }
    }

    private void getConfig() {
        vListener = configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Config config = dataSnapshot.getValue(Config.class);
                FACEBOOK_PAGE_ID = config.getPage_id();
                FACEBOOK_PAGE_LINK = config.getPage_link();
                MAILSUBJECT = config.getMail_subject();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void onDestroyView() {
        super.onDestroyView();
        configRef.removeEventListener(vListener);

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
