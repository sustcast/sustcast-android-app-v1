package com.sust.sustcast.fragment;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sust.sustcast.R;
import com.sust.sustcast.data.IceUrl;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StreamFragment extends Fragment implements Player.EventListener {

    boolean isPlaying;
    String iceURL;
    DatabaseReference rootRef;
    DatabaseReference songReference;
    DatabaseReference urlRef;
    String name;
    int countList = 0;
    String newUrl = "";
    float newLoad = Integer.MAX_VALUE;
    String newKey = "";
    int newList;
    long count;
    ValueEventListener vListener;
    ChildEventListener cListener;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
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
        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
        tvPlaying = rootView.findViewById(R.id.tv_track);
        tvPlaying.setText("Fetching Track info ......");
        setButton();
        rootRef = FirebaseDatabase.getInstance().getReference();
        setIceURL();
        getMetadata();
        bPlay = rootView.findViewById(R.id.button_stream);
        unbinder = ButterKnife.bind(this, rootView);
        isPlaying = false;


        return rootView;
    }


    private void getMetadata() {
        songReference = rootRef.child("song");
        songReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("title_show").getValue(String.class);
                tvPlaying.setText(name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setIceURL() {
        urlRef = rootRef.child("IcecastServer");
        vListener = urlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
                System.out.println("cl : " + countList + "count : " + count);
                System.out.println("We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                if (exoPlayer == null) {
                    if (!newKey.isEmpty()) {
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("numlisteners", newList + 1);
                        urlRef.child(newKey).updateChildren(updates);
                    }
                    System.out.println("newkey in condition : " + urlRef.child(newKey).child("numlisteners"));
                    if (exoPlayer == null) {
                        getPlayer();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cListener = urlRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println("ds => " + dataSnapshot.getValue());
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

    private void setButton() {
        bPlay.setOnClickListener(view -> {
            if (isPlaying == false && exoPlayer.getPlayWhenReady() == true) { // should stop
                Log.i("CASE => ", "STOP " + isPlaying + " " + exoPlayer.getPlayWhenReady());
                if (exoPlayer != null) {
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                isPlaying = !isPlaying;

            } else { //should play
                isPlaying = !isPlaying;

                if (exoPlayer == null) {
                    getPlayer();
                }
                Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            }

        });
    }

    private void getPlayer() {
        System.out.println("newurl : " + newUrl);
        System.out.println("newkey : " + newKey);
        System.out.println("newload : " + newLoad);

        if (newUrl.isEmpty() || newKey.isEmpty()) {
            Toast.makeText(getContext(), "ALL SERVERS ARE CURRENTLY FULL!!", Toast.LENGTH_SHORT).show();
        }

        iceURL = newUrl;
        //iceURL = getString(R.string.ice_stream);

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
                new Handler(), error -> {

        }

        );

        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), defaultTrackSelector);
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                System.out.println("ERROR ERROR ERRROOOOOOOR");
                Toast.makeText(getContext(), "Mr. Meow is taking a nap :( Check back after a while or try our other features!!", Toast.LENGTH_SHORT).show();
            }
        });
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);


    }


    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        urlRef.removeEventListener(vListener);
        urlRef.removeEventListener(cListener);
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }


}