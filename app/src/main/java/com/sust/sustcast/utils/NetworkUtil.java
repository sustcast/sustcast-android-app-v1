package com.sust.sustcast.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;

public class NetworkUtil {

    public static void checkNetworkInfo(Context context, final OnConnectionStatusChange onConnectionStatusChange) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null) {
                onConnectionStatusChange.onChange(false);
            }
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    onConnectionStatusChange.onChange(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    onConnectionStatusChange.onChange(false);
                }
            });

        }
        //for android version below Nougat api 24
        else {

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            onConnectionStatusChange.onChange(networkInfo != null && networkInfo.isConnectedOrConnecting());
        }
    }

    public interface OnConnectionStatusChange {

        void onChange(boolean type);
    }
}