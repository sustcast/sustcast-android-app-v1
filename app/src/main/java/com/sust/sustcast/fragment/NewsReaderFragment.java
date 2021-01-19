package com.sust.sustcast.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.sust.sustcast.R;
import com.sust.sustcast.utils.ExoHelper;
import com.sust.sustcast.utils.NetworkUtil;


import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.sust.sustcast.data.Constants.CHECKNET;
import static com.sust.sustcast.data.Constants.SERVEROFF;


public class NewsReaderFragment extends Fragment {

    boolean isPlaying;
    ExoHelper exoHelper;
    private Unbinder unbinder;
    private Button bPlay;
    View rootView;
    private static final String TAG = "NewsReader";
    private BroadcastReceiver receiver;
    public String PLAY = "com.sust.sustcast.PLAY";
    public String PAUSE = "com.sust.sustcast.PAUSE";
    public String NOINTERNET = "com.sust.sustcast.NOINTERNET";

    public NewsReaderFragment() {
    }

    public static NewsReaderFragment newInstance() {
        return new NewsReaderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_news_reader, container, false);
        bPlay = rootView.findViewById(R.id.button_play);
        unbinder = ButterKnife.bind(this, rootView);

        NetworkUtil.checkNetworkInfo(rootView.getContext(), type -> {
            if (!type) {
                Log.d(TAG, "onCreateView: " + "No internet");
                Toast.makeText(rootView.getContext(), CHECKNET, Toast.LENGTH_LONG).show();
                Intent noInternet = new Intent(NOINTERNET).setPackage(rootView.getContext().getPackageName());
                rootView.getContext().sendBroadcast(noInternet);
            }
        });


        exoHelper = new ExoHelper(getContext(), new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                //Crashlytics.logException(error);

                if (!(getContext() == null)) {
                    Toast.makeText(getContext(), SERVEROFF, Toast.LENGTH_LONG).show();
                    exoHelper.ToggleButton(false);
                    exoHelper.StopNotification();
                } else {
                    Log.d(TAG, "onPlayerError: " + "Context is null");
                }

            }
        }, bPlay, "NewsReader");

        isPlaying = true;
        setButton();
        exoHelper.startExo(rootView.getContext().getString(R.string.bbc_news));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(PLAY);
        intentFilter.addAction(NOINTERNET);

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!(intent.getAction() == null)) {
                        if (intent.getAction().equals(PAUSE)) {
                            Log.d(TAG, "onReceive: " + "Paused");
                            exoHelper.ToggleButton(false);

                        } else if (intent.getAction().equals(PLAY)) {
                            Log.d(TAG, "onReceive: " + "Playing");
                            exoHelper.ToggleButton(true);
                        }
                        else if (intent.getAction().equals(NOINTERNET)) {
                            exoHelper.ToggleButton(false);
                            exoHelper.stopExo();
                        }
                    } else {
                        Log.d(TAG, "onReceive: " + "Nothing received!");
                    }

                }
            };

            getActivity().registerReceiver(receiver, intentFilter);

        } else {
            Log.d(TAG, "onStart: " + "Receiver already registered");
        }


    }

    private void setButton() {
        bPlay.setOnClickListener(view -> {

            Log.d(TAG, "setButton: " + isPlaying);


            if (isPlaying) {
                exoHelper.startExo(rootView.getContext().getString(R.string.bbc_news));
            } else {
                exoHelper.stopExo();
            }

            isPlaying = !isPlaying;
        });

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        exoHelper.stopExo();


        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            Crashlytics.logException(exception);
        }


    }


}
