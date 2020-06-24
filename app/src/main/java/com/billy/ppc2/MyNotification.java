package com.billy.ppc2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class MyNotification {
    private final int NOTIFICATION_ID = 123;
    private Context context;
    private String myChannelId = "CHANNEL";

    public MyNotification(String msg, Context context) {
        this.context = context;
        buildMyChannel();
        actualNotificationFire(msg);
    }

    private void buildMyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "my awesome channel";
            String description = "sending sms channel";
            NotificationChannel channel = new NotificationChannel(myChannelId, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void actualNotificationFire(String myMsg) {
        android.app.Notification notification = new NotificationCompat.Builder(context, myChannelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(myMsg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
