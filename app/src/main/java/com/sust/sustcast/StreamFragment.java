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
import com.google.firebase.database.ValueEventListener;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StreamFragment extends Fragment implements Player.EventListener {

    boolean isPlaying;
    String iceURL;
    DatabaseReference rootRef;
    DatabaseReference songReference;
    String name;
    String artist;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
    private String TAG = "StreamFrag";
    int countList = 0;
    boolean urlState = false;
    String newUrl = "";
    float newLoad = Integer.MAX_VALUE;
    String newKey = "";
    int newList;
    long count;
    private TextView tvPlaying;

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
        tvPlaying = rootView.findViewById(R.id.tv_track);
        tvPlaying.setText("Fetching Track info ......");
        rootRef = FirebaseDatabase.getInstance().getReference();
        setIceURL();
        songReference = rootRef.child("song");
        songReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue(String.class);
                artist = dataSnapshot.child("artist").getValue(String.class);
                Log.i(TAG, "Song name: " + name + ", artist " + artist);
                Toast.makeText(getContext(), name + " - " + artist, Toast.LENGTH_SHORT).show();
                tvPlaying.setText(name + " - " + artist);

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
            } else if (isPlaying == true && exoPlayer.getPlayWhenReady() == false) { //should play
                Log.i("CASE => ", "PLAY" + isPlaying + " " + exoPlayer.getPlayWhenReady());
                exoPlayer.setPlayWhenReady(true);
                exoPlayer.getPlaybackState();
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            }
            isPlaying = !isPlaying;

        });

        return rootView;
    }

    private void setIceURL() {
        DatabaseReference urlRef = rootRef.child("IcecastServer");
        urlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
                System.out.println("cl : " + countList + "count : " + count);
                System.out.println("We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                if (exoPlayer == null) {
                    urlRef.child(newKey).child("numlisteners").setValue(newList + 1);
                    getPlayer();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        urlRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                IceUrl iceUrl = dataSnapshot.getValue(IceUrl.class);
                int limit = iceUrl.getLimit();
                String url = iceUrl.getUrl();
                int numList = iceUrl.getNumlisteners();
                float load = (float) numList / (float) limit;
                if (load < 1 && load < newLoad) {
                    newLoad = load;
                    newUrl = url;
                    newKey = dataSnapshot.getKey();
                    newList = numList;

                }

                System.out.println("key => " + dataSnapshot.getKey());
                System.out.println("limit = >" + iceUrl.getLimit());
                System.out.println("url = >" + iceUrl.getUrl());
                System.out.println("numlistener = >" + iceUrl.getNumlisteners());
                System.out.println("load => " + load);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    }

    private void getPlayer() {
        System.out.println("newurl : " + newUrl);
        System.out.println("newkey : " + newKey);
        System.out.println("newload : " + newLoad);

        if (newUrl.isEmpty() || newKey.isEmpty()) {
            Toast.makeText(getContext(), "SERVERS ARE CURRENTLY FULL!!", Toast.LENGTH_SHORT).show();
        }

        //iceURL = newUrl
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
        exoPlayer.release();
    }


}