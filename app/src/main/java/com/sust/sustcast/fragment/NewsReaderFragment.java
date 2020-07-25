package com.sust.sustcast.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sust.sustcast.R;
import com.sust.sustcast.utils.ExoHelper;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class NewsReaderFragment extends Fragment {

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
        exoHelper = new ExoHelper(getContext());
        isPlaying = false;
        setButton();
        if (exoPlayer == null) {
            exoPlayer = exoHelper.startExo(getString(R.string.bbc_news));
        }
        return rootView;
    }

    private void setButton() {
        bPlay.setOnClickListener(view -> {
            if (isPlaying == false) { // should stop

                if (exoPlayer != null) {
                    isPlaying = !isPlaying;
                    exoPlayer = exoHelper.stopExo(bPlay);
                }


            } else {
                isPlaying = !isPlaying;
                if (exoPlayer == null) {
                    exoPlayer = exoHelper.startExo(getString(R.string.bbc_news));
                }
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                bPlay.setText(R.string.now_playing);
            }


        });

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        // Release the player when it is not needed
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }


}
