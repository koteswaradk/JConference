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
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.service.EventsService;
import com.juniper.jconference.util.ConnectionDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ScreenOFFONReceiver extends BroadcastReceiver {
    private String TAG="ScreenOFFONReceiver";
    public static boolean wasScreenOn = true;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    String time;
    String mydate;
    Context receivercontext;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        this.receivercontext=context;
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // DO WHATEVER YOU NEED TO DO HERE
           // System.out.println("SCREEN TURNED OFF");
            wasScreenOn = false;
            PendingIntent service = null;
            /*Intent intentForService = new Intent(context.getApplicationContext(), EventsService.class);
            final AlarmManager alarmManager = (AlarmManager)
            context.getSystemService(Context.ALARM_SERVICE);
            final Calendar time = Calendar.getInstance();
            time.set(Calendar.MINUTE, 0);
            time.set(Calendar.SECOND, 0);
            time.set(Calendar.MILLISECOND, 0);
            if (service == null) {
                service = PendingIntent.getService(context, 0,
                        intentForService, PendingIntent.FLAG_CANCEL_CURRENT);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 60, service);
            receivercontext.startService(intentForService);*/

        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            // AND DO WHATEVER YOU NEED TO DO HERE
          //  System.out.println("USER PRESENT");
            wasScreenOn = true;
           // updateDBFromCalendar();
          //  Toast.makeText(receivercontext,"userpresent",Toast.LENGTH_SHORT).show();

        } if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // AND DO WHATEVER YOU NEED TO DO HERE
           // System.out.println("SCREEN TURNED ON");
            wasScreenOn = true;
           // updateDBFromCalendar();



        }
    }

    private void updateDBFromCalendar(){
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        String devicedate = formatter.format(today).replace("/", " ");

      //  devicedate="15 Aug 2017";
       // Log.d("receiver",""+devicedate);
        Cursor cursor = receivercontext.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (!devicedate.equalsIgnoreCase(date_from_evet))
                {
                     // Log.d("service ","date not equal");
                    dropTable();
                    insertDataToDb(devicedate);
                    readEventsFromDB(devicedate);

                }
                if (devicedate.equalsIgnoreCase(date_from_evet))
                {
                     // Log.d("receiver ","date equal");


                }
            }
        }

    }
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(receivercontext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) receivercontext, new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(receivercontext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        Cursor cursor = receivercontext.getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {

                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                      //  Log.i("ScreenOFFONReceiver","device_date "+device_date);
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

                            Uri selectedUri = receivercontext.getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
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

        int result1= receivercontext.getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }
    public void readEventsFromDB(String currentdate) {
        // Log.i(TAG, "1");

        //  Log.i(TAG, "2");
        String[] column=  new String[] {"Distinct "+ EventsDBHelper.KEY_CURRE_EVENT,EventsDBHelper.KEY_CURRE_DATE_TIME,EventsDBHelper.KEY_CURRE_DETAILS};
        Cursor cursor = receivercontext.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, column, null, null, null, null);
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
                      /*  Log.i(TAG, "eventtitle: " + eventtitle);
                        Log.i(TAG, "date and time: " + eventdateandtime);
                        Log.i(TAG, "eventdetails: " + eventdetails);*/
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
