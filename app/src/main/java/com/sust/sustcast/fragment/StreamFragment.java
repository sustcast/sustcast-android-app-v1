package com.sust.sustcast.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sust.sustcast.R;
import com.sust.sustcast.data.IceUrl;
import com.sust.sustcast.utils.ExoHelper;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class StreamFragment extends Fragment implements Player.EventListener {

    boolean isPlaying;
    DatabaseReference rootRef;
    DatabaseReference songReference;
    DatabaseReference urlRef;
    String name;
    String newUrl = "";
    float newLoad = Integer.MAX_VALUE;
    String newKey = "";
    int newList;
    long count;
    ValueEventListener vListener;
    ChildEventListener cListener;
    Unbinder unbinder;
    Button bPlay;
    TextView tvPlaying;
    ExoHelper exoHelper;
    private SimpleExoPlayer exoPlayer;

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
        rootRef = FirebaseDatabase.getInstance().getReference(); //root database reference
        exoHelper = new ExoHelper(getContext());
        setIceURL();
        getMetadata();
        bPlay = rootView.findViewById(R.id.button_stream);
        setButton();
        unbinder = ButterKnife.bind(this, rootView);
        isPlaying = true;
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
        //value listener triggers after child listener ends
        cListener = urlRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { // read each child and compute load
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

        vListener = urlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
                System.out.println("We're done loading the initial " + dataSnapshot.getChildrenCount() + " items");
                System.out.println("final url -> " + newUrl);
                System.out.println("final load -> " + newLoad);
                System.out.println("final key -> " + newKey);

                if (exoPlayer == null && newUrl.isEmpty() == false) { // if there is no previous exoplayer instance and we have a load-balanced URL, init exoplayer
                    System.out.println("Trying to set ICE url because exoplayer state -> " + (exoPlayer == null) + " newURL : " + newUrl);
                    if (isPlaying == true) {
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("numlisteners", newList + 1);
                        urlRef.child(newKey).updateChildren(updates);
                        exoPlayer = exoHelper.startExo(newUrl);
//                        getPlayer();
                    } else {
                        System.out.println("can't start player because button state -> " + isPlaying + " paused state");
                    }
                } else if (newUrl.isEmpty() == true) {
                    Toast.makeText(getContext(), R.string.server_full, Toast.LENGTH_SHORT).show();
                    System.out.println("can't set ICE URL because newUrl -> " + newUrl);
                } else {
                    System.out.println("can't set ICE URL because exoplayer state -> " + (exoPlayer == null));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setButton() {
        bPlay.setOnClickListener(view -> {
            System.out.println("player state for button -> " + isPlaying);

            if (isPlaying == false) { //isPlaying : false -> button should stop exoPlayer
                isPlaying = !isPlaying;
                System.out.println(" Stop button \n exoState :  " + (exoPlayer == null)); // exoState : False -> can stop player
                exoPlayer = exoHelper.stopExo(bPlay);
//                stopExo();
            } else {
                isPlaying = !isPlaying;
                System.out.println("Play button \n exoState :  " + (exoPlayer == null)); // exoState : True -> can start player
                if (exoPlayer == null && newUrl.isEmpty() == false) {
                    exoPlayer = exoHelper.startExo(newUrl);
                    Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                    bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    bPlay.setText(R.string.now_playing);
//                    getPlayer();
                }

            }

        });
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        urlRef.removeEventListener(vListener);
        urlRef.removeEventListener(cListener);

        if (exoPlayer != null) {
            System.out.println("On destroy View ");
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }


}