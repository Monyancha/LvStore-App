package com.amsavarthan.lvstore;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = MessagingService.class.getSimpleName();

    private NotificationUtil notificationUtils;
    private String cDesc;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleDataMessage(remoteMessage);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String imageUrl=remoteMessage.getData().get("imageUrl");
        String timeStamp=remoteMessage.getData().get("timestamp");
        String doc_id=remoteMessage.getData().get("doc_id");
        String update_link=remoteMessage.getData().get("update_link");
        long id = System.currentTimeMillis();

        Intent resultIntent=new Intent(getApplicationContext(),AppDetails.class);
        resultIntent.putExtra("doc_id",doc_id);

        try {
            boolean foreground=new ForegroundCheckTask().execute(getApplicationContext()).get();

            if(!foreground){

                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage((int) id, timeStamp, "App Updates", cDesc, getApplicationContext(), title, body, resultIntent);
                } else {
                    showNotificationMessageWithBigImage((int) id, timeStamp, "App Updates", cDesc, getApplicationContext(), title, body, resultIntent, imageUrl);
                }

            }else{

                boolean active= getSharedPreferences("fcm_activity",MODE_PRIVATE).getBoolean("active",true);

                if(active) {
                    Intent intent = new Intent("pushNotification");

                    intent.putExtra("title", title);
                    intent.putExtra("body", body);
                    intent.putExtra("notification_id", id);

                    showNotificationMessage((int) id, timeStamp, "App Updates", cDesc, getApplicationContext(), title, body, resultIntent);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                }else{

                    if (TextUtils.isEmpty(imageUrl)) {
                        showNotificationMessage((int) id, timeStamp, "App Updates", cDesc, getApplicationContext(), title, body, resultIntent);
                    } else {
                        showNotificationMessageWithBigImage((int) id, timeStamp,"App Updates", cDesc, getApplicationContext(), title, body, resultIntent, imageUrl);
                    }


                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }

    private void showNotificationMessage(int id, String timeStamp, String channelName, String channelDesc, Context context, String title, String message, Intent intent) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, channelName, channelDesc, title, message, intent, null);
    }

    private void showNotificationMessageWithBigImage(int id, String timeStamp, String channelName, String channelDesc, Context context, String title, String message, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, channelName, channelDesc, title, message, intent, imageUrl);
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
