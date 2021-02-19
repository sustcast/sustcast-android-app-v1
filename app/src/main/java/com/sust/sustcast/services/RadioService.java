package com.sust.sustcast.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.sust.sustcast.R;
import com.sust.sustcast.fragment.FragmentHolder;

import static com.sust.sustcast.data.Constants.CHANNEL_ID;
import static com.sust.sustcast.data.Constants.CHANNEL_NAME;
import static com.sust.sustcast.data.Constants.ERROR;
import static com.sust.sustcast.data.Constants.PAUSE;
import static com.sust.sustcast.data.Constants.PAUSED;
import static com.sust.sustcast.data.Constants.PLAY;
import static com.sust.sustcast.data.Constants.PLAYING;

public class RadioService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "RadioService";
    private SimpleExoPlayer exoPlayer;
    private Context context;
    private BroadcastReceiver receiver;
    private String iceURL;
    private AudioManager audioManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        iceURL = intent.getStringExtra("url");
        registerReceiver();
        Play();
        ShowPlayPauseNotification();

        startForeground(1234, CreateNotification(getCustomPlayDesign()));

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        context.registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            //stop();
            Pause();

            Log.d(TAG, "onStartCommand: " + AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

            return START_NOT_STICKY;
        }

        Log.d(TAG, "Starting RadioService");

        return START_STICKY;
    }


    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Pause();
            Log.d(TAG, "onReceive: " + "Noisy");
        }
    };

    public void InitializePlayer(String url) {
        if (url == null || url.isEmpty()) {
            Log.d(TAG, "Empty url");
            Intent errorIntent = new Intent(ERROR).setPackage(context.getPackageName());
            context.sendBroadcast(errorIntent);
            return;
        }

        if (exoPlayer == null) {

            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, getApplicationInfo().loadLabel(getPackageManager()).toString()));

            DefaultLoadControl loadControl = new DefaultLoadControl.Builder().createDefaultLoadControl();
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));

            exoPlayer = new SimpleExoPlayer.Builder(this, renderersFactory).setLoadControl(loadControl).build();
            exoPlayer.prepare(mediaSource, true, true);
            exoPlayer.setHandleWakeLock(true);


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

        } else {
            Log.d(TAG, "Exo is already running");
        }


    }


    private void initAudioFocus() {
        if (audioManager != null) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

    }


    public void Play() {
        InitializePlayer(iceURL);
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            initAudioFocus();
        }
        startForeground(1234, CreateNotification(getCustomPlayDesign()));

    }

    public void Pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            audioManager.abandonAudioFocus(this);
        }


    }

    public void stop() {

        exoPlayer.stop();

        audioManager.abandonAudioFocus(this);
        Log.d(TAG, "stop: ");
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        } else {
            Log.d(TAG, "releasePlayer: " + "No player running");
        }

    }


    public void ShowPlayPauseNotification() {

        if (!shouldShowPauseButton()) {
            PauseNotification();
        } else {
            PlayNotification();
        }

    }

    public void PauseNotification() {

        managerCompat().notify(1234, CreateNotification(getCustomPauseDesign()));
    }

    private NotificationManagerCompat managerCompat() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }


        return manager;
    }

    public void PlayNotification() {

        managerCompat().notify(1234, CreateNotification(getCustomPlayDesign()));
    }


    public Notification CreateNotification(RemoteViews remoteViews) {

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.sustcast_logo_circle_only)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(0)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setContentIntent(createCurrentContentIntent())
                .build();

        return notification;

    }


    private RemoteViews getCustomPlayDesign() {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                R.layout.playback_notification);
        remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.sustcast_logo_circle_only);

        Intent playIntent = new Intent(PAUSE).setPackage(context.getPackageName());


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setImageViewResource(R.id.notification_button, R.drawable.exo_notification_pause);

        remoteViews.setOnClickPendingIntent(R.id.notification_button, pendingIntent);

        remoteViews.setTextViewText(R.id.notification_title, PLAYING);

        return remoteViews;
    }


    private RemoteViews getCustomPauseDesign() {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                R.layout.playback_notification);

        remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.sustcast_logo_circle_only);

        Intent playIntent = new Intent(PLAY).setPackage(context.getPackageName());


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setImageViewResource(R.id.notification_button, R.drawable.exo_notification_play);

        remoteViews.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        remoteViews.setTextViewText(R.id.notification_title, PAUSED);

        return remoteViews;
    }

    private boolean shouldShowPauseButton() {
        if (exoPlayer != null) {
            return exoPlayer.getPlaybackState() != Player.STATE_ENDED
                    && exoPlayer.getPlaybackState() != Player.STATE_IDLE
                    && exoPlayer.getPlayWhenReady();
        } else {
            return true;
        }

    }


    public PendingIntent createCurrentContentIntent() {

        Intent intent
                = new Intent(context, FragmentHolder.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // For opening current content
        PendingIntent pendingIntent
                = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        return pendingIntent;
    }

    public void registerReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PAUSE);
        intentFilter.addAction(PLAY);
        intentFilter.addAction(ERROR);

        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (!(intent.getAction() == null)) {
                        if (intent.getAction().equals(PAUSE)) {
                            Pause();
                            PauseNotification();
                            stopForeground(false);

                        } else if (intent.getAction().equals(PLAY)) {
                            Play();
                        } else if (intent.getAction().equals(ERROR)) {
                            Pause();
                            PauseNotification();
                            stopForeground(false);
                            releasePlayer();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();

        try {
            if (receiver != null && becomingNoisyReceiver != null) {
                context.unregisterReceiver(receiver);
                context.unregisterReceiver(becomingNoisyReceiver);
            }
        } catch (Exception exception) {
            Log.d(TAG, "onDestroyView: " + "Exception!!");
            Crashlytics.logException(exception);
        }

        Log.d(TAG, "RadioService is destroyed!");
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        Intent pauseIntent = new Intent(PAUSE).setPackage(context.getPackageName());

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_LOSS:

                context.sendBroadcast(pauseIntent);
                Log.d(TAG, "onAudioFocusChange: " + AudioManager.AUDIOFOCUS_LOSS);

                break;


            case AudioManager.AUDIOFOCUS_GAIN:

                Play();
                Log.d(TAG, "onAudioFocusChange: " + AudioManager.AUDIOFOCUS_GAIN);

                break;


            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                context.sendBroadcast(pauseIntent);
                Log.d(TAG, "onAudioFocusChange: " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "onAudioFocusChange: " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

                break;

        }
    }
}
