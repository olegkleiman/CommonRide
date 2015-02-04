package com.labs.okey.commonride;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.microsoft.windowsazure.mobileservices.notifications.MobileServicePush;
import com.microsoft.windowsazure.mobileservices.notifications.Registration;
import com.microsoft.windowsazure.mobileservices.notifications.RegistrationCallback;

/**
 * Created by Oleg on 25-Jan-15.
 */
public class GCMHandler extends  com.microsoft.windowsazure.notifications.NotificationsHandler{

    Context ctx;
    private NotificationManager mNotificationManager;

    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onRegistered(Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {
                    MobileServicePush push = MainActivity.wamsClient.getPush();
                    if( push != null ) {
                        push.register(gcmRegistrationId, null,
                                new RegistrationCallback(){

                                    @Override
                                    public void onRegister(Registration registration, Exception e) {

                                    }
                                });
                    }
                    return null;
                }
                catch(Exception e) {
                    String msg = e.getMessage();
                    // handle error
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        String nhMessage = bundle.getString("message");
        String rideId =  bundle.getString("extras");

        sendNotification(nhMessage, rideId);
    }

    private void sendNotification(String msg, String rideId) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(ctx, SingleRideActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId);
        launchIntent.putExtras(b);

        PendingIntent contentIntent =
                PendingIntent.getActivity(ctx, 0,
                                          launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setVibrate(new long[]{500, 500})
                        .setContentTitle("Common Ride")
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
