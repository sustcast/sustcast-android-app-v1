package com.sust.sustcast.fragment;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.sust.sustcast.R;
import com.sust.sustcast.utils.FontHelper;

import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

public class FragmentHolder extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    private AppUpdateManager appUpdateManager;
    private int RC_APP_UPDATE = 999;
    private int inAppUpdateType;
    private com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask;
    private InstallStateUpdatedListener installStateUpdatedListener;

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

    //    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        FontHelper.adjustFontScale(this, getResources().getConfiguration());

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        inAppUpdateType = AppUpdateType.IMMEDIATE;
        inAppUpdate();

        installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        };
        appUpdateManager.registerListener(installStateUpdatedListener);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(StreamFragment.newInstance());

    }

    private void popupSnackbarForCompleteUpdate() {
        try {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.fragment_container_view_tag), "An Update has been downloaded.\nRestart to update.", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Install", view -> {
                if (appUpdateManager != null) {
                    appUpdateManager.completeUpdate();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
            snackbar.show();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        try {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                inAppUpdateType,
                                this,
                                RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(FragmentHolder.this, "App download starts...", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(FragmentHolder.this, "App download canceled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
                Toast.makeText(FragmentHolder.this, "App download failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void inAppUpdate() {
        try {
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(inAppUpdateType)) {
                    Log.e("FAG", "inAppUpdate: Available");
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                inAppUpdateType,
                                FragmentHolder.this,
                                RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                } else {
                    Log.e("Update", "updateStatus " + appUpdateInfo.updateAvailability());
                    Log.e("Update", "Type: Immediate " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
                    Log.e("Update", "Type: Flexible " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE));
                    Log.e("Update", "inAppUpdate: Unavailable");
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentHolder.this.moveTaskToBack(true);
        FragmentHolder.this.finish();
    }
}
