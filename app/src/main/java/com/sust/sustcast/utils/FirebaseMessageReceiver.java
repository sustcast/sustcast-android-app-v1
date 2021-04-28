package com.sust.sustcast.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sust.sustcast.R;
import com.sust.sustcast.fragment.FragmentHolder;

import static com.sust.sustcast.R.drawable.ic_small_notification_icon;
import static com.sust.sustcast.R.drawable.sustcast_logo_circle_only;

public class FirebaseMessageReceiver
        extends FirebaseMessagingService {
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference tokenRef;
    @Override
    public void
    onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification() != null) {
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }
    @Override
    public void onNewToken(@NonNull String token) {
        tokenRef = rootRef.child("tokens");
        tokenRef.push().setValue(token);
    }
    private RemoteViews getCustomDesign(String title,
                                        String message) {
        RemoteViews remoteViews = new RemoteViews(
                getApplicationContext().getPackageName(),
                R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);

        return remoteViews;
    }

    public void showNotification(String title,
                                 String message) {
        Intent intent
                = new Intent(this, FragmentHolder.class);
        String channel_id = "notification_channel";

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent
                = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder
                = new NotificationCompat
                .Builder(getApplicationContext(),
                channel_id)
                .setSmallIcon(ic_small_notification_icon)
                .setColor(Color.parseColor("#FED500"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title))
                .setAutoCancel(true)
                .setVibrate(new long[]{400, 400, 300,
                        400, 300})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
        System.out.println("BuildVersion : " + Build.VERSION.SDK_INT);
        builder = builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(ic_small_notification_icon)
                .setColor(Color.parseColor("#FED500"))
                .setPriority(2)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title));

        NotificationManager notificationManager
                = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel
                    = new NotificationChannel(
                    channel_id, "web_app",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(
                    notificationChannel);
        }
        notificationManager.notify(0, builder.build());
    }
}
