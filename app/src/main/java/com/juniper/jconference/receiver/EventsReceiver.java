package com.juniper.jconference.receiver;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.juniper.jconference.DynamicListAddActivity;
import com.juniper.jconference.JDialerMainActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.service.EventsService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.security.AccessController.getContext;

public class EventsReceiver extends BroadcastReceiver {
    private static final String TAG=getContext().getClass().getSimpleName();
    Context context;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 122;
    private static int BASIC_NOTIFICATION_ID=100;
    String title,time,date,year,month,timezone,_date;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        this.context=context;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            _date= extras.getString("date");
            Log.i(TAG,"Inside EventsReceiver"+_date);
           /* Date today = Calendar.getInstance().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
            String devicedate1 = formatter.format(today).replace("/", " ");*/
            updateDBFromCalendar(_date);
          //  Toast.makeText(context,_date,Toast.LENGTH_LONG).show();
           // Notification(context,intent.getStringExtra("New Conference Call is Available"));
        }

      /*  if (extras != null) {
            title= extras.getString("title");
             time= extras.getString("time");
             date= extras.getString("date");
             year= extras.getString("year");
            month=extras.getString("month");
            timezone=extras.getString("timezone");
        }*/
        //CallModel model=new CallModel();
       /* Log.i(TAG,"Inside Receiver onReceive");

        Log.i(TAG,title+" New Conference Call is Available");
        Log.i(TAG,time);
        Log.i(TAG,date);
        Log.i(TAG,year);
        Log.i(TAG,month);
        Log.i(TAG,timezone);
        //createNotification(context,title,time,date,year,month);
        notificationCustom(context,title,time,date,year,month);*/
        //Notification(context,intent.getStringExtra("New Conference Call is Available"));

    }
    private void updateDBFromCalendar(String devicedate){
       /* mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

      */

        //  devicedate="15 Aug 2017";
        Log.d("EventsReceiver",""+devicedate);
        Cursor cursor = context.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (!devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("EventsReceiver ","date not equal");
                    dropTable();
                    insertDataToDb(devicedate);
                    readEventsFromDB(devicedate);

                }
                if (devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("EventsReceiver ","date equal");
                    readEventsFromDB(devicedate);


                }
            }
        }

    }
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);
        String[] projection = new String[]{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.EVENT_ID};

        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = context.getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {

                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.i("EventsReceiver","device_date "+device_date);
                        // device_date="06 Aug 2017";
                        //  Log.i(TAG,"date_from_evet"+date_from_evet);
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))){
                            //   Log.i(TAG,"8 "+now);
                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
                            //  Log.i(TAG,"insert date and time "+date_and_time_full);
                            // Log.d(TAG,"insert titles"+titles);
                            //  Log.d(TAG,"insert details"+details);
                            // Log.d(TAG, "-------------------------------------");
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                            Uri selectedUri = context.getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
                            if (selectedUri!=null){
                                if (ContentUris.parseId(selectedUri)>0);

                            }else{

                            }

                        }

                    } while (cursor.moveToNext());
                }



            }


        } catch (Exception ex) {

        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();

            } catch (Exception ex) {
            }


        }
    }
    public void dropTable(){

        int result1= context.getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }
    public void readEventsFromDB(String currentdate) {
        // Log.i(TAG, "1");

        //  Log.i(TAG, "2");
        String[] column=  new String[] {"Distinct "+ EventsDBHelper.KEY_CURRE_EVENT,EventsDBHelper.KEY_CURRE_DATE_TIME,EventsDBHelper.KEY_CURRE_DETAILS};
        Cursor cursor = context.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, column, null, null, null, null);
        if (cursor!=null) {
            //  Log.i(TAG, "3");
            if (cursor.moveToFirst()) {
                // Log.i(TAG, "4");
                String date_from_evet =cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                do {
                  /*  Log.i(TAG, "event"+date_from_evet);
                    Log.i(TAG, "device"+devicedate);*/
                    // devicedate="06 Aug 2017";
                    if (date_from_evet.equalsIgnoreCase(currentdate.replace("-"," "))){
                        // Log.i(TAG, "6");
                        CallModel model = new CallModel();
                        String eventtitle = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                        String eventdateandtime = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME));
                        String eventdetails = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DETAILS));

                        model.setTitle(eventtitle);
                        Log.i(TAG, "eventtitle: " + eventtitle);
                        Log.i(TAG, "date and time: " + eventdateandtime);
                        Log.i(TAG, "eventdetails: " + eventdetails);
                        //print values on log

                        model.setTime(eventdateandtime.substring(11, 16));
                        model.setTimezone("(" +eventdateandtime.substring(20, 29) + ")");
                        model.setDate(eventdateandtime.substring(0, 10) + " " + eventdateandtime.substring(30, 34));

                        String[] questionMarkTokens = eventdetails.split("Join online meeting");
                        String beforeQuestionMark = questionMarkTokens[0];
                        // beforeQuestionMark.replace("."," ");
                        model.setDetails(eventdetails);

                        // Log.i(TAG, "details: " + beforeQuestionMark.replace(".",""));
                        // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                        model.setDateandtime(eventdateandtime);


                        /*Log.d("Title"+"date from cursor",eventtitle);
                        Log.d("DateAndTime"+"from cursor",eventdateandtime);
                        Log.d("deatils"+"from cursor",eventdetails);*/
                    }



                } while (cursor.moveToNext());


            }
        }if (cursor==null){
            //  Log.d(TAG+"cursor==null","cursor==null");

        }

    }
    public void Notification(Context context, String message) {
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
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder.setSmallIcon(R.drawable.viber);
            builder.setContentTitle(title)
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText(message))
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
        }
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }

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
