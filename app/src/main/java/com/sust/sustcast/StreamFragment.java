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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.ButterKnife;
import butterknife.Unbinder;
//import wseemann.media.FFmpegMediaMetadataRetriever;

public class StreamFragment extends Fragment implements Player.EventListener {

    boolean isPlaying;
    String iceURL;
    //    FFmpegMediaMetadataRetriever fmmr;
    DatabaseReference mDatabase;
    Song song;
    String name;
    String artist;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
    private String TAG = "StreamFrag";
    private TextView tv_song;


    public StreamFragment() {
    }


    public static StreamFragment newInstance() {
        StreamFragment fragment = new StreamFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
        tv_song = rootView.findViewById(R.id.tv_track);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                song = dataSnapshot.getValue(Song.class);
                name = song.getName();
                artist = song.getArtist();
                Log.i(TAG, "Song name: " + name + ", artist " + artist);
                tv_song.setText(name + " - " + artist);
                Toast.makeText(getContext(), name + " - " + artist, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                song = dataSnapshot.getValue(Song.class);
                name = song.getName();
                artist = song.getArtist();
                Log.i(TAG, "Song name: " + name + ", artist " + artist);
                tv_song.setText(name + " - " + artist);
                Toast.makeText(getContext(), name + " - " + artist, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        bPlay = rootView.findViewById(R.id.button_stream);
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
            }
            isPlaying = !isPlaying;

        });

        getPlayer();
        //getMetadata();
        return rootView;
    }


    private void getPlayer() {
        iceURL = getString(R.string.ice_stream);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        TrackSelection.Factory trackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector defaultTrackSelector =
                new DefaultTrackSelector(trackSelectionFactory);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "SUSTCast"),
                defaultBandwidthMeter);
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(iceURL),
                dataSourceFactory,
                extractorsFactory,
                new Handler(), Throwable::printStackTrace);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), defaultTrackSelector);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
//        fmmr.release();
        exoPlayer.release();
    }

//    public void getMetadata() {
//        fmmr = new FFmpegMediaMetadataRetriever();
//        try {
//            fmmr.setDataSource(iceURL);
//        } catch (Exception e) {
//            Toast.makeText(getContext(), "DjMeow is taking a nap. He will be back soon", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getContext(), LandingActivity.class));
//
//        }
//        try {
//            for (int i = 0; i < Constantss.METADATA_KEYS.length; i++) {
//                String key = Constantss.METADATA_KEYS[i];
//                String value = fmmr.extractMetadata(key);
////
////                if (value != null) {
////                    Log.i("METADATA => ", "Key: " + key + " Value: " + value);
////                } else {
////                    Log.i("METADATA => ", "Key: " + key + " Value: " + "NONE");
////
////                }
//            }
//        } catch (IllegalArgumentException ex) {
//            ex.printStackTrace();
//        }
//
//    }


}
