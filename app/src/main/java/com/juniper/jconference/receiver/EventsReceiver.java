package com.juniper.jconference.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.juniper.jconference.DynamicListAddActivity;
import com.juniper.jconference.JDialerMainActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.service.EventsService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.security.AccessController.getContext;

public class EventsReceiver extends BroadcastReceiver {
    private static final String TAG="BroadCastReceiver";
    Context context;
    private static int BASIC_NOTIFICATION_ID=100;
    String title,time,date,year,month,timezone;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        this.context=context;
        Log.i(TAG,"Inside Receiver");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            title= extras.getString("title");
             time= extras.getString("time");
             date= extras.getString("date");
             year= extras.getString("year");
            month=extras.getString("month");
            timezone=extras.getString("timezone");
        }
        //CallModel model=new CallModel();
        Log.i(TAG,"Inside Receiver onReceive");

        Log.i(TAG,title+" New Conference Call is Available");
        Log.i(TAG,time);
        Log.i(TAG,date);
        Log.i(TAG,year);
        Log.i(TAG,month);
        Log.i(TAG,timezone);
        //createNotification(context,title,time,date,year,month);
        notificationCustom(context,title,time,date,year,month);
        //Notification(context,intent.getStringExtra("New Conference Call is Available"));

    }
   /* public void Notification(Context context, String message) {
        // Set Notification Title

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(context, DynamicListAddActivity.class);
        // Send data to NotificationView Class

        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                .setTicker(title)
                // Set Title
                .setContentTitle(title+"\n"+"AT "+time+" "+month+" "+date+" "+year)
                // Set Text
                .setContentText(message)
                // Add an Action Button below Notification
                .addAction(R.drawable.viber, "Action Button", pIntent)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true)
                //set Vibrator
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
       *//* if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder.setSmallIcon(R.drawable.viber);
            builder.setContentTitle(title)
                   *//**//* .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText(notificationMessage))*//**//*
                    .setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                    .setLights(Color.DKGRAY, 500, 500)
                    .setContentText(title);
        } else {
            RemoteViews customNotificationView = new RemoteViews(context.getPackageName(),
                    R.layout.notification);

            customNotificationView.setTextViewText(R.id.title_display1, "App Testing");
            customNotificationView.setTextViewText(R.id.time_display1, "10:00");
            customNotificationView.setTextViewText(R.id.date_display1, "Jul 11");
            customNotificationView.setTextViewText(R.id.time_zone_display1, "2017");

            builder.setContent(customNotificationView);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setAutoCancel(true);
            builder.setDefaults(Notification.DEFAULT_SOUND);
            builder.setLights(Color.BLUE, 500, 500);
        }*//*
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }*/

  /*  // Check for network availability
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }*/

    private void createNotification(Context context,String event,String time,String date,String year,String month) {
        // BEGIN_INCLUDE(notificationCompat)
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        // END_INCLUDE(notificationCompat)

        // BEGIN_INCLUDE(intent)
        //Create Intent to launch this Activity again if the notification is clicked.
        Intent i = new Intent(context, JDialerMainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 123, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        // END_INCLUDE(intent)

        // BEGIN_INCLUDE(ticker)
        // Sets the ticker text
        builder.setTicker(context.getResources().getString(R.string.app_name));

        // Sets the small icon for the ticker
        builder.setSmallIcon(R.drawable.viber);
        // END_INCLUDE(ticker)

        // BEGIN_INCLUDE(buildNotification)
        // Cancel the notification when clicked
        builder.setAutoCancel(true);

        //Vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        //LED
        builder.setLights(Color.RED, 3000, 3000);

        // Build the notification
        Notification notification = builder.build();
        // END_INCLUDE(buildNotification)
       // builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        // BEGIN_INCLUDE(customLayout)
        // Inflate the notification layout as RemoteViews
       /* RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);

        // Set text on a TextView in the RemoteViews programmatically.
        final String t_event = event;
        final String t_time = time;
        final String t_date = date;
        final String t_year = year;
        final String text = "You Have Conference Call ";
        contentView.setTextViewText(R.id.title_display1, "App Testing");
        contentView.setTextViewText(R.id.time_display1, "10:00");
        contentView.setTextViewText(R.id.date_display1, "Jul 11");
        contentView.setTextViewText(R.id.time_zone_display1, "2017");

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);

        // Set text on a TextView in the RemoteViews programmatically.
        final String time = DateFormat.getTimeInstance().format(new Date()).toString();
        final String text = "new Conference Call Sheduled Now";
        contentView.setTextViewText(R.id.textView, text);*/
        /* Workaround: Need to set the content view here directly on the notification.
         * NotificationCompatBuilder contains a bug that prevents this from working on platform
         * versions HoneyComb.
         * See https://code.google.com/p/android/issues/detail?id=30495
         */
       /* notification.contentView = contentView;*/

        // Add a big content view to the notification if supported.
        // Support for expanded notifications was added in API level 16.
        // (The normal contentView is shown when the notification is collapsed, when expanded the
        // big content view set here is displayed.)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder.setSmallIcon(R.drawable.viber);
            builder.setContentTitle("Custom notification")
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText("Big Text"))
                    .setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                    .setLights(Color.DKGRAY, 500, 500)
                    .setContentText("notification message");
        } else {
            RemoteViews customNotificationView = new RemoteViews(context.getPackageName(),
                    R.layout.notification);


            customNotificationView.setTextViewText(R.id.title_display1, event);
            customNotificationView.setTextViewText(R.id.date_display1, month+" "+date+" "+year);
            customNotificationView.setTextViewText(R.id.time_display1, time);
            customNotificationView.setTextViewText(R.id.time_zone_display1, timezone);

            builder.setContent(customNotificationView);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setAutoCancel(true);
            builder.setDefaults(Notification.DEFAULT_SOUND);
            builder.setLights(Color.DKGRAY, 500, 500);
        }
        // END_INCLUDE(customLayout)

        // START_INCLUDE(notify)
        // Use the NotificationManager to show the notification
        NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.notify(BASIC_NOTIFICATION_ID, notification);
        // END_INCLUDE(notify)
    }
    private void notificationCustom(Context context,String event,String time,String date,String year,String month){

        Intent intent = new Intent(context, JDialerMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);


// Android 2.x does not support remote view + custom notification concept using
// support library
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mBuilder.setSmallIcon(R.drawable.viber);
            mBuilder.setContentTitle("Custom notification")
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText("Big Text"))
                    .setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                    .setLights(Color.DKGRAY, 500, 500)
                    .setContentText("notification message").setVisibility(NotificationCompat.VISIBILITY_PUBLIC);;
        } else {
            RemoteViews customNotificationView = new RemoteViews(context.getPackageName(),
                    R.layout.notification);
            customNotificationView.setTextViewText(R.id.title_display1, event);
            customNotificationView.setTextViewText(R.id.date_display1, month+" "+date+" "+year);
            customNotificationView.setTextViewText(R.id.time_display1, time);
            customNotificationView.setTextViewText(R.id.time_zone_display1, timezone);

            mBuilder.setContent(customNotificationView);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setAutoCancel(true);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mBuilder.setLights(Color.DKGRAY, 500, 500);
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
// build notification
        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(1000, mBuilder.build());
    }
}
