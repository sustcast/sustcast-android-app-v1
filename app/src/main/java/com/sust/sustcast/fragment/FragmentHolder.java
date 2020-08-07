package com.sust.sustcast.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sust.sustcast.R;
import com.sust.sustcast.utils.FontHelper;

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
        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        FontHelper.adjustFontScale(this, getResources().getConfiguration());

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(StreamFragment.newInstance());

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        FragmentHolder.this.finish();
    }
}
