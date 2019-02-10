package com.amsavarthan.lvstore;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by amsavarthan on 10/2/19.
 */

public class NotificationUtil {

    private static String TAG = NotificationUtil.class.getSimpleName();

    private Context mContext;

    public NotificationUtil(Context mContext) {
        this.mContext = mContext;
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void clearNotificationsById(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static long getTimeMilliSec(String timeStamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showNotificationMessage(int id, String timeStamp, String channelName, String channelDesc, String title, String message, Intent intent) {
        showNotificationMessage(id, timeStamp, channelName, channelDesc, title, message, intent, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager, String channelName, String channelDesc) {
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel("admin_id", channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDesc);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.BLUE);
        adminChannel.canShowBadge();
        adminChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public void showNotificationMessage(int id, String timeStamp, String channelName, String channelDesc, final String title, final String message, Intent intent, String imageUrl) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // notification icon
        final int icon = R.drawable.ic_notification;

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext, "admin_id");


        try {
            if (!TextUtils.isEmpty(imageUrl)) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);


                if (bitmap != null) {
                    showBigNotification(id, timeStamp, channelName, channelDesc, bitmap, mBuilder, icon, title, message, resultPendingIntent);
                } else {
                    showSmallNotification(id, timeStamp, channelName, channelDesc, mBuilder, icon, title, message, resultPendingIntent);
                }

            } else {
                showSmallNotification(id, timeStamp, channelName, channelDesc, mBuilder, icon, title, message, resultPendingIntent);
            }
        } catch (Exception e) {
            Log.e("showNotificationMessage", e.getMessage());
        }


    }

    private void showSmallNotification(int id, String timeStamp, String channelName, String channelDesc, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager, channelName, channelDesc);
        }

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setShowWhen(true)
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigTextStyle)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(message)
                .build();

        notificationManager.notify(id, notification);
    }

    private void showBigNotification(int id, String timeStamp, String channelName, String channelDesc, Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.bigPicture(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager, channelName, channelDesc);
        }

        Notification notification;

        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setShowWhen(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(message)
                .build();

        notificationManager.notify(id, notification);
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
