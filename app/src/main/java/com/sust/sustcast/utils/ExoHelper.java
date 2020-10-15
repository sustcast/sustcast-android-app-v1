package com.sust.sustcast.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sust.sustcast.R;
import com.sust.sustcast.fragment.FragmentHolder;
import static com.sust.sustcast.data.Constants.PAUSED;
import static com.sust.sustcast.data.Constants.PLAYING;


public class ExoHelper {
    private static final String TAG = "ExoHelper";
    private String iceURL;
    private Context context;
    private String fragmentName;
    private SimpleExoPlayer exoPlayer;
    private Player.EventListener eventListener;
    private Button button;
    private PlayerNotificationManager playerNotificationManager;


    public ExoHelper(Context context) {
        this.context = context;
    }

    public ExoHelper(Context context, Player.EventListener eventListener, Button button, String fragmentName) {
        if (exoPlayer != null) {
            return;
        }


        this.fragmentName = fragmentName; // Name of the current fragment. Use in PlayerNotificationManager.MediaDescriptionAdapter
        this.context = context;
        this.eventListener = eventListener;
        this.button = button;

        createNotificationChannel();  // For creating notification channels.
    }


    public void stopExo() {
        if (exoPlayer != null) { //if exo is running
            Log.d(TAG, "Stopping exo....");
            exoPlayer.stop();
            playerNotificationManager.setPlayer(null);
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

        if (eventListener != null) {
            exoPlayer.addListener(eventListener);
        }
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        ToggleButton(true);
        setPlayerNotificationManager(exoPlayer);


    }

    public SimpleExoPlayer getPlayer() {
        return exoPlayer;
    }

    public String NotificationContent() {
        if (exoPlayer.isPlaying()) {
            return PLAYING;
        } else {
            return PAUSED;
        }

        // There can be more conditions.
    }


    public boolean state()  // May come in handy.
    {
        return exoPlayer.isPlaying();
    }


    public void ToggleButton(boolean state) {
        if (state) {
            Drawable img = button.getContext().getResources().getDrawable(R.drawable.play_button);
            button.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            button.setText(R.string.now_playing);
        } else {
            Drawable img1 = button.getContext().getResources().getDrawable(R.drawable.pause_button);
            button.setCompoundDrawablesWithIntrinsicBounds(img1, null, null, null);
            button.setText(R.string.server_off);
        }
    }


    private Bitmap BigLogoGen() {
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return logo;

        // May change from device to device.

    }


    private PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public String getCurrentSubText(Player player) {
            return null;
        }

        @Override
        public String getCurrentContentTitle(Player player) {
            return NotificationContent();
        }

        @Override
        public PendingIntent createCurrentContentIntent(Player player) {

            Intent intent
                    = new Intent(context, FragmentHolder.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // For opening current content
            PendingIntent pendingIntent
                    = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            return pendingIntent;
        }

        @Override
        public String getCurrentContentText(Player player) {
            return null;
        }

        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return BigLogoGen();
        }
    };


    private void createNotificationChannel() {


        Log.d("In create channel: ","yes");

        String channel_id = "123";
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel
                    = new NotificationChannel(
                    channel_id, "SUSTcast",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(
                    notificationChannel);
        }
    }


    private void setPlayerNotificationManager(Player player) {


        Log.d("Starting notification: ","Yes");

        playerNotificationManager = new PlayerNotificationManager(context, "123", 1234, mediaDescriptionAdapter);
        playerNotificationManager.setUseNavigationActions(false);
        playerNotificationManager.setUsePlayPauseActions(true);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setColor(Color.rgb(254, 213, 0)); // Yellow Color from the logo & UI
        //playerNotificationManager.setColor(Color.GREEN);
        playerNotificationManager.setColorized(true);
        playerNotificationManager.setUseChronometer(false);
        playerNotificationManager.setSmallIcon(R.drawable.sustcast_logo_circle_only);
        playerNotificationManager.setPlayer(player);

    }

}
