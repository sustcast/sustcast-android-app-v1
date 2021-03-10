package com.sust.sustcast.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sust.sustcast.R;
import com.sust.sustcast.data.IceUrl;
import com.sust.sustcast.services.RadioService;
import com.sust.sustcast.utils.ConnectionLiveData;
import com.sust.sustcast.utils.LoadBalancingUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.sust.sustcast.data.Constants.CHECKNET;
import static com.sust.sustcast.data.Constants.ERROR;
import static com.sust.sustcast.data.Constants.NO_INTERNET;
import static com.sust.sustcast.data.Constants.PAUSE;
import static com.sust.sustcast.data.Constants.PLAY;
import static com.sust.sustcast.data.Constants.SERVEROFF;


public class StreamFragment extends Fragment {

    private static final String TAG = "StreamFragment";
    ChildEventListener cListener;
    Unbinder unbinder;
    Button bPlay;
    TextView tvPlaying;
    private boolean isPlaying = true;    // Make the boolean global so that we can change the value as per our needs
    private DatabaseReference rootRef;
    private DatabaseReference songReference;
    private DatabaseReference urlRef;
    private List<IceUrl> iceUrlList;
    private String title;
    private BroadcastReceiver receiver;

    public StreamFragment() {
    }

    public static StreamFragment newInstance() {
        return new StreamFragment();
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
        tvPlaying.setText(rootView.getContext().getString(R.string.metadata_loading));


        bPlay = rootView.findViewById(R.id.button_stream);
        unbinder = ButterKnife.bind(this, rootView);
        ConnectionLiveData connectionLiveData = new ConnectionLiveData(rootView.getContext());

        setButton();

        connectionLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                Log.d(TAG, "onCreateView: " + "No internet");
                Toast.makeText(rootView.getContext(), CHECKNET, Toast.LENGTH_LONG).show();

                Intent pauseIntent = new Intent(NO_INTERNET).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(pauseIntent);
                isPlaying = false;
                ToggleButton(false);


            }
        });


        rootRef = FirebaseDatabase.getInstance().getReference(); //root database reference
        setServerUrlListners();
        setMetaDataListeners();
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

                    if (getContext() != null) {
                        Intent intent = new Intent(getContext(), RadioService.class);
                        intent.putExtra("url", LoadBalancingUtil.selectIceCastSource(iceUrlList).getUrl());
                        getContext().startService(intent);
                        ToggleButton(true);
                    } else {
                        Log.d(TAG, "run: " + "Context is null");
                    }

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

                Log.d(TAG, "onCancelled: ");
            }


        });
    }

    private void setServerUrlListners() {
        iceUrlList = new ArrayList<>();

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
        bPlay.setOnClickListener(view -> {
            Log.d(TAG, "setButton: isplaying -> " + isPlaying);


            Intent intent = new Intent(getContext(), RadioService.class);
            if (isPlaying) {

                /*
                Intent pauseIntent = new Intent(PAUSE).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(pauseIntent);
                 */

                getContext().stopService(intent);
                ToggleButton(false);
                isPlaying = false;
            } else {

                /*
                Intent playIntent = new Intent(PLAY).setPackage(getContext().getPackageName());
                getContext().sendBroadcast(playIntent);
                 */

                intent.putExtra("url", LoadBalancingUtil.selectIceCastSource(iceUrlList).getUrl());
                getContext().startService(intent);
                ToggleButton(true);
                isPlaying = true;
            }
            //isPlaying = !isPlaying;
        });


    }


    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(PLAY);
        intentFilter.addAction(ERROR);
        intentFilter.addAction(NO_INTERNET);

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!(intent.getAction() == null)) {
                        if (intent.getAction().equals(PAUSE)) {
                            Log.d(TAG, "onReceive: " + "Paused");
                            ToggleButton(false);
                            isPlaying = false;
                        } else if (intent.getAction().equals(PLAY)) {
                            Log.d(TAG, "onReceive: " + "Playing");
                            ToggleButton(true);
                            isPlaying = true;
                        } else if (intent.getAction().equals(ERROR)) {
                            Log.d(TAG, "onReceive: " + "ERROR");
                            ToggleButton(false);
                            Toast.makeText(context, SERVEROFF, Toast.LENGTH_SHORT).show();
                        } else if (intent.getAction().equals(NO_INTERNET)) {
                            Log.d(TAG, "onReceive: " + "NO internet");
                            ToggleButton(false);
                            Toast.makeText(context, CHECKNET, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "onReceive: " + "Nothing received!");
                    }


                }
            };

            getActivity().registerReceiver(receiver, intentFilter);

        } else {
            Log.d(TAG, "onStart: " + "Receiver already registered");
        }


    }


    public void ToggleButton(boolean state) {
        if (state) {
            Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
            bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            bPlay.setText(R.string.now_playing);
        } else {
            Drawable img1 = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
            bPlay.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
            bPlay.setText(R.string.server_off);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        urlRef.removeEventListener(cListener);

        Intent intent = new Intent(getContext(), RadioService.class);
        getContext().stopService(intent);


        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            FirebaseCrashlytics.getInstance().recordException(exception);
        }

    }


}