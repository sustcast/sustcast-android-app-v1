package com.sust.sustcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


public class FeedbackFragment extends Fragment {
    private Button bFeedback;
    private Button bFacebook;
    private Button bRate;
    private Button bLogOut;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    public static FeedbackFragment newInstance() {
        FeedbackFragment fragment = new FeedbackFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
        bFeedback = rootView.findViewById(R.id.feedback_button);
        bFacebook = rootView.findViewById(R.id.facebook_button);
        bRate = rootView.findViewById(R.id.rate_button);
        bLogOut = rootView.findViewById(R.id.logout_button);

        bFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveFeedback();
            }
        });

        bFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(visitFacebook());
            }
        });

        bRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveRate();
            }
        });

        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
        return rootView;
    }

    private void logOut() {
    }

    private void giveRate() {
    }

    private Intent visitFacebook() {

        try {
            getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/426253597411506"));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/appetizerandroid"));
        }
    }

    private void giveFeedback() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + "sustcast@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My email's subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "My email's body");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
