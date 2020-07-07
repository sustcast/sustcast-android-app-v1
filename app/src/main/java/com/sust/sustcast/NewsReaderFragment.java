package com.sust.sustcast;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayerFactory;
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

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class NewsReaderFragment extends Fragment {

    boolean isPlaying;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
    private String TAG = "NewsFrag";

    public NewsReaderFragment() {
        // Required empty public constructor
    }

    public static NewsReaderFragment newInstance() {
        NewsReaderFragment fragment = new NewsReaderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_reader, container, false);
        bPlay = rootView.findViewById(R.id.button_play);
        unbinder = ButterKnife.bind(this, rootView);
        isPlaying = false;
        bPlay.setOnClickListener(view -> {
            if (isPlaying == false && exoPlayer.getPlayWhenReady() == true) { // should stop
                Log.i("CASE => ", "STOP " + isPlaying + " " + exoPlayer.getPlayWhenReady());
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.getPlaybackState();
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                bPlay.setText(R.string.now_paused);
            } else if (isPlaying == true && exoPlayer.getPlayWhenReady() == false) { //should play
                Log.i("CASE => ", "PLAY" + isPlaying + " " + exoPlayer.getPlayWhenReady());
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.getPlaybackState();
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                bPlay.setText(R.string.now_playing);
            } else if (exoPlayer.getPlayWhenReady() == true && isPlaying == true) {  //restart
                Log.i("CASE => ", "RESTART" + isPlaying + " " + exoPlayer.getPlayWhenReady());
                exoPlayer.release();
                exoPlayer.stop();
                exoPlayer.setPlayWhenReady(true);

            }

            isPlaying = !isPlaying;
        });

        getPlayer();
        return rootView;
    }

    private void getPlayer() {
        // URL of the video to stream
        String newsURL = getString(R.string.bbc_news);

	/* A TrackSelector that selects tracks provided by the MediaSource to be consumed by each of the available Renderers.
	  A TrackSelector is injected when the player is created. */
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces Extractor instances for parsing the media data.
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        TrackSelection.Factory trackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector defaultTrackSelector =
                new DefaultTrackSelector(trackSelectionFactory);

        // Produces DataSource instances through which media data is loaded.
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "SUSTCast"),
                defaultBandwidthMeter);

        // This is the MediaSource representing the media to be played.
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(newsURL),
                dataSourceFactory,
                extractorsFactory,
                new Handler(), Throwable::printStackTrace);
        // Create the player with previously created TrackSelector
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), defaultTrackSelector);

        // Prepare the player with the source.
        exoPlayer.prepare(mediaSource);

        // Autoplay the video when the player is ready
        exoPlayer.setPlayWhenReady(true);
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        // Release the player when it is not needed
        exoPlayer.release();
    }


}
