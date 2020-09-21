package com.sust.sustcast.fragment;

import android.graphics.drawable.Drawable;
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

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class NewsReaderFragment extends Fragment {

    private static final String TAG = "NewsReaderFragment";
    boolean isPlaying;
    ExoHelper exoHelper;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;

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
        View rootView = inflater.inflate(R.layout.fragment_news_reader, container, false);
        bPlay = rootView.findViewById(R.id.button_play);
        unbinder = ButterKnife.bind(this, rootView);

        exoHelper = new ExoHelper(getContext(), new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Crashlytics.logException(error);
                Toast.makeText(getContext(), getString(R.string.server_off), Toast.LENGTH_LONG).show();
                stopStream();
            }
        });

        isPlaying = true;
        setButton();
        exoHelper.startExo(getString(R.string.bbc_news));

        return rootView;
    }

    private void toggleButtonState() {
        Log.d(TAG, "splaying -> " + isPlaying);

        if (isPlaying) {
            stopStream();
        } else {
            startStream();
        }
    }

    private void startStream() {

        Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
        bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        bPlay.setText(R.string.now_playing);

        exoHelper.startExo(getString(R.string.bbc_news));

        isPlaying = true;

    }

    private void stopStream() {

        Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
        bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        bPlay.setText(R.string.now_paused);

        exoHelper.stopExo();

        isPlaying = false;
    }

    private void setButton() {

        Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
        bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        bPlay.setText(R.string.now_playing);

        bPlay.setOnClickListener(view -> {
            toggleButtonState();
        });

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        exoHelper.stopExo();
    }


}
