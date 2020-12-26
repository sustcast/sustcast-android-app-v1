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
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sust.sustcast.R;
import com.sust.sustcast.utils.ExoHelper;
import com.sust.sustcast.utils.NetworkInfoUtility;


import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.sust.sustcast.data.Constants.CHECKNET;


public class NewsReaderFragment extends Fragment {

    boolean isPlaying;
    ExoHelper exoHelper;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
    View rootView;
    private static final String TAG = "NewsReader";
    private BroadcastReceiver receiver;

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
        NetworkInfoUtility networkInfoUtility = new NetworkInfoUtility();
        boolean net = networkInfoUtility.isNetWorkAvailableNow(getContext());
        if (!net) {
            Toast.makeText(getContext(), CHECKNET, Toast.LENGTH_LONG).show();
        }
        exoHelper = new ExoHelper(getContext(), new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Crashlytics.logException(error);
                Toast.makeText(getContext(), rootView.getContext().getString(R.string.server_off), Toast.LENGTH_LONG).show();
                exoHelper.ToggleButton(false);
                exoHelper.StopNotification();
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
        intentFilter.addAction("Paused");
        intentFilter.addAction("Playing");

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals("Paused")) {
                        Log.d(TAG, "onReceive: " + "Paused");
                        exoHelper.ToggleButton(false);

                    } else if (intent.getAction().equals("Playing")) {
                        Log.d(TAG, "onReceive: " + "Playing");
                        exoHelper.ToggleButton(true);
                    } else {
                        Log.d(TAG, "onReceive: " + "Nothing received!");
                    }


                }
            };

        } else {
            Log.d(TAG, "onStart: " + "Receiver already registered");
        }


        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void setButton() {
        bPlay.setOnClickListener(view -> {

            Log.d(TAG, "setButton: " + isPlaying);


            if (!isPlaying) {
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
                Log.d(TAG, "onDestroyView");
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            Crashlytics.logException(exception);
        }


    }


}
