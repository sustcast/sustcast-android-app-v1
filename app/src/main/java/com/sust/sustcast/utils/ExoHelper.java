package com.sust.sustcast.utils;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.Nullable;

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
    private com.sust.sustcast.utils.PlayerNotificationManager customPlayerNotificationManager;
    public boolean playBackState;


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


    }


    public void stopExo() {
        if (exoPlayer != null) { //if exo is running
            Log.d(TAG, "Stopping exo....");
            exoPlayer.stop();
            //playerNotificationManager.setPlayer(null);
            customPlayerNotificationManager.setPlayer(null);
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
        //setPlayerNotificationManager(exoPlayer);
        createCustomPlayerNotificationManger(exoPlayer);


         /*

        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                playBackState = isPlaying;
            }
        });

         */

    }


    public SimpleExoPlayer getPlayer() {
        return exoPlayer;
    }

    public String NotificationContent() {

        if (exoPlayer.getPlayWhenReady()) {  // Better than isPlaying
            return PLAYING;
        } else {
            return PAUSED;
        }
    }


    public void StopNotification() {
        customPlayerNotificationManager.setPlayer(null);
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


    // CustomPlayerNotificationManager

    public void createCustomPlayerNotificationManger(Player player) {

        customPlayerNotificationManager = new com.sust.sustcast.utils.PlayerNotificationManager(context, "123", 1234, mediaDescriptionAdapter1);
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
            return NotificationContent(); // Title of the notification
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



    /*
     *
     * You may need to edit some code in PlayerNotificationManager in order to work with the below methods
     * As they are not needed for now, I'm commenting them out.
     *
     *
     */


    /*

    public boolean isRunning()
    {
        return playBackState;
    }

       */

    /*

    public static Bitmap drawableToBitmap(Drawable drawable) {  //Not needed if createCustomPlayerNotificationManger is used
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() + 50, drawable.getIntrinsicHeight() + 50, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

     */


    /*


    // Below is the normal PlayerNotificationManager. We are not using it.


    private void setPlayerNotificationManager(Player player) {


        Log.d(TAG, "Starting notification");

        playerNotificationManager = new PlayerNotificationManager(context, "123", 1234, mediaDescriptionAdapter);
        playerNotificationManager.setUseNavigationActions(false);
        playerNotificationManager.setUsePlayPauseActions(true);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0);
        //playerNotificationManager.setColor(Color.rgb(254, 213, 0)); // Yellow Color from the logo & UI
        playerNotificationManager.setUseChronometer(false);
        playerNotificationManager.setSmallIcon(R.drawable.sustcast_logo_circle_only);
        playerNotificationManager.setPlayer(player);

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
            return drawableToBitmap(context.getDrawable(R.drawable.ic_logo));
        }
    };

 */


}
