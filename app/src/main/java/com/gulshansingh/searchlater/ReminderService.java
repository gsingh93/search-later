package com.gulshansingh.searchlater;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReminderService extends IntentService {
    private static final String TAG = "ReminderService";

    public ReminderService() {
        super("Reminder Service");
    }

    private static int getReminderInterval(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int interval = sp.getInt("time_amount", 1) * 1000;
        String timeUnit = sp.getString("time_unit", "Days");
        if (timeUnit.equals("Days")) {
            interval *= 60 * 60 * 24;
        } else if (timeUnit.equals("Hours")) {
            interval *= 60 * 60;
        } else if (timeUnit.equals("Minutes")) {
            interval *= 60;
        } else {
            throw new RuntimeException("Invalid time unit");
        }

        return interval;
    }

    public static void startAlarm(Context context) {
        long when = System.currentTimeMillis() + getReminderInterval(context);
        Log.i(TAG, "Current time is " + System.currentTimeMillis());
        Log.i(TAG, "Alarm scheduled for " + when);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(getClass().getName(), "Received intent");

        int num_queries = PreferenceManager.getDefaultSharedPreferences(this).getInt("num_queries", 0);
        if (num_queries == 0) {
            Log.i(getClass().getName(), "No unsearched queries, not sending notification");
            return;
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent , 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Search Later")
                .setContentText("You have unsearched queries")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setColor(Color.DKGRAY)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();

        nm.notify(0, notification);
    }
}
