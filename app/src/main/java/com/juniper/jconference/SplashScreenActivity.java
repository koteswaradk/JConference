package com.juniper.jconference;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.provider.Provider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SplashScreenActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    String devicedate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 5 seconds
                    sleep(3*1000);

                    // After 5 seconds redirect to another intent
                    Intent i=new Intent(getBaseContext(),JdialerStartupActivity.class);
                    startActivity(i);

                    //Remove activity
                    finish();

                } catch (Exception e) {

                }
            }
        };

        // start thread
        background.start();
    }



    @Override
    protected void onResume() {
        super.onResume();
        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
        try {
            Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);

            if (!cursor.moveToFirst() || cursor.getCount() == 0){
                Log.v(TAG,"cursor.getCount() == 0");
                insertDataToDb(devicedate);
            }

        }catch (SQLiteException eq){

        }
        readData();
    }

    private void readData(){
        try {
            Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);

            if (cursor.moveToFirst() || cursor.getCount() != 0){
                SplashScreenActivity.this.finish();
            }

        }catch (SQLiteException eq){

        }
    }
    private void updateDBFromCalender(String s_devicedate){

        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (!s_devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("service ","date not equal");
                    dropTable();
                    insertDataToDb(s_devicedate);

                }
                if (s_devicedate.equalsIgnoreCase(date_from_evet))
                {
                    Log.d("service ","date equal");
                    updateTodayMeeting(s_devicedate);


                }
            }
        }

    }
    public void dropTable(){

        int result1= getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }
    private void updateTodayMeeting(String date) {
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

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR},
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
        try {

            cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, null, null, null);
            // Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {

                // Log.i(TAG, cursor.moveToFirst() + "kkkk");


                if (cursor.moveToFirst()) {

                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {

                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);

                        if (date_from_evet.equalsIgnoreCase(date)){
                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
                           /* Log.i(TAG+"toady","insert date and time "+date_and_time_full);
                            Log.d(TAG+"today","insert titles"+titles);
                            Log.d(TAG+"today","insert details"+details);
                            Log.d(TAG, "-------------------------------------");*/
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                            Uri selectedUri =getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
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
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        long now = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR},
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
            // Log.d(TAG,"1");
            if (cursor != null) {

                //  Log.d(TAG,"2");
                if (cursor.moveToFirst()) {

                    do {
                        // Log.d(TAG,"3");
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        // Log.d(TAG,"date_from_evet insert titles"+date_from_evet);
                        // Log.d(TAG,"device_date insert details"+device_date);
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))){

                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
                         /*   Log.i(TAG,"insert date and time "+date_and_time_full);
                            Log.d(TAG,"insert titles"+titles);
                            Log.d(TAG,"insert details"+details);
                            Log.d(TAG, "-------------------------------------");*/
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                            Uri selectedUri =getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
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
}
