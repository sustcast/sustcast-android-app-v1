package com.sust.sustcast.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ConnectionLiveData extends LiveData<Boolean> {

    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback connectivityManagerCallback;
    public static final String TAG = "ConnectionLiveData";

    private final NetworkRequest.Builder networkRequestBuilder;


    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(getConnectivityMarshmallowManagerCallback());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            marshmallowNetworkAvailableRequest();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lollipopNetworkAvailableRequest();
        }
    }


    @Override
    protected void onInactive() {
        super.onInactive();

        /*
         *
         * It is necessary to clean up the Callbacks.
         *
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                connectivityManager.unregisterNetworkCallback(connectivityManagerCallback);
            } catch (Exception exception) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
        }

    }

    public ConnectionLiveData(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkRequestBuilder = new NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI);


    }

    private ConnectivityManager.NetworkCallback getConnectivityMarshmallowManagerCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManagerCallback = new ConnectivityManager.NetworkCallback() {


                @Override
                public void onLost(@NonNull Network network) {
                    postValue(false);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        postValue(true);
                    }
                }
            };
            return connectivityManagerCallback;
        } else {
            throw new IllegalAccessError("Accessing wrong API version");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void lollipopNetworkAvailableRequest() {
        connectivityManager.registerNetworkCallback(networkRequestBuilder.build(), getConnectivityLollipopManagerCallback());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void marshmallowNetworkAvailableRequest() {
        connectivityManager.registerNetworkCallback(networkRequestBuilder.build(), getConnectivityMarshmallowManagerCallback());
    }

    private ConnectivityManager.NetworkCallback getConnectivityLollipopManagerCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManagerCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    postValue(false);
                }
            };
            return connectivityManagerCallback;
        } else {
            throw new IllegalAccessError("Accessing wrong API version");
        }
    }

    private void updateConnection() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        postValue(activeNetwork != null && activeNetwork.isConnected());
    }


}
