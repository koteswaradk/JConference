package com.juniper.jconference.receiver;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.service.EventsService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RepeatAlaramBootReceiver extends BroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    Context racontext;
    String mydate;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 123;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        racontext=context;
        // an Intent broadcast.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            // Set the alarm to start at approximately 2:00 p.m.
            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intentservice = new Intent(racontext, EventsService.class);
            alarmIntent = PendingIntent.getBroadcast(racontext, 0, intentservice, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 5);
            updateDBFromCalendar();
            Log.i("Repeat","set repeat");
    // With setInexactRepeating(), you have to use one of the AlarmManager interval
    // constants--in this case, AlarmManager.INTERVAL_DAY.
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);

        }
    }
    private void updateDBFromCalendar(){
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        String devicedate = formatter.format(today).replace("/", " ");

        //  devicedate="15 Aug 2017";
        Log.d("Repeat",""+devicedate);
        Cursor cursor = racontext.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (!devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("Repeat ","date not equal");
                    dropTable();
                    insertDataToDb(devicedate);
                    readEventsFromDB(devicedate);

                }
                if (devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("Repeat ","date equal");


                }
            }
        }

    }
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(racontext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) racontext, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(racontext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        Cursor cursor = racontext.getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {

                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.i("Repeat","device_date "+device_date);
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

                            Uri selectedUri = racontext.getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
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

        int result1= racontext.getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }
    public void readEventsFromDB(String currentdate) {
        // Log.i(TAG, "1");

        //  Log.i(TAG, "2");
        String[] column=  new String[] {"Distinct "+ EventsDBHelper.KEY_CURRE_EVENT,EventsDBHelper.KEY_CURRE_DATE_TIME,EventsDBHelper.KEY_CURRE_DETAILS};
        Cursor cursor = racontext.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, column, null, null, null, null);
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
                        Log.i("Repeat", "eventtitle: " + eventtitle);
                        Log.i("Repeat", "date and time: " + eventdateandtime);
                        Log.i("Repeat", "eventdetails: " + eventdetails);
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
}
