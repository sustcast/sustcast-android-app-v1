package com.sust.sustcast.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
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
import com.sust.sustcast.fragment.FragmentHolder;
import com.sust.sustcast.utils.PlayerNotificationManager;

import static com.sust.sustcast.data.Constants.PAUSED;
import static com.sust.sustcast.data.Constants.PLAYING;


public class MusicPlayerService extends Service implements Player.EventListener {

    public static final String TAG = "MusicPlayerService";
    public String PAUSE = "com.sust.sustcast.PAUSE";
    public String ERROR = "com.sust.sustcast.ERROR";
    private SimpleExoPlayer exoPlayer;
    private com.sust.sustcast.utils.PlayerNotificationManager customPlayerNotificationManager;
    private String iceURL;

    private BroadcastReceiver receiver;

    private Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startExo(intent.getStringExtra("url"));

        RegisterReceiver();

        Log.d(TAG, "Starting MusicPlayerService");


        return START_STICKY;
    }

    public void startExo(String newUrl) {
        if (newUrl == null || newUrl.isEmpty()) {
            Log.d(TAG, "startExo: empty url");
            Intent errorIntent = new Intent(ERROR).setPackage(context.getPackageName());
            context.sendBroadcast(errorIntent);
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
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.setHandleWakeLock(true);

        /*
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();

        exoPlayer.setAudioAttributes(audioAttributes, true);
         */

        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException exception) {

                Log.d(TAG, "onPlayerError: ");
                Intent errorIntent = new Intent(ERROR).setPackage(context.getPackageName());
                context.sendBroadcast(errorIntent);
                stopForeground(false);
                Crashlytics.logException(exception);

            }
        });

        createCustomPlayerNotificationManger(exoPlayer);

    }


    public void createCustomPlayerNotificationManger(Player player) {

        customPlayerNotificationManager = new com.sust.sustcast.utils.PlayerNotificationManager(context, "123", 1234, mediaDescriptionAdapter1, new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopForeground(true);
            }


            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                startForeground(notificationId, notification);
            }
        });

        customPlayerNotificationManager.setUseNavigationActions(false);
        customPlayerNotificationManager.setSmallIcon(R.drawable.sustcast_logo_circle_only);
        customPlayerNotificationManager.setUseChronometer(false);
        customPlayerNotificationManager.setPlayer(player);

    }


    private com.sust.sustcast.utils.PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter1 = new com.sust.sustcast.utils.PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public String getCurrentSubText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, com.sust.sustcast.utils.PlayerNotificationManager.BitmapCallback callback) {
            return null;
        }

        @Override
        public String getCurrentContentTitle(Player player) {
            return NotificationTitle(); // Title of the notification
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


    };

    public String NotificationTitle() {

        if (exoPlayer.getPlayWhenReady()) {  // Better than isPlaying
            return PLAYING;
        } else {
            return PAUSED;
        }
    }

    public void stopExo() {
        if (exoPlayer != null) { //if exo is running
            Log.d(TAG, "Stopping exo....");
            exoPlayer.stop();
            customPlayerNotificationManager.setPlayer(null);
            exoPlayer.release();
            exoPlayer = null;


        } else {
            Log.d(TAG, "Can't stop because No exoplayer is running");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        stopExo();

        try {
            if (receiver != null) {
                context.unregisterReceiver(receiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            Crashlytics.logException(exception);
        }

        Log.d(TAG, "onDestroy: ");

    }


    public void RegisterReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAUSE);

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!(intent.getAction() == null)) {
                        if (intent.getAction().equals(PAUSE)) {
                            Log.d(TAG, "onReceive: " + "Paused");
                            stopForeground(false);
                        }
                    } else {
                        Log.d(TAG, "onReceive: " + "Nothing received!");
                    }
                }
            };

            context.registerReceiver(receiver, intentFilter);

        } else {
            Log.d(TAG, "onStart: " + "Receiver already registered");
        }


    }

}
