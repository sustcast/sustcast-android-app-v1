package com.sust.sustcast.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sust.sustcast.R;

public class ExoHelper {
    private static final String TAG = "ExoHelper";
    private String iceURL;
    private Context context;

    private SimpleExoPlayer exoPlayer;
    private Player.EventListener eventListener;
    private Button button;
    public ExoHelper(Context context) {
        this.context = context;
    }

    public ExoHelper(Context context, Player.EventListener eventListener, Button button) {
        if (exoPlayer != null) {
            return;
        }
        this.context = context;
        this.eventListener = eventListener;
        this.button = button;
    }


    public void stopExo() {
        if (exoPlayer != null) { //if exo is running
            Log.d(TAG, "Stopping exo....");
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            ToggleButton(false);
        } else {
            Log.d(TAG, "Can't stop because No exoplayer is running");
        }
    }

    public void startExo(String newUrl) {
        if (newUrl == null || newUrl.isEmpty()) {
            Log.d(TAG, "startExo: empty url");
            Toast.makeText(context, R.string.server_off, Toast.LENGTH_SHORT).show();
            ToggleButton(false); // show pause button
            return;
        }

        if (exoPlayer != null) {
            Log.d(TAG, "startExo: Exo is already running now");
            return;
        }

        iceURL = newUrl;

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory trackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector defaultTrackSelector =
                new DefaultTrackSelector(trackSelectionFactory);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getApplicationInfo().name),
                defaultBandwidthMeter);

        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(iceURL),
                dataSourceFactory,
                extractorsFactory,
                new Handler(), error -> {
        });

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, defaultTrackSelector);

        if(eventListener != null){
            exoPlayer.addListener(eventListener);
        }
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        ToggleButton(true);

    }

    public SimpleExoPlayer getPlayer() {
        return exoPlayer;
    }


    public void ToggleButton(boolean state) {
        if (state == true) {
            Drawable img = button.getContext().getResources().getDrawable(R.drawable.play_button);
            button.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            button.setText(R.string.now_playing);
        } else {
            Drawable img1 = button.getContext().getResources().getDrawable(R.drawable.pause_button);
            button.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
            button.setText(R.string.server_off);
        }
    }
}
