package com.juniper.jconference.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.DynamicListAddActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.receiver.EventsReceiver;


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
    String event_title="No_Event_Available",date="",time="",month,date_month_year_time,year,timezone="", s_hour,s_minuite;;
    String device_date_month_year_time;
    ArrayList<String>phonenumberList;
    String mydate;
    String deviceHour;

    public EventsService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        super.onStartCommand(intent, flags, startId);


        readCurrentEventsFromCallender();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // Log.d(TAG,"service on create");

    }


    public void readCurrentEventsFromCallender() {
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
                    Log.e("formated", date_month_year_time);
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
                        Log.i("inside is 24",device_date_month_year_time);
                        /* if(Integer.parseInt(mydate.substring(11,14).replaceAll("[^0-9]", ""))<10){
                        deviceHour="0"+mydate.substring(11,16).replace(" ","");
                        // Log.i("<10",""+deviceHour);
                        // Log.i("0 set",""+ mydate.substring(0, 12)+deviceHour);

                        device_date_month_year_time=mydate.substring(0, 12)+deviceHour;
                        Log.i("complete",""+ device_date_month_year_time);
                    }if (Integer.parseInt(mydate.substring(11,14).replaceAll("[^0-9]", ""))>=10) {
                        //timeFormat(mydate.substring(11,13).toString());
                        // deviceHour=mydate.substring(11, 17).replace(" ","");
                        //  Log.i(">10",""+deviceHour);
                        device_date_month_year_time=mydate.substring(0, 17);
                    }*/
                    }
                    if (!DateFormat.is24HourFormat(this)){
                        Log.i(TAG,"not 24 format");
                        Calendar cal = Calendar.getInstance();

                        int minute = cal.get(Calendar.MINUTE);
                        //24 hour format
                        int hour = cal.get(Calendar.HOUR);
                        Log.e("hour of day 24 format",""+hour+":"+minute);

                        //12 hour format
                        int hour24 = cal.get(Calendar.HOUR_OF_DAY);
                        Log.e("hour of day 12 format",""+hour24+":"+minute);
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
                    }

                    Log.i("checking",""+device_date_month_year_time+" == ");
                    Log.i("checking",""+date_month_year_time);

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
                        Log.i(TAG,"After Broad cast");
                    }
                    //  conference_call_model.add(model);

                    //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                    Log.d(TAG, "-------------------------------------");


                } while (cursor.moveToNext());



              /*  Log.e(TAG+"date", mydate);
                String t_year = mydate.substring(7, 11);
                // t_hour.setText(today.getHours()+":"+minute);

                String t_hour = mydate.substring(11, 17);
                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.getInstance().DAY_OF_WEEK);

                String t_date = dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6);
                Log.e("from calendar", mydate.substring(0, 17));
                Log.e("time from device",t_hour);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");

                Calendar cal = Calendar.getInstance();

                int millisecond = cal.get(Calendar.MILLISECOND);
                int second = cal.get(Calendar.SECOND);
                int minute = cal.get(Calendar.MINUTE);
                //24 hour format
                int hour = cal.get(Calendar.HOUR);
                Log.e("hour of day 12 format",""+hour+":"+minute);
                //12 hour format
                int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                Log.e("hour of day 24 format",""+hourofday+":"+minute);
               // device_date_month_year_time=mydate.substring(0, 11)+" "+hourofday+":"+minute;
              *//*  Datetime = sdf.format(c.getTime());
                System.out.println("============="+Datetime);*//*
                Log.d("date_month_year_time",date_month_year_time);*/

              /*  if (DateFormat.is24HourFormat(this)){
                    Log.i(TAG,"inside is 24");
                    device_date_month_year_time=mydate.substring(0, 11)+" "+hourofday+":"+minute;
                }
                if ( !DateFormat.is24HourFormat(this)){
                    Log.i(TAG,"not 24 format");

                    device_date_month_year_time=mydate.substring(0, 11)+" "+hourofday+":"+minute;
                    Log.i(TAG,"device: "+device_date_month_year_time);
                }*/
               // mydate.substring(0, 17)
            }

        }
    }


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


}
