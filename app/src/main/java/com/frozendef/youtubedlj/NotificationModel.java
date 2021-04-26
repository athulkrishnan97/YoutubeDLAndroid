package com.frozendef.youtubedlj;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationModel {

    Context context;


    NotificationModel(Context context){
        this.context=context;

    }

    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManager;

    protected void showInitialNotification(){


        int notificationId =1;
        notificationManager = NotificationManagerCompat.from(context);
        builder = new NotificationCompat.Builder(context, "downloadNotificationChannel");
        builder.setContentTitle("YoutubeDLJ")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setNotificationSilent();
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(notificationId, builder.build());

       /* builder.setContentText("Download complete")
                .setProgress(0,0,false);
        notificationManager.notify(notificationId, builder.build());*/


    }


    protected void updateNotification(int max, int current,int notificationId){
        if(current==100)builder.setContentText("Finishing Up...");
        builder.setProgress(max,current,false);
        notificationManager.notify(notificationId,builder.build());


    }

    protected void completeNotification(int notificationId,boolean successful,String videoName){

        NotificationCompat.Builder completedBuilder = new NotificationCompat.Builder(context, "downloadNotificationChannel");
        completedBuilder.setContentTitle(videoName)
                .setSmallIcon(R.drawable.download)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(false);

        if(successful) {
            completedBuilder.setContentText("Download Completed");

        }
        else {
            completedBuilder.setContentText("Download Failed");
        }
        notificationManager.notify(notificationId, completedBuilder.build());

    }


    protected void cancelAllNotification(){
        notificationManager.cancelAll();
    }


    protected void createDownloadNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download Notification";
            String description = "Notification for download progress";
            String channelId="downloadNotificationChannel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }




}
