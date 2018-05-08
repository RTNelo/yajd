package me.rotatingticket.yajd.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;

import me.rotatingticket.yajd.R;


/**
 * Base for Service running on foreground.
 */
public abstract class BaseForegroundService extends Service {
    private static final String SERVICE_NOTIFICATION_CHANNEL_ID = "service";
    private static final int NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Create NotificationChannel for Android Oreo+
     * @param id NotificationChannel id.
     * @param name NotificationChannel name.
     * @param importance NotificationChannel importance.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void createNotificationChannel(String id, String name, int importance) {
        // skip if already have the channel
        if (notificationManager.getNotificationChannel(id) != null){
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    /**
     * Create the notification for the foreground service.
     * Will automatically create NotificationChannel under Android Oreo+
     * @param contentIntent The content intent for the notification.
     * @param drawable The small icon for the notfication.
     * @param title The title for the notification.
     * @param content The content for the notification.
     * @return The notification for foreground service.
     */
    protected Notification createForegroundNotification(PendingIntent contentIntent,
                                                      int drawable,
                                                      String title,
                                                      String content) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(SERVICE_NOTIFICATION_CHANNEL_ID,
                  getString(R.string.service_notification_channel_name),
                  NOTIFICATION_CHANNEL_IMPORTANCE);
            notification = new Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
                  .setOngoing(true)
                  .setContentIntent(contentIntent)
                  .setSmallIcon(drawable)
                  .setContentTitle(title)
                  .setContentText(content)
                  .setWhen(System.currentTimeMillis())
                  .setAutoCancel(true)
                  .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                  .setOngoing(true)
                  .setContentIntent(contentIntent)
                  .setSmallIcon(drawable)
                  .setContentTitle(title)
                  .setContentText(content)
                  .setWhen(System.currentTimeMillis())
                  .setPriority(NotificationCompat.PRIORITY_MAX)
                  .setAutoCancel(true)
                  .build();
        }
        return notification;
    }
}
