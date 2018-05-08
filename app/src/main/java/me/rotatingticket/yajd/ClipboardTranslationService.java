package me.rotatingticket.yajd;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import me.rotatingticket.yajd.dict.Dict;
import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.viewmodel.MainActivityViewModel;


/**
 * Service for clipboard translation.
 */
public class ClipboardTranslationService extends Service {
    private static final String SERVICE_NOTIFICATION_CHANNEL_ID = "service";
    private static final int NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private static final int NOTIFICATION_ID = 110;

    private ClipboardManager.OnPrimaryClipChangedListener onPrimaryClipChangedListener;
    private ClipboardManager clipboardManager;
    private Handler mainHandler;
    private Dict dict;
    private NotificationManager notificationManager;

    public ClipboardTranslationService() {
        super();
    }

    private void prepareClipboardListener() {
        prepareDict();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mainHandler = new Handler(Looper.getMainLooper());
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        onPrimaryClipChangedListener = () -> {
            // only process text in clipboard
            ClipData clipData = clipboardManager.getPrimaryClip();
            String text = clipData.getItemAt(0).coerceToText(this).toString();
            if (text.length() != 0) {
                AsyncTask.execute(() -> queryAndShow(text));
            }
        };
    }

    /**
     * Query the result of text and show the result on screen.
     * Should not be invoked in main thread because Room query may block it.
     * @param text Text to query.
     */
    private void queryAndShow(String text) {
        List<? extends WordEntry> wordEntries = dict.userQuery(text);

        if (wordEntries.size() == 0) {
            showNotFound(text);
        } else {
            showQueryResult(text, wordEntries);
        }
    }


    /**
     * Show the query result.
     * Now it will show it by toast simply.
     * @param query The query text.
     * @param wordEntries The result word entries.
     */
    private void showQueryResult(String query, List<? extends WordEntry> wordEntries) {
        StringWriter resultWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(resultWriter);

        for (WordEntry wordEntry : wordEntries) {
            printWriter.print(wordEntry.getWord());
            printWriter.print(" ");
            printWriter.println(StringUtils.join(wordEntry.getRomajis(), "; "));
            printWriter.println(wordEntry.getDescription());
            printWriter.println();
        }

        String result = resultWriter.toString().trim();
        showTextInToast(result);
    }

    /**
     * Show that the corresponding word of the query is not found.
     * Now it will only show one message in a toast.
     * @param query Query to show
     */
    private void showNotFound(String query) {
        showTextInToast(getString(R.string.clipboard_service_word_not_found));
    }

    /**
     * Show text in the toast.
     * @param text Text to show.
     */
    private void showTextInToast(String text) {
        // toast must create on the ui thread
        mainHandler.post(() -> Toast.makeText(getBaseContext(),
              text,
              Toast.LENGTH_LONG).show());
    }

    /**
     * Prepare dictionary.
     */
    private void prepareDict() {
        dict = MainActivityViewModel.prepareDict(getBaseContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prepareClipboardListener();
        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener);

        PendingIntent notificationIntent = PendingIntent.getActivity(this,
              0,
              new Intent(this, MainActivity.class),
              0);
        Notification notification = createForegroundNotification(notificationIntent);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String id, String name, int importance) {
        if (notificationManager.getNotificationChannel(id) != null){
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private Notification createForegroundNotification(PendingIntent contentIntent) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(SERVICE_NOTIFICATION_CHANNEL_ID,
                  getString(R.string.service_notification_channel_name),
                  NOTIFICATION_CHANNEL_IMPORTANCE);
            notification = new Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
                  .setOngoing(true)
                  .setContentIntent(contentIntent)
                  .setSmallIcon(R.drawable.ic_edit)
                  .setContentTitle(getString(R.string.clipboard_service_notification))
                  .setContentText(getString(R.string.clipboard_service_notification_content))
                  .setWhen(System.currentTimeMillis())
                  .setAutoCancel(true)
                  .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                  .setOngoing(true)
                  .setContentIntent(contentIntent)
                  .setSmallIcon(R.drawable.ic_edit)
                  .setContentTitle(getString(R.string.clipboard_service_notification))
                  .setContentText(getString(R.string.clipboard_service_notification_content))
                  .setWhen(System.currentTimeMillis())
                  .setPriority(NotificationCompat.PRIORITY_MAX)
                  .setAutoCancel(true)
                  .build();
        }
        return notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove listener
        if (clipboardManager != null && onPrimaryClipChangedListener != null) {
            clipboardManager.removePrimaryClipChangedListener(onPrimaryClipChangedListener);
        }
        stopForeground(true);
    }
}
