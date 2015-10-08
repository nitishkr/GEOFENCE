package com.example.nitishkumar.geofence;

/**
 * Created by nitishkumar on 07/10/15.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {

    private final static String TAG = ReceiveTransitionsIntentService.class.getPackage() + "." + ReceiveTransitionsIntentService.class.getSimpleName();
    private int flag;
    private String email;
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
        Log.v(TAG, "Service Constructor");
        flag = 0;

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        email = intent.getExtras().getString("Email");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );


            sendNotification(geofenceTransitionDetails,"Hello There has been a transition");
            Log.i(TAG, geofenceTransitionDetails);
        } else {

            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }

    }


    private void sendNotification(String transitionType, String ids) {


        Intent notificationIntent =
                new Intent(getApplicationContext(),GooglePlayServicesActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);


        stackBuilder.addParentStack(GooglePlayServicesActivity.class);

        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(
                        getString(R.string.geofence_transition_notification_title,
                                transitionType, ids))
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }



    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                flag =1;
                Intent i = new Intent(this, SensorActivity.class);
                i.setAction("com.example.app1.sensor");
                i.addCategory("android.intent.category.DEFAULT");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("Email", email);
                i.putExtra("kill",0);
                startActivity(i);


                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:

                    Intent in = new Intent(this, GooglePlayServicesActivity.class);
                    in.putExtra("Email",email );
                    in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.setAction("com.example.app1.ABOUT");
                    in.addCategory("android.intent.category.DEFAULT");

                    startActivity(in);

                return getString(R.string.geofence_transition_exited);

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_transition_dwell);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

}