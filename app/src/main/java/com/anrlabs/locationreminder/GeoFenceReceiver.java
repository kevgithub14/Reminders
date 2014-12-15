package com.anrlabs.locationreminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.anrlabs.reminders.DatabaseHelper;
import com.anrlabs.reminders.MainActivity;
import com.anrlabs.reminders.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.Iterator;
import java.util.List;

/**
 * Created by sandeepkannan on 12/9/14.
 */
public class GeoFenceReceiver extends BroadcastReceiver {
    Context context;

    Intent broadcastIntent = new Intent();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        broadcastIntent.addCategory(GeoFenceConsants.CATEGORY_LOCATION_SERVICES);
        handleEnter(intent);

    }




    private void handleEnter(Intent intent) {
        int transition = LocationClient.getGeofenceTransition(intent);
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Post a notification
            List<Geofence> geofences = LocationClient
                    .getTriggeringGeofences(intent);
            String[] ids = new String[geofences.size()];
            int index=0;
           for (Geofence geofence : geofences) {

               ids[index]= geofence.getRequestId();

           }
            index=0;
            List<String> lst = DatabaseHelper.getInstance(this.context).loadTitlesForNotification(ids);
           for (String  str :lst) {
               sendNotification((String)lst.get(index));
           }
        } else {
            // Always log as an error
         }
    }

    private void sendNotification(String title) {

        Intent notificationIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText( "Click here to open app")
                .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    public static String getErrorString(Context context, int errorCode) {

        // Get a handle to resources, to allow the method to retrieve messages.
        Resources mResources = context.getResources();

        // Define a string to contain the error message
        String errorString;

        // Decide which error message to get, based on the error code.
        switch (errorCode) {

            case ConnectionResult.DEVELOPER_ERROR:
                errorString = "DEVELOPER ERROR";
                break;

            case ConnectionResult.INTERNAL_ERROR:
                errorString = "INTERNAL ERROR";
                break;

            case ConnectionResult.INVALID_ACCOUNT:
                errorString = "The Account is invalid";
                break;

            case ConnectionResult.LICENSE_CHECK_FAILED:
                errorString = "The licence check failed";
                break;

            case ConnectionResult.NETWORK_ERROR:
                errorString = "There was a problem connecting to network";
                break;

            case ConnectionResult.RESOLUTION_REQUIRED:
                errorString = "Addition information needed";
                break;

            case ConnectionResult.SERVICE_DISABLED:
                errorString = "Google play serivce disabled";
                break;

            case ConnectionResult.SERVICE_INVALID:
                errorString = "The version of Google play on this device is not matching";
                break;

            case ConnectionResult.SERVICE_MISSING:
                errorString = "Google play sermice missing";
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                errorString = "Please update the google play";
                break;

            case ConnectionResult.SIGN_IN_REQUIRED:
                errorString = "Please sign in to your google service";
                break;

            default:
                errorString = "Unknown error";
                break;
        }

        // Return the error message
        return errorString;
    }
}