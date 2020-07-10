package com.sust.sustcast.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sust.sustcast.R;

public class FragmentHolder extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_stream_frag:
                        openFragment(StreamFragment.newInstance());
                        return true;
                    case R.id.nav_news_frag:
                        openFragment(NewsReaderFragment.newInstance());
                        return true;
                    case R.id.nav_feedback_frag:
                        openFragment(FeedbackFragment.newInstance());
                        return true;
                }
                return false;
            };


    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(StreamFragment.newInstance());

    }
}
