package com.juniper.jconference.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.DynamicListAddActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.receiver.EventsReceiver;
import com.juniper.jconference.receiver.ScreenOFFONReceiver;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

public class EventsService extends Service {
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    private static final String TAG = "EventsService";
    Context context;

    String event_title="No_Event_Available",date_month_year_time,timezone="", s_hour,s_minuite;;
    String device_date_month_year_time;
    ArrayList<String>phonenumberList;
    String mydate,devicedate;

    public EventsService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        super.onStartCommand(intent, flags, startId);
        Log.i(TAG,"onStartCommand");
       // readCurrentEventsFromCallender();
        updateDBFromServer();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenOFFONReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void updateDBFromServer(){
      //  mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        String devicedate = formatter.format(today).replace("/", " ");
        //  Log.d("service",""+devicedate);
        // Log.i(TAG, "selection");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (devicedate.equalsIgnoreCase(date_from_evet))
                {
                   // insertDataToDb(devicedate);
                    //updateCurrentEventsFromCalendar(devicedate);

                   if (!geTitleFromDB(devicedate).equalsIgnoreCase(geTitleFromCallender(devicedate))){
                     /*  Log.d("service ","not equal---");
                       Log.i("from db",geTitleFromDB(devicedate));
                       Log.i("from callander",geTitleFromCallender(devicedate));*/
                      // insertTitleToDB(geTitleFromCallender(devicedate,));
                       insertDataToDb(devicedate,geTitleFromCallender(devicedate));
                   }else{
                      // Log.i("out side",geTitleFromCallender(devicedate));
                   }



                }
            }
        }

    }


    public void updateCurrentEventsFromCalendar(String _devicedate) {

       /* Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};
        Cursor cursor = null;
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";
*/
        // Log.i(TAG, "selection");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        long now = System.currentTimeMillis();
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);
        String[] projection = new String[]{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.EVENT_ID};

        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        Cursor dbcursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        try {
            Log.i(TAG, cursor + "iiiiii");
            if ((cursor != null)) {

                Log.i(TAG, cursor.moveToFirst() + "kkkk");


                if (cursor.moveToFirst()&&(dbcursor.moveToFirst())) {


                    do {

                        String date_from_evet = new Date(cursor.getLong(3)).toString().substring(8, 10) + " " + new Date(cursor.getLong(3)).toString().substring(4, 7) + " " + new Date(cursor.getLong(3)).toString().substring(30, 34);
                       /* Log.d(TAG, "event date" + date_from_evet);
                        Log.d(TAG, " date of current" + _devicedate);*/
                        if (date_from_evet.equalsIgnoreCase(_devicedate.replace("-", " "))) {

                            Log.d(TAG, "1");

                            Log.d(TAG, "2");
                            if (dbcursor != null) {
                                Log.d(TAG, "3");
                                if (dbcursor.moveToFirst()) {
                                    Log.d(TAG, "4");
                                    String title = dbcursor.getString(dbcursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                                    Log.d("service ", "title: " + title);

                                    String titlecaleneder = cursor.getString(1);
                                    Log.d("service ", "calendar title: " + titlecaleneder);
                                    if (title.equalsIgnoreCase(titlecaleneder)) {
                                        Log.d("service ", " equal");
                                        String titles = cursor.getString(1);
                                        String details = cursor.getString(2);
                                        String date_and_time_full = new Date((cursor.getLong(3))).toString();
                                          Log.i(TAG,"insert date and time "+date_and_time_full);
                                          Log.d(TAG,"insert titles"+titles);
                                          Log.d(TAG,"insert details"+details);
                                          Log.d(TAG, "-------------------------------------");
                                        ContentValues selectedValues = new ContentValues();
                                        selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                                        selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                                        selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                                        Uri selectedUri = getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
                                        if (selectedUri!=null){
                                            if (ContentUris.parseId(selectedUri)>0);

                                        }else{

                                        }


                                    }
                                    if (!title.equalsIgnoreCase(titlecaleneder)) {
                                        Log.d("service", "not equal");
                                        String titles = cursor.getString(1);
                                        String details =cursor.getString(2);
                                        String date_and_time_full= new Date((cursor.getLong(3))).toString();
                                        Log.i(TAG,"insert date and time "+date_and_time_full);
                                        Log.d(TAG,"insert titles"+titles);
                                        Log.d(TAG,"insert details"+details);

                                    }
                                }
                            }


                        }


                    } while (cursor.moveToNext()&&(dbcursor.moveToNext()));


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

    public String geTitleFromDB(String devicedate){
        String dbTitle=null;

        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            //  Log.i(TAG, "3");
            if (cursor.moveToFirst()) {
                // Log.i(TAG, "4");
                String date_from_evet =cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                do {
                   /* Log.i(TAG, "event"+date_from_evet);
                    Log.i(TAG, "device"+devicedate);*/
                    // devicedate="06 Aug 2017";
                   // if (date_from_evet.equalsIgnoreCase(devicedate.replace("-"," "))){
                        // Log.i(TAG, "6");
                        CallModel model = new CallModel();
                        dbTitle = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                   // }

                } while (cursor.moveToNext());

            }
        }if (cursor==null){
            //  Log.d(TAG+"cursor==null","cursor==null");

        }
        return dbTitle;
    }
    public String geTitleFromCallender(String devicedate){
        String calTitle=null;
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};
        Cursor cursor = null;
        // 0 = January, 1 = February, ...
        // Log.i(TAG, "readEventsFromCallender");
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

        // Log.i(TAG, "selection");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return ;
        }
        try {

            cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
            // Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    do {

                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);

                        if (date_from_evet.equalsIgnoreCase(devicedate.replace("-"," "))){
                            //    Log.d(TAG,"inside if");
                            calTitle = cursor.getString(1);

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
        return calTitle;

    }
    private void insertDataToDb(String device_date,String _title) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        Cursor cursor = getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {

                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                       // Log.i("service",""+device_date);
                        // device_date="06 Aug 2017";
                        //  Log.i(TAG,"date_from_evet"+date_from_evet);
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))) {
                            if (cursor.getString(1).equalsIgnoreCase(_title)){
                            //   Log.i(TAG,"8 "+now);
                            String titles = cursor.getString(1);
                            String details = cursor.getString(2);
                            String date_and_time_full = new Date((cursor.getLong(3))).toString();
                            /*  Log.i(TAG,"insert date and time "+date_and_time_full);
                              Log.d(TAG,"insert titles"+titles);
                              Log.d(TAG,"insert details"+details);
                             Log.d(TAG, "-------------------------------------");*/
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS, details);

                            Uri selectedUri = getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
                            if (selectedUri != null) {
                                if (ContentUris.parseId(selectedUri) > 0) ;

                            } else {

                            }
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
  /* private void updateDBFromServer(){
       mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

       Date today = Calendar.getInstance().getTime();
       SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
      String devicedate = formatter.format(today).replace("/", " ");
     //  Log.d("service",""+devicedate);
       Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
       if (cursor!=null) {
           if (cursor.moveToFirst()) {
               String date_from_evet =cursor.getString(
                       cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                       cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                       cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
            //   Log.d("service ","date_from_evet: "+date_from_evet);
           if (!devicedate.equalsIgnoreCase(date_from_evet))
           {
             //  Log.d("service ","date not equal");
               dropTable();
               insertDataToDb(devicedate);

           }
           if (devicedate.equalsIgnoreCase(date_from_evet))
           {
             //  Log.d("service ","date equal");

           }
           }
       }

       *//*  if (DateFormat.is24HourFormat(this)){

           device_date_month_year_time=mydate.substring(0, 17);
           Log.i("inside is 24",device_date_month_year_time);
           Log.i("------------",mydate.substring(11,16));
          // if (mydate.substring(11,16).toString().equalsIgnoreCase("24:00")||mydate.substring(11,16).toString().equalsIgnoreCase("24:01")||mydate.substring(11,16).toString().equalsIgnoreCase("24:02")||mydate.substring(11,16).toString().equalsIgnoreCase("24:03")||mydate.substring(11,16).toString().equalsIgnoreCase("24:04")||mydate.substring(11,16).toString().equalsIgnoreCase("24:05"))
           if (mydate.substring(11,16).toString().replace("_"," ").equalsIgnoreCase("24:00"))
           {
               Date today = Calendar.getInstance().getTime();
               SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
               String devicedate = formatter.format(today).replace("/"," ");
               Log.v(TAG,"date check=="+devicedate);
               dropTable();
               insertDataToDb(devicedate);
           }
       }*//*
     *//*  if (!DateFormat.is24HourFormat(this)){
           Log.i(TAG,"not 24 format");
           Calendar cal = Calendar.getInstance();

           int minute = cal.get(Calendar.MINUTE);
           //24 hour format
           int hour = cal.get(Calendar.HOUR);
           Log.e(" 24 format",""+hour+":"+minute);

           //12 hour format
           int hour24 = cal.get(Calendar.HOUR_OF_DAY);
           Log.e(" 12 format",""+hour24+":"+minute);
           // changing the hor to 24 hour format for notifiction


           if(hour24<10){
               s_hour="0"+Integer.toString(hour24);
           }if (hour24>10){
               s_hour=Integer.toString(hour24);
           }
           if (minute<10) {
               s_minuite = "0" + Integer.toString(minute);
           }if (minute>10){
               s_minuite=Integer.toString(minute);
           }
           device_date_month_year_time=mydate.substring(0, 11)+" "+s_hour+":"+s_minuite;
           Log.i(TAG,"device: "+device_date_month_year_time);
           String hour_minite=s_hour+":"+s_minuite;
           Log.i(TAG,"device: ---"+hour_minite);
           if (hour_minite.equalsIgnoreCase("24:00"))
           {
               Date today = Calendar.getInstance().getTime();
               SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
               String devicedate = formatter.format(today).replace("/"," ");
               Log.v(TAG,"date check=="+devicedate);
               dropTable();
               insertDataToDb(devicedate);
           }
       }*//*

    }*/
    /*public void readCurrentEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        // 0 = January, 1 = February, ...
       // Log.i(TAG,"readEventsFromCallender");
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

       // Log.i(TAG,"selection");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);

        if(cursor!=null) {
           // Log.i(TAG, cursor + "");
            // conference_call_model.clear();
            if (cursor.moveToFirst()) {

               // Log.i(TAG, cursor.moveToFirst() + "");
                do {
                    String title = cursor.getString(1);
                    CallModel model = new CallModel();
                    model.setTitle(cursor.getString(1));
                    event_title = cursor.getString(1);
                    //print values on log
                   // Log.i(TAG, "title:from cursor " + event_title);
                    // titleList.add(title);
                   // Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                    //cursor_date_month_year_time = (new Date(cursor.getLong(3))).toString().substring()
                   // Log.i(TAG, "date and time: from cursor" + (new Date(cursor.getLong(3))).toString().substring(0, 16).toString());
                    model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                   String  time=null;
                           time = (new Date(cursor.getLong(3))).toString().substring(11, 16);

                    timezone="(" + (new Date(cursor.getLong(3))).toString().substring(20, 29) + ")";
                    model.setTimezone("(" + (new Date(cursor.getLong(3))).toString().substring(20, 29) + ")");
                  //  Log.i("time from cursor", time);
                    model.setDate((new Date(cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34));
                   String date=null;
                           date = (new Date(cursor.getLong(3))).toString().substring(8, 10);
                   // Log.i("date from cursor", date);

                   String month=null;
                           month = (new Date(cursor.getLong(3))).toString().substring(4, 7);
                  //  Log.i("month from cursor", month);
                   String year=null;
                           year = (new Date(cursor.getLong(3))).toString().substring(30, 34);
                    date_month_year_time = date + " " + month + " " + year + " " + time;
                  //  Log.e("formated", date_month_year_time);
                    // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                    model.setDateandtime((new Date(cursor.getLong(3))).toString());
                    if (getConferenceId(cursor.getString(2)) != null) {
                     //   Log.d(TAG, "Conference ID" + getConferenceId(cursor.getString(2)));
                        model.setConference(getConferenceId(cursor.getString(2)));
                    }
                   // cursor_date_month_year_time=time+" "+month+" "+date;
                    ArrayList<String> plist = extractPhoneNumber(cursor.getString(2));
                    // model.setPhNumber(cursor.getString(2));
                    Set<String> hs = new HashSet<>();
                    hs.addAll(plist);
                    plist.clear();
                    plist.addAll(hs);
                    Collections.reverse(plist);
                    model.setNumberList(plist);

                    for (int i = 0; i < plist.size(); i++) {

                        // Log.i(Tag+"Phone Number list",plist.get(i).replace(" ",""));
                        model.setPhNumber(plist.get(i));
                       // Log.i(TAG + "Numbers ", plist.get(i));

                    }
                    mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    //                    Log.d("raw",mydate.substring(0,26));

                    if (DateFormat.is24HourFormat(this)){

                        device_date_month_year_time=mydate.substring(0, 17);
                       *//* Log.i("inside is 24",device_date_month_year_time);
                        Log.i("------------",mydate.substring(11,16));*//*
                        if (mydate.substring(11,16).toString().equalsIgnoreCase("24:00")||mydate.substring(11,16).toString().equalsIgnoreCase("24:01")||mydate.substring(11,16).toString().equalsIgnoreCase("24:02")||mydate.substring(11,16).toString().equalsIgnoreCase("24:03")||mydate.substring(11,16).toString().equalsIgnoreCase("24:04")||mydate.substring(11,16).toString().equalsIgnoreCase("24:05"))
                        {
                            Date today = Calendar.getInstance().getTime();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                            String devicedate = formatter.format(today).replace("/"," ");
                          //  Log.v(TAG,"date check=="+devicedate);
                            dropTable();
                            insertDataToDb(devicedate);
                        }
                    }
                    if (!DateFormat.is24HourFormat(this)){
                       // Log.i(TAG,"not 24 format");
                        Calendar cal = Calendar.getInstance();

                        int minute = cal.get(Calendar.MINUTE);
                        //24 hour format
                        int hour = cal.get(Calendar.HOUR);
                       // Log.e("hour of day 24 format",""+hour+":"+minute);

                        //12 hour format
                        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
                       // Log.e("hour of day 12 format",""+hour24+":"+minute);
                        // changing the hor to 24 hour format for notifiction


                        if(hour24<10){
                            s_hour="0"+Integer.toString(hour24);
                        }if (hour24>10){
                            s_hour=Integer.toString(hour24);
                        }
                        if (minute<10) {
                            s_minuite = "0" + Integer.toString(minute);
                        }if (minute>10){
                            s_minuite=Integer.toString(minute);
                        }

                        device_date_month_year_time=mydate.substring(0, 11)+" "+s_hour+":"+s_minuite;
                      //  Log.i(TAG,"device: "+device_date_month_year_time);
                        String hour_minite=s_hour+":"+s_minuite;
                        if (hour_minite.equalsIgnoreCase("24:00")||hour_minite.equalsIgnoreCase("24:01")||hour_minite.equalsIgnoreCase("24:02")||hour_minite.equalsIgnoreCase("24:03")||hour_minite.equalsIgnoreCase("24:04")||hour_minite.equalsIgnoreCase("24:05"))
                        {
                            Date today = Calendar.getInstance().getTime();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                            String devicedate = formatter.format(today).replace("/"," ");
                         //   Log.v(TAG,"date check=="+devicedate);
                            dropTable();
                            insertDataToDb(devicedate);
                        }
                    }

                   // Log.i("checking",""+device_date_month_year_time.replace("-"," ")+" == ");
                  //  Log.i("checking",""+date_month_year_time);

                    if (date_month_year_time.equalsIgnoreCase(device_date_month_year_time)){
                        Intent broadcastIntent = new Intent("com.juniper.jconference.receiver.EventsReceiver");
                        broadcastIntent.setClass(this, EventsReceiver.class);
                        broadcastIntent.putExtra("title",event_title);
                        broadcastIntent.putExtra("time",time);
                        broadcastIntent.putExtra("date",date);
                        broadcastIntent.putExtra("month",month);
                        broadcastIntent.putExtra("year",year);
                        broadcastIntent.putExtra("timezone",timezone);
                        this.sendBroadcast(broadcastIntent);
                     //   Log.i(TAG,"After Broad cast");
                    }
                    //  conference_call_model.add(model);

                    //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                  //  Log.d(TAG, "-------------------------------------");


                } while (cursor.moveToNext());
            }

        }
    }*/


    public String getConferenceId(String mystring){

        String conferenceid="";
        try {

            String[] questionMarkTokens = mystring.split("Conference ID");
            String beforeQuestionMark = questionMarkTokens[0];

            String aftertext = questionMarkTokens[1];

            conferenceid = aftertext.substring(1, 11).replace(" ", "");


        }catch (Exception e){

        }

        return conferenceid;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public ArrayList<String> extractPhoneNumber(String input){

        phonenumberList=new ArrayList<>();
        Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(input, "IN").iterator();
        //  Log.d(Tag+"Raw String",existsPhone.next().rawString());
        try {
            // callNumb=existsPhone.next().rawString();
            phonenumberList.clear();
            do {
                // System.out.println("Phone == " + existsPhone.next().rawString());

                // String formatted = PhoneNumberUtil.getInstance().format(existsPhone.next().number(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL);


                phonenumberList.add(existsPhone.next().rawString());

                // devidePhoneNumber(input);

                // Log.d(Tag+"phone number formated--",formatted);
            }while (existsPhone.hasNext());


        }catch (NoSuchElementException e){

        }


        return phonenumberList;
    }
   /* private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        Cursor cursor = getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {

                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.i(TAG,"device_date "+device_date);
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

                            Uri selectedUri = getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
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
    }*/


}
