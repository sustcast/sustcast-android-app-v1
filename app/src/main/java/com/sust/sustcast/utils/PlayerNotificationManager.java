package com.sust.sustcast.utils;

/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.sust.sustcast.R;
import com.sust.sustcast.data.ButtonEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayerNotificationManager {


    /*

     Forked from: https://github.com/google/ExoPlayer/blob/release-v2/library/ui/src/main/java/com/google/android/exoplayer2/ui/PlayerNotificationManager.java


     */

    public interface MediaDescriptionAdapter {


        CharSequence getCurrentContentTitle(Player player);

        @Nullable
        PendingIntent createCurrentContentIntent(Player player);

        @Nullable
        CharSequence getCurrentContentText(Player player);

        @Nullable
        default CharSequence getCurrentSubText(Player player) {
            return null;
        }

        @Nullable
        Bitmap getCurrentLargeIcon(Player player, BitmapCallback callback);
    }

    /**
     * Defines and handles custom actions.
     */
    public interface CustomActionReceiver {


        Map<String, NotificationCompat.Action> createCustomActions(Context context, int instanceId);

        List<String> getCustomActions(Player player);

        void onCustomAction(Player player, String action, Intent intent);
    }

    /**
     * A listener for changes to the notification.
     */
    public interface NotificationListener {

        @Deprecated
        default void onNotificationStarted(int notificationId, Notification notification) {
        }

        @Deprecated
        default void onNotificationCancelled(int notificationId) {
        }

        default void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
        }

        default void onNotificationPosted(
                int notificationId, Notification notification, boolean ongoing) {
        }
    }

    /**
     * Receives a {@link Bitmap}.
     */
    public final class BitmapCallback {
        private final int notificationTag;

        /**
         * Create the receiver.
         */
        private BitmapCallback(int notificationTag) {
            this.notificationTag = notificationTag;
        }

        public void onBitmap(final Bitmap bitmap) {
            if (bitmap != null) {
                postUpdateNotificationBitmap(bitmap, notificationTag);
            }
        }
    }

    private static final String TAG = "PlayerNotificationMgr";

    /**
     * The action which starts playback.
     */
    public static final String ACTION_PLAY = "com.google.android.exoplayer.play";
    /**
     * The action which pauses playback.
     */
    public static final String ACTION_PAUSE = "com.google.android.exoplayer.pause";
    /**
     * The action which skips to the previous window.
     */
    public static final String ACTION_PREVIOUS = "com.google.android.exoplayer.prev";
    /**
     * The action which skips to the next window.
     */
    public static final String ACTION_NEXT = "com.google.android.exoplayer.next";
    /**
     * The action which fast forwards.
     */
    public static final String ACTION_FAST_FORWARD = "com.google.android.exoplayer.ffwd";
    /**
     * The action which rewinds.
     */
    public static final String ACTION_REWIND = "com.google.android.exoplayer.rewind";
    /**
     * The action which stops playback.
     */
    public static final String ACTION_STOP = "com.google.android.exoplayer.stop";
    /**
     * The extra key of the instance id of the player notification manager.
     */
    public static final String EXTRA_INSTANCE_ID = "INSTANCE_ID";
    /**
     * The action which is executed when the notification is dismissed. It cancels the notification
     * and calls {@link NotificationListener#onNotificationCancelled(int, boolean)}.
     */
    private static final String ACTION_DISMISS = "com.google.android.exoplayer.dismiss";

    // Internal messages.

    private static final int MSG_START_OR_UPDATE_NOTIFICATION = 0;
    private static final int MSG_UPDATE_NOTIFICATION_BITMAP = 1;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            NotificationCompat.VISIBILITY_PRIVATE,
            NotificationCompat.VISIBILITY_PUBLIC,
            NotificationCompat.VISIBILITY_SECRET
    })
    public @interface Visibility {
    }

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            NotificationCompat.PRIORITY_DEFAULT,
            NotificationCompat.PRIORITY_MAX,
            NotificationCompat.PRIORITY_HIGH,
            NotificationCompat.PRIORITY_LOW,
            NotificationCompat.PRIORITY_MIN
    })
    public @interface Priority {
    }

    private static int instanceIdCounter;

    private final Context context;
    private final String channelId;
    private final int notificationId;
    private final MediaDescriptionAdapter mediaDescriptionAdapter;
    @Nullable
    private final CustomActionReceiver customActionReceiver;
    private final Handler mainHandler;
    private final NotificationManagerCompat notificationManager;
    private final IntentFilter intentFilter;
    private final Player.EventListener playerListener;
    private final NotificationBroadcastReceiver notificationBroadcastReceiver;
    private final Map<String, NotificationCompat.Action> playbackActions;
    private final Map<String, NotificationCompat.Action> customActions;
    private final PendingIntent dismissPendingIntent;
    private final int instanceId;
    private final Timeline.Window window;

    @Nullable
    private NotificationCompat.Builder builder;
    @Nullable
    private List<NotificationCompat.Action> builderActions;
    @Nullable
    private Player player;
    @Nullable
    private PlaybackPreparer playbackPreparer;
    private ControlDispatcher controlDispatcher;
    private boolean isNotificationStarted;
    private int currentNotificationTag;
    @Nullable
    private NotificationListener notificationListener;
    @Nullable
    private MediaSessionCompat.Token mediaSessionToken;
    private boolean useNavigationActions;
    private boolean useNavigationActionsInCompactView;
    private boolean usePlayPauseActions;
    private boolean useStopAction;
    private int badgeIconType;
    private boolean colorized;
    private int defaults;
    private int color;
    @DrawableRes
    private int smallIconResourceId;
    private int visibility;
    @Priority
    private int priority;
    private boolean useChronometer;

    @Deprecated
    public static PlayerNotificationManager createWithNotificationChannel(
            Context context,
            String channelId,
            @StringRes int channelName,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter) {
        return createWithNotificationChannel(
                context,
                channelId,
                channelName,
                /* channelDescription= */ 0,
                notificationId,
                mediaDescriptionAdapter);
    }

    public static PlayerNotificationManager createWithNotificationChannel(
            Context context,
            String channelId,
            @StringRes int channelName,
            @StringRes int channelDescription,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter) {
        NotificationUtil.createNotificationChannel(
                context, channelId, channelName, channelDescription, NotificationUtil.IMPORTANCE_LOW);
        return new PlayerNotificationManager(
                context, channelId, notificationId, mediaDescriptionAdapter);
    }

    @Deprecated
    public static PlayerNotificationManager createWithNotificationChannel(
            Context context,
            String channelId,
            @StringRes int channelName,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter,
            @Nullable NotificationListener notificationListener) {
        return createWithNotificationChannel(
                context,
                channelId,
                channelName,
                /* channelDescription= */ 0,
                notificationId,
                mediaDescriptionAdapter,
                notificationListener);
    }


    public static PlayerNotificationManager createWithNotificationChannel(
            Context context,
            String channelId,
            @StringRes int channelName,
            @StringRes int channelDescription,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter,
            @Nullable NotificationListener notificationListener) {
        NotificationUtil.createNotificationChannel(
                context, channelId, channelName, channelDescription, NotificationUtil.IMPORTANCE_LOW);
        return new PlayerNotificationManager(
                context, channelId, notificationId, mediaDescriptionAdapter, notificationListener);
    }

    public PlayerNotificationManager(
            Context context,
            String channelId,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter) {
        this(
                context,
                channelId,
                notificationId,
                mediaDescriptionAdapter,
                /* notificationListener= */ null,
                /* customActionReceiver */ null);
    }


    public PlayerNotificationManager(
            Context context,
            String channelId,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter,
            @Nullable NotificationListener notificationListener) {
        this(
                context,
                channelId,
                notificationId,
                mediaDescriptionAdapter,
                notificationListener,
                /* customActionReceiver*/ null);
    }

    public PlayerNotificationManager(
            Context context,
            String channelId,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter,
            @Nullable CustomActionReceiver customActionReceiver) {
        this(
                context,
                channelId,
                notificationId,
                mediaDescriptionAdapter,
                /* notificationListener */ null,
                customActionReceiver);
    }


    public PlayerNotificationManager(
            Context context,
            String channelId,
            int notificationId,
            MediaDescriptionAdapter mediaDescriptionAdapter,
            @Nullable NotificationListener notificationListener,
            @Nullable CustomActionReceiver customActionReceiver) {
        context = context.getApplicationContext();
        this.context = context;
        this.channelId = channelId;
        this.notificationId = notificationId;
        this.mediaDescriptionAdapter = mediaDescriptionAdapter;
        this.notificationListener = notificationListener;
        this.customActionReceiver = customActionReceiver;
        controlDispatcher = new DefaultControlDispatcher();
        window = new Timeline.Window();
        instanceId = instanceIdCounter++;
        //noinspection Convert2MethodRef
        mainHandler =
                Util.createHandler(
                        Looper.getMainLooper(), msg -> PlayerNotificationManager.this.handleMessage(msg));
        notificationManager = NotificationManagerCompat.from(context);
        playerListener = new PlayerListener();
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        intentFilter = new IntentFilter();
        useNavigationActions = true;
        usePlayPauseActions = true;
        colorized = true;
        useChronometer = true;
        color = Color.TRANSPARENT;
        smallIconResourceId = R.drawable.exo_notification_small_icon;
        defaults = 0;
        priority = NotificationCompat.PRIORITY_LOW;
        badgeIconType = NotificationCompat.BADGE_ICON_SMALL;
        visibility = NotificationCompat.VISIBILITY_PUBLIC;

        // initialize actions
        playbackActions = createPlaybackActions(context, instanceId);
        for (String action : playbackActions.keySet()) {
            intentFilter.addAction(action);
        }
        customActions =
                customActionReceiver != null
                        ? customActionReceiver.createCustomActions(context, instanceId)
                        : Collections.emptyMap();
        for (String action : customActions.keySet()) {
            intentFilter.addAction(action);
        }
        dismissPendingIntent = createBroadcastIntent(ACTION_DISMISS, context, instanceId);
        intentFilter.addAction(ACTION_DISMISS);
    }


    public final void setPlayer(@Nullable Player player) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
        Assertions.checkArgument(
                player == null || player.getApplicationLooper() == Looper.getMainLooper());
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(playerListener);
            if (player == null) {
                stopNotification(/* dismissedByUser= */ false);
            }
        }
        this.player = player;
        if (player != null) {
            player.addListener(playerListener);
            postStartOrUpdateNotification();
        }
    }


    public void setPlaybackPreparer(@Nullable PlaybackPreparer playbackPreparer) {
        this.playbackPreparer = playbackPreparer;
    }


    public final void setControlDispatcher(ControlDispatcher controlDispatcher) {
        if (this.controlDispatcher != controlDispatcher) {
            this.controlDispatcher = controlDispatcher;
            invalidate();
        }
    }


    @Deprecated
    public final void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }


    public final void setUseNavigationActions(boolean useNavigationActions) {
        if (this.useNavigationActions != useNavigationActions) {
            this.useNavigationActions = useNavigationActions;
            invalidate();
        }
    }

    public final void setUseNavigationActionsInCompactView(
            boolean useNavigationActionsInCompactView) {
        if (this.useNavigationActionsInCompactView != useNavigationActionsInCompactView) {
            this.useNavigationActionsInCompactView = useNavigationActionsInCompactView;
            invalidate();
        }
    }

    public final void setUsePlayPauseActions(boolean usePlayPauseActions) {
        if (this.usePlayPauseActions != usePlayPauseActions) {
            this.usePlayPauseActions = usePlayPauseActions;
            invalidate();
        }
    }


    public final void setUseStopAction(boolean useStopAction) {
        if (this.useStopAction == useStopAction) {
            return;
        }
        this.useStopAction = useStopAction;
        invalidate();
    }

    public final void setMediaSessionToken(MediaSessionCompat.Token token) {
        if (!Util.areEqual(this.mediaSessionToken, token)) {
            mediaSessionToken = token;
            invalidate();
        }
    }


    public final void setBadgeIconType(@NotificationCompat.BadgeIconType int badgeIconType) {
        if (this.badgeIconType == badgeIconType) {
            return;
        }
        switch (badgeIconType) {
            case NotificationCompat.BADGE_ICON_NONE:
            case NotificationCompat.BADGE_ICON_SMALL:
            case NotificationCompat.BADGE_ICON_LARGE:
                this.badgeIconType = badgeIconType;
                break;
            default:
                throw new IllegalArgumentException();
        }
        invalidate();
    }


    public final void setColorized(boolean colorized) {
        if (this.colorized != colorized) {
            this.colorized = colorized;
            invalidate();
        }
    }


    public final void setDefaults(int defaults) {
        if (this.defaults != defaults) {
            this.defaults = defaults;
            invalidate();
        }
    }


    public final void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidate();
        }
    }


    public final void setPriority(@Priority int priority) {
        if (this.priority == priority) {
            return;
        }
        switch (priority) {
            case NotificationCompat.PRIORITY_DEFAULT:
            case NotificationCompat.PRIORITY_MAX:
            case NotificationCompat.PRIORITY_HIGH:
            case NotificationCompat.PRIORITY_LOW:
            case NotificationCompat.PRIORITY_MIN:
                this.priority = priority;
                break;
            default:
                throw new IllegalArgumentException();
        }
        invalidate();
    }


    public final void setSmallIcon(@DrawableRes int smallIconResourceId) {
        if (this.smallIconResourceId != smallIconResourceId) {
            this.smallIconResourceId = smallIconResourceId;
            invalidate();
        }
    }

    public final void setUseChronometer(boolean useChronometer) {
        if (this.useChronometer != useChronometer) {
            this.useChronometer = useChronometer;
            invalidate();
        }
    }

    public final void setVisibility(@Visibility int visibility) {
        if (this.visibility == visibility) {
            return;
        }
        switch (visibility) {
            case NotificationCompat.VISIBILITY_PRIVATE:
            case NotificationCompat.VISIBILITY_PUBLIC:
            case NotificationCompat.VISIBILITY_SECRET:
                this.visibility = visibility;
                break;
            default:
                throw new IllegalStateException();
        }
        invalidate();
    }

    /**
     * Forces an update of the notification if already started.
     */
    public void invalidate() {
        if (isNotificationStarted) {
            postStartOrUpdateNotification();
        }
    }

    // We're calling a deprecated listener method that we still want to notify.
    @SuppressWarnings("deprecation")
    private void startOrUpdateNotification(Player player, @Nullable Bitmap bitmap) {
        boolean ongoing = getOngoing(player);
        builder = createNotification(player, builder, ongoing, bitmap);
        if (builder == null) {
            stopNotification(/* dismissedByUser= */ false);
            return;
        }
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
        if (!isNotificationStarted) {
            isNotificationStarted = true;
            context.registerReceiver(notificationBroadcastReceiver, intentFilter);
            if (notificationListener != null) {
                notificationListener.onNotificationStarted(notificationId, notification);
            }
        }
        @Nullable NotificationListener listener = notificationListener;
        if (listener != null) {
            listener.onNotificationPosted(notificationId, notification, ongoing);
        }
    }

    // We're calling a deprecated listener method that we still want to notify.
    @SuppressWarnings("deprecation")
    private void stopNotification(boolean dismissedByUser) {
        if (isNotificationStarted) {
            isNotificationStarted = false;
            mainHandler.removeMessages(MSG_START_OR_UPDATE_NOTIFICATION);
            notificationManager.cancel(notificationId);
            context.unregisterReceiver(notificationBroadcastReceiver);
            if (notificationListener != null) {
                notificationListener.onNotificationCancelled(notificationId, dismissedByUser);
                notificationListener.onNotificationCancelled(notificationId);
            }
        }
    }


    @Nullable
    protected NotificationCompat.Builder createNotification(
            Player player,
            @Nullable NotificationCompat.Builder builder,
            boolean ongoing,
            @Nullable Bitmap largeIcon) {
        if (player.getPlaybackState() == Player.STATE_IDLE
                && (player.getCurrentTimeline().isEmpty() || playbackPreparer == null)) {
            builderActions = null;
            return null;
        }

        List<String> actionNames = getActions(player);
        List<NotificationCompat.Action> actions = new ArrayList<>(actionNames.size());
        for (int i = 0; i < actionNames.size(); i++) {
            String actionName = actionNames.get(i);
            @Nullable
            NotificationCompat.Action action =
                    playbackActions.containsKey(actionName)
                            ? playbackActions.get(actionName)
                            : customActions.get(actionName);
            if (action != null) {
                actions.add(action);
            }
        }

        if (builder == null || !actions.equals(builderActions)) {
            builder = new NotificationCompat.Builder(context, channelId);
            builderActions = actions;
            for (int i = 0; i < actions.size(); i++) {
                builder.addAction(actions.get(i));
            }
        }

        /*

        Custom MediaCustomViewStyle. For more info: https://developer.android.com/reference/android/app/Notification.DecoratedMediaCustomViewStyle

         */

        MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
        if (mediaSessionToken != null) {
            mediaStyle.setMediaSession(mediaSessionToken);
        }
        mediaStyle.setShowActionsInCompactView(getActionIndicesForCompactView(actionNames, player));
        // Configure dismiss action prior to API 21 ('x' button).
        mediaStyle.setShowCancelButton(!ongoing);
        mediaStyle.setCancelButtonIntent(dismissPendingIntent);
        builder.setStyle(mediaStyle);

        // Set intent which is sent if the user selects 'clear all'
        builder.setDeleteIntent(dismissPendingIntent);

        builder.setCustomContentView(getCustomDesign()); //Custom Design.


        //builder.setCustomBigContentView(getCustomDesign());  // Maybe somebody can use it :)


        // Set notification properties from getters.
        builder
                .setBadgeIconType(badgeIconType)
                .setOngoing(ongoing)
                .setColor(color)
                .setColorized(colorized)
                .setSmallIcon(smallIconResourceId)
                .setVisibility(visibility)
                .setPriority(priority)
                .setDefaults(defaults);

        // Changing "showWhen" causes notification flicker if SDK_INT < 21.
        if (Util.SDK_INT >= 21
                && useChronometer
                && player.isPlaying()
                && !player.isPlayingAd()
                && !player.isCurrentWindowDynamic()
                && player.getPlaybackParameters().speed == 1f) {
            builder
                    .setWhen(System.currentTimeMillis() - player.getContentPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            builder.setShowWhen(false).setUsesChronometer(false);
        }

        // Set media specific notification properties from MediaDescriptionAdapter.
        // builder.setContentTitle(mediaDescriptionAdapter.getCurrentContentTitle(player));
        // builder.setContentText(mediaDescriptionAdapter.getCurrentContentText(player));
        builder.setSubText(mediaDescriptionAdapter.getCurrentSubText(player));
        if (largeIcon == null) {
            largeIcon =
                    mediaDescriptionAdapter.getCurrentLargeIcon(
                            player, new BitmapCallback(++currentNotificationTag));
        }
        setLargeIcon(builder, largeIcon);
        builder.setContentIntent(mediaDescriptionAdapter.createCurrentContentIntent(player));

        return builder;
    }

    private RemoteViews getCustomDesign() {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                R.layout.playback_notification);

        remoteViews.setTextViewText(R.id.notification_title, mediaDescriptionAdapter.getCurrentContentTitle(player));  // Notification Title

        return remoteViews;
    }


    protected List<String> getActions(Player player) {
        boolean enablePrevious = false;
        boolean enableRewind = false;
        boolean enableFastForward = false;
        boolean enableNext = false;
        Timeline timeline = player.getCurrentTimeline();
        if (!timeline.isEmpty() && !player.isPlayingAd()) {
            timeline.getWindow(player.getCurrentWindowIndex(), window);
            enablePrevious = window.isSeekable || !window.isDynamic || player.hasPrevious();
            // enableRewind = controlDispatcher.isRewindEnabled();
            // enableFastForward = controlDispatcher.isFastForwardEnabled();
            enableNext = window.isDynamic || player.hasNext();
        }

        List<String> stringActions = new ArrayList<>();
        if (useNavigationActions && enablePrevious) {
            stringActions.add(ACTION_PREVIOUS);
        }
        if (enableRewind) {
            stringActions.add(ACTION_REWIND);
        }
        if (usePlayPauseActions) {
            if (shouldShowPauseButton(player)) {
                stringActions.add(ACTION_PAUSE);
            } else {
                stringActions.add(ACTION_PLAY);
            }
        }
        if (enableFastForward) {
            stringActions.add(ACTION_FAST_FORWARD);
        }
        if (useNavigationActions && enableNext) {
            stringActions.add(ACTION_NEXT);
        }
        if (customActionReceiver != null) {
            stringActions.addAll(customActionReceiver.getCustomActions(player));
        }
        if (useStopAction) {
            stringActions.add(ACTION_STOP);
        }
        return stringActions;
    }


    @SuppressWarnings("unused")
    protected int[] getActionIndicesForCompactView(List<String> actionNames, Player player) {
        int pauseActionIndex = actionNames.indexOf(ACTION_PAUSE);
        int playActionIndex = actionNames.indexOf(ACTION_PLAY);
        int skipPreviousActionIndex =
                useNavigationActionsInCompactView ? actionNames.indexOf(ACTION_PREVIOUS) : -1;
        int skipNextActionIndex =
                useNavigationActionsInCompactView ? actionNames.indexOf(ACTION_NEXT) : -1;

        int[] actionIndices = new int[3];
        int actionCounter = 0;
        if (skipPreviousActionIndex != -1) {
            actionIndices[actionCounter++] = skipPreviousActionIndex;
        }
        boolean shouldShowPauseButton = shouldShowPauseButton(player);
        if (pauseActionIndex != -1 && shouldShowPauseButton) {
            actionIndices[actionCounter++] = pauseActionIndex;
        } else if (playActionIndex != -1 && !shouldShowPauseButton) {
            actionIndices[actionCounter++] = playActionIndex;
        }
        if (skipNextActionIndex != -1) {
            actionIndices[actionCounter++] = skipNextActionIndex;
        }
        return Arrays.copyOf(actionIndices, actionCounter);
    }

    /**
     * Returns whether the generated notification should be ongoing.
     */
    protected boolean getOngoing(Player player) {
        int playbackState = player.getPlaybackState();
        return (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY)
                && player.getPlayWhenReady();
    }

    private boolean shouldShowPauseButton(Player player) {
        return player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    private void postStartOrUpdateNotification() {
        if (!mainHandler.hasMessages(MSG_START_OR_UPDATE_NOTIFICATION)) {
            mainHandler.sendEmptyMessage(MSG_START_OR_UPDATE_NOTIFICATION);
        }
    }

    private void postUpdateNotificationBitmap(Bitmap bitmap, int notificationTag) {
        mainHandler
                .obtainMessage(
                        MSG_UPDATE_NOTIFICATION_BITMAP, notificationTag, C.INDEX_UNSET /* ignored */, bitmap)
                .sendToTarget();
    }

    private boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START_OR_UPDATE_NOTIFICATION:
                if (player != null) {
                    startOrUpdateNotification(player, /* bitmap= */ null);
                }
                break;
            case MSG_UPDATE_NOTIFICATION_BITMAP:
                if (player != null && isNotificationStarted && currentNotificationTag == msg.arg1) {
                    startOrUpdateNotification(player, (Bitmap) msg.obj);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private static Map<String, NotificationCompat.Action> createPlaybackActions(
            Context context, int instanceId) {
        Map<String, NotificationCompat.Action> actions = new HashMap<>();
        actions.put(
                ACTION_PLAY,
                new NotificationCompat.Action(
                        R.drawable.exo_notification_play,
                        context.getString(R.string.exo_controls_play_description),
                        createBroadcastIntent(ACTION_PLAY, context, instanceId)));
        actions.put(
                ACTION_PAUSE,
                new NotificationCompat.Action(
                        R.drawable.exo_notification_pause,
                        context.getString(R.string.exo_controls_pause_description),
                        createBroadcastIntent(ACTION_PAUSE, context, instanceId)));
        actions.put(
                ACTION_STOP,
                new NotificationCompat.Action(
                        R.drawable.exo_notification_stop,
                        context.getString(R.string.exo_controls_stop_description),
                        createBroadcastIntent(ACTION_STOP, context, instanceId)));


        return actions;
    }

    private static PendingIntent createBroadcastIntent(
            String action, Context context, int instanceId) {
        Intent intent = new Intent(action).setPackage(context.getPackageName());
        intent.putExtra(EXTRA_INSTANCE_ID, instanceId);
        return PendingIntent.getBroadcast(
                context, instanceId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressWarnings("nullness:argument.type.incompatible")
    private static void setLargeIcon(NotificationCompat.Builder builder, @Nullable Bitmap largeIcon) {
        builder.setLargeIcon(largeIcon);
    }

    private class PlayerListener implements Player.EventListener {


        public void onPlaybackStateChanged(@Player.State int playbackState) {
            postStartOrUpdateNotification();
        }


        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            postStartOrUpdateNotification();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            postStartOrUpdateNotification();
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            postStartOrUpdateNotification();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            postStartOrUpdateNotification();
        }

        @Override
        public void onRepeatModeChanged(@Player.RepeatMode int repeatMode) {
            postStartOrUpdateNotification();
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            postStartOrUpdateNotification();
        }
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Player player = PlayerNotificationManager.this.player;
            if (player == null
                    || !isNotificationStarted
                    || intent.getIntExtra(EXTRA_INSTANCE_ID, instanceId) != instanceId) {
                return;
            }
            String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                if (player.getPlaybackState() == Player.STATE_IDLE) {
                    if (playbackPreparer != null) {
                        playbackPreparer.preparePlayback();
                    }
                } else if (player.getPlaybackState() == Player.STATE_ENDED) {
                    controlDispatcher.dispatchSeekTo(player, player.getCurrentWindowIndex(), C.TIME_UNSET);
                }
                controlDispatcher.dispatchSetPlayWhenReady(player, /* playWhenReady= */ true);

                EventBus.getDefault().post(new ButtonEvent(true));
                Log.d(TAG, "ButtonEvent: True");


            } else if (ACTION_PAUSE.equals(action)) {
                controlDispatcher.dispatchSetPlayWhenReady(player, /* playWhenReady= */ false);

                if (player.getPlaybackState() == Player.STATE_READY)
                {
                    EventBus.getDefault().post(new ButtonEvent(false));
                    Log.d(TAG, "ButtonEvent: False");
                }


            } else if (ACTION_STOP.equals(action)) {
                controlDispatcher.dispatchStop(player, /* reset= */ true);
            } else if (ACTION_DISMISS.equals(action)) {
                stopNotification(/* dismissedByUser= */ true);
            } else if (action != null
                    && customActionReceiver != null
                    && customActions.containsKey(action)) {
                customActionReceiver.onCustomAction(player, action, intent);
            }
        }
    }
}
