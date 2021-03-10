package com.sust.sustcast.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.sust.sustcast.R;
import com.sust.sustcast.services.RadioService;
import com.sust.sustcast.utils.ConnectionLiveData;


import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.sust.sustcast.data.Constants.CHECKNET;
import static com.sust.sustcast.data.Constants.ERROR;
import static com.sust.sustcast.data.Constants.NO_INTERNET;
import static com.sust.sustcast.data.Constants.PAUSE;
import static com.sust.sustcast.data.Constants.PLAY;
import static com.sust.sustcast.data.Constants.SERVEROFF;


public class NewsReaderFragment extends Fragment {

    boolean isPlaying = true;
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


        ConnectionLiveData connectionLiveData = new ConnectionLiveData(rootView.getContext());
        connectionLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                Log.d(TAG, "onCreateView: " + "No internet");
                Toast.makeText(rootView.getContext(), CHECKNET, Toast.LENGTH_LONG).show();

                Intent pauseIntent = new Intent(NO_INTERNET).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(pauseIntent);
                isPlaying = false;
                ToggleButton(false);
            }
        });

        setButton();

        Intent intent = new Intent(rootView.getContext(), RadioService.class);
        intent.putExtra("url", rootView.getContext().getString(R.string.bbc_news));
        rootView.getContext().startService(intent);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(PLAY);
        intentFilter.addAction(ERROR);

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!(intent.getAction() == null)) {
                        if (intent.getAction().equals(PAUSE)) {
                            ToggleButton(false);
                            isPlaying = false;
                        } else if (intent.getAction().equals(PLAY)) {
                            ToggleButton(true);
                            isPlaying = true;
                        } else if (intent.getAction().equals(ERROR)) {
                            ToggleButton(false);
                            Toast.makeText(context, SERVEROFF, Toast.LENGTH_SHORT).show();
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


    public void ToggleButton(boolean state) {
        if (state) {
            Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
            bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            bPlay.setText(R.string.now_playing);
        } else {
            Drawable img1 = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
            bPlay.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
            bPlay.setText(R.string.server_off);
        }
    }

    private void setButton() {
        bPlay.setOnClickListener(view -> {

            Log.d(TAG, "setButton: " + isPlaying);

            Intent intent = new Intent(getContext(), RadioService.class);
            if (!isPlaying) {

                /*
                Intent playIntent = new Intent(PLAY).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(playIntent);
                 */

                intent.putExtra("url", getContext().getString(R.string.bbc_news));
                getContext().startService(intent);
                ToggleButton(true);
                isPlaying = true;
            } else {

                /*
                Intent pauseIntent = new Intent(PAUSE).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(pauseIntent);
                 */

                getContext().stopService(intent);
                ToggleButton(false);
                isPlaying = false;
            }

        });

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        Intent intent = new Intent(getContext(), RadioService.class);
        getContext().stopService(intent);


        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            FirebaseCrashlytics.getInstance().recordException(exception);
        }


    }


}
