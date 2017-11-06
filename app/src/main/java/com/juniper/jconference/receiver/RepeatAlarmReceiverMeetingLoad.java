package com.juniper.jconference.receiver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.juniper.jconference.JdialerStartupActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.provider.Provider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by koteswara on 10/31/17.
 */

public class RepeatAlarmReceiverMeetingLoad extends BroadcastReceiver{
    String TAG = getClass().getSimpleName();
    public static final int REQUEST_CODE = 12345;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    Context context;
    String devicedate;
    String date_from_evet="data already inserted";
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("OnReceive");

        this.context = context;

        Date today = Calendar.getInstance().getTime();
        Log.i("Activity",""+today);
       /* */

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
        try {

                updateDBFromCalender(devicedate);


        }catch (SQLiteException eq){

        }

    }
    private void normalNotification() {
        Intent intent1 = new Intent(context, JdialerStartupActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_ONE_SHOT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setContentTitle("Today Meetings...")
                .setContentText("Loading Today Meetings")
                .setAutoCancel(true)
                .setSound(alarmSound);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1000, mBuilder.build());

    }
    private void updateDBFromCalender(String s_devicedate){

        Cursor cursor = context.getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
               /* String date_from_evet =cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);*/

                System.out.println("3");
                // Log.d("service ",date_from_evet);
                Date datee= new Date(cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)));
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                String date = formatter.format(datee).toString().replace("/"," ");


                if (!s_devicedate.equalsIgnoreCase(date))
                {

                     dropTable();
                    insertDataToDb(s_devicedate);

                }

            }
        }

    }

    public void dropTable(){
        System.out.println("4");
        int result1= context.getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
        // long now = System.currentTimeMillis();
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        currentTime.set(Calendar.ZONE_OFFSET, TimeZone.getTimeZone("UTC").getRawOffset());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY));
        long now = calendar.getTimeInMillis();
        System.out.println("5");
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(((Activity)context), new String[]{Manifest.permission.READ_CALENDAR},
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
        System.out.println("6");
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = context.getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");
        try {
            Log.d(TAG,"1");
            if (cursor != null) {

                Log.d(TAG,"2");
                if (cursor.moveToFirst()) {

                    do {
                       /*  Log.d(TAG,"3");

                        Log.d(TAG+"tttt",cursor.getString(1));
                        Log.d(TAG+"dddd",cursor.getString(2));
                        Log.d(TAG,"device_date "+device_date);*/
                        Date datee= new Date(cursor.getLong(3));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                        String date = formatter.format(datee).toString().replace("/"," ");
                        date_from_evet=date;
                        // Log.d(TAG,"yyyyyyyyyyy "+date);
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        // String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        //  Log.d(TAG,"date_from_evet insert titles"+date_from_evet);
                        //   Log.d(TAG,"device_date insert details"+device_date);
                        if (date.equalsIgnoreCase(device_date.replace("-"," "))){

                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
                            date_from_evet=date_and_time_full;
                         /*   Log.i(TAG,"insert date and time "+date_and_time_full);
                            Log.d(TAG,"insert titles"+titles);
                            Log.d(TAG,"insert details"+details);
                            Log.d(TAG, "-------------------------------------");*/
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                            Uri selectedUri =context.getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
                            if (selectedUri!=null){
                                if (ContentUris.parseId(selectedUri)>0);

                            }else{

                            }


                        }

                    } while (cursor.moveToNext());
                    System.out.println("7");
                    normalNotification();
                    Intent i = new Intent();
                    i.setClassName(context, "com.juniper.jconference.JdialerStartupActivity");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
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
