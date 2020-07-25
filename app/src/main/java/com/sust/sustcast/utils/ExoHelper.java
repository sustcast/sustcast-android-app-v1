package com.sust.sustcast.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
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
    String iceURL;
    Context context;
    private SimpleExoPlayer exoPlayer;

    public ExoHelper(Context context) {
        this.context = context;
    }


    public SimpleExoPlayer stopExo(Button bPlay) {
        if (exoPlayer != null) { //if exo is running
            System.out.println("Stopping exo....");
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
            bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            bPlay.setText(R.string.now_paused);
        } else {
            System.out.println("Can't stop because No exoplayer is running");
        }

        return exoPlayer;

    }

    public SimpleExoPlayer startExo(String newUrl) {
        if (newUrl.isEmpty()) {
            Toast.makeText(context, R.string.server_full, Toast.LENGTH_SHORT).show();
        }

        if (exoPlayer != null) {
            System.out.println("Exo is already running now");
            return null;
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
                Util.getUserAgent(context, "SUSTcast"),
                defaultBandwidthMeter);

        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(iceURL),
                dataSourceFactory,
                extractorsFactory,
                new Handler(), error -> {

        }

        );

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, defaultTrackSelector);
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                System.out.println("ERROR ERROR ERRROOOOOOOR");
                Toast.makeText(context, R.string.server_off, Toast.LENGTH_SHORT).show();
            }
        });

        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

        return exoPlayer;

    }

}
