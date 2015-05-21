package com.labs.okey.annex;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.SingleRideActivity;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.notifications.MobileServicePush;
import com.microsoft.windowsazure.mobileservices.notifications.Registration;
import com.microsoft.windowsazure.mobileservices.notifications.RegistrationCallback;

/**
 * Created by Oleg Kleiman on 25-Jan-15.
 */
public class GCMHandler extends  com.microsoft.windowsazure.notifications.NotificationsHandler{

    Context ctx;

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onUnregistered(Context context, String gcmRegistrationId) {
        super.onUnregistered(context, gcmRegistrationId);
    }

    @Override
    public void onRegistered(final Context context,  final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String userID = sharedPrefs.getString(Globals.USERIDPREF, "");

        new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {
                try {

                    // Better use WAMS SDK v2 like:
                    //MainActivity.wamsClient.getPush().register(gcmRegistrationId, null);

                    MobileServicePush push = MainActivity.wamsClient.getPush();
                    if( push != null ) {

                        User user = User.load(context);
                        String[] tags = {user.getGroup(), userID};

                        push.register(gcmRegistrationId, tags,
                                new RegistrationCallback(){

                                    @Override
                                    public void onRegister(Registration registration,
                                                           Exception ex) {
                                        if( ex != null) {
                                            String msg =  ex.getMessage();
                                            Log.e("Registration error: " , msg);
                                        }
                                    }
                                });
                    }
                    return null;
                }
                catch(Exception e) {
                    String msg = e.getMessage();
                    Log.e("Registration error: ", msg);
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

        String title = context.getResources().getString(R.string.app_label);

        sendNotification(nhMessage, rideId, title);
    }

    private void sendNotification(String msg, String rideId, String title) {
        NotificationManager mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(ctx, SingleRideActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId);
        launchIntent.putExtras(b);

        PendingIntent contentIntent =
                PendingIntent.getActivity(ctx, 0,
                                          launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Use TaskStackBuilder to build the back stack and get the PendingIntent
//        PendingIntent pendingIntent =
//                TaskStackBuilder.create(ctx)
//                        // add all of DetailsActivity's parents to the stack,
//                        // followed by DetailsActivity itself
//                        .addNextIntentWithParentStack(launchIntent)
//                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.launcher_48)
                        .setVibrate(new long[]{500, 500})
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        //mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
