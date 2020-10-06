package com.sust.sustcast.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sust.sustcast.R;
import com.sust.sustcast.dialogs.SimpleAlertDialog;
import com.sust.sustcast.utils.FontHelper;

import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

public class FragmentHolder extends AppCompatActivity {

    private static final String TAG = "FragmentHolder";

    BottomNavigationView bottomNavigation;


    private static int RC_APP_UPDATE = 999;
    private Context context;
    private String token;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);
        context = this;
        FontHelper.adjustFontScale(context, getResources().getConfiguration());


        try {
            checkForUpdate();
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        //monitor token generation
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                token = task.getException().getMessage();
                Log.w("FCM TOKEN Fail ", task.getException());
            } else {
                token = task.getResult().getToken();
                Log.i("FCM TOKEN ", token);
            }
        });
        openFragment(StreamFragment.newInstance());

    }

    private void checkForUpdate() {
        AppUpdateManager appUpdateManager;
        Task<AppUpdateInfo> appUpdateInfoTask;

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        InstallStateUpdatedListener installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                SimpleAlertDialog dialog = new SimpleAlertDialog(context, R.drawable.sustcast_logo_circle_only, "install", "update download completed", false) {
                    @Override
                    public void buttonAction() {
                        FragmentHolder.this.finishAffinity();
                        appUpdateManager.completeUpdate();
                        dismiss();
                    }
                };

                dialog.show();
            }
        };

        appUpdateManager.registerListener(installStateUpdatedListener);

        try {
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                    Log.i("FAG", "inAppUpdate: Available");
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                FragmentHolder.this,
                                RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException ignored) {
                        Crashlytics.logException(ignored);
                    }
                } else {
                    Log.i("Update", "updateStatus " + appUpdateInfo.updateAvailability());
                    Log.i("Update", "Type: Immediate " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
                    Log.i("Update", "Type: Flexible " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE));
                    Log.i("Update", "inAppUpdate: Unavailable");
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: App download starts...");
                //Toast.makeText(FragmentHolder.this, "App download starts...", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "onActivityResult: App download cancelled...");
                //Toast.makeText(FragmentHolder.this, "App download canceled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
                Log.i(TAG, "onActivityResult: App download failed....");
                //Toast.makeText(FragmentHolder.this, "App download failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        IsFinish("Want to close app?\nYou can put in on background pressing the Home Button!");

//        FragmentHolder.this.moveTaskToBack(true);
//        FragmentHolder.this.finish();
    }

    public void IsFinish(String alertmessage) {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    android.os.Process.killProcess(android.os.Process.myPid());
                    // This above line close correctly
                    //finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MaterialAlertDialog_OK_color);
        builder.setMessage(alertmessage)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


}
