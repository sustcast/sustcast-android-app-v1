package com.sust.sustcast.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sust.sustcast.R;
import com.sust.sustcast.data.IceUrl;
import com.sust.sustcast.utils.ExoHelper;
import com.sust.sustcast.utils.LoadBalancingUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class StreamFragment extends Fragment implements Player.EventListener {

    private static final String TAG = "StreamFragment";
    private boolean isPlaying;

    private DatabaseReference rootRef;
    private DatabaseReference songReference;
    private DatabaseReference urlRef;
    ChildEventListener cListener;

    Unbinder unbinder;
    Button bPlay;
    TextView tvPlaying;

    ExoHelper exoHelper;

    private List<IceUrl> iceUrlList;
    private String title;
    private String token;

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
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    token = task.getException().getMessage();
                    Log.w("FCM TOKEN Fail stream", task.getException());
                } else {
                    token = task.getResult().getToken();
                    Log.i("FCM TOKEN stream", token);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);

        tvPlaying = rootView.findViewById(R.id.tv_track);
        bPlay = rootView.findViewById(R.id.button_stream);

        setButton();
        tvPlaying.setText(getString(R.string.metadata_loading));

        exoHelper = new ExoHelper(getContext(), new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                tvPlaying.setText(getString(R.string.server_off));
                Crashlytics.logException(error);
            }
        });

        isPlaying = true;

        rootRef = FirebaseDatabase.getInstance().getReference(); //root database reference
        setServerUrlListners();
        setMetaDataListeners();

        unbinder = ButterKnife.bind(this, rootView);

        startRadioOnCreate();

        return rootView;
    }

    private void startRadioOnCreate() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iceUrlList.size() > 0) {
                    Log.d(TAG, "start radio on create");
                    exoHelper.startExo(LoadBalancingUtil.selectIceCastSource(iceUrlList).getUrl());
                } else {
                    startRadioOnCreate();
                }
            }
        }, 1000);
    }

    private void setMetaDataListeners() {
        songReference = rootRef.child("song");
        songReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                title = dataSnapshot.child("title_show").getValue(String.class);
                tvPlaying.setText(title);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setServerUrlListners() {
        iceUrlList = new ArrayList<IceUrl>();

        urlRef = rootRef.child("IcecastServer");
        cListener = urlRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { // read each child and compute load
                IceUrl iceUrl = dataSnapshot.getValue(IceUrl.class);
                iceUrlList.add(iceUrl);
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

        Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
        bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        bPlay.setText(R.string.now_playing);

        bPlay.setOnClickListener(view -> {
            Log.d(TAG, "setButton: isplaying -> " + isPlaying);

            if (isPlaying) {
                Drawable img1 = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
                bPlay.setText(R.string.now_paused);

                exoHelper.stopExo();
            } else {
                Drawable img1 = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                bPlay.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
                bPlay.setText(R.string.now_playing);

                exoHelper.startExo(LoadBalancingUtil.selectIceCastSource(iceUrlList).getUrl());
            }

            isPlaying = !isPlaying;
        });


    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        urlRef.removeEventListener(cListener);
        exoHelper.stopExo();
    }


}