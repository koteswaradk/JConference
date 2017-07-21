package com.juniper.jconference;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.adapter.InnerCallAdapter;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.service.EventsService;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;

public class DynamicListAddActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    private static int BASIC_NOTIFICATION_ID=100;
    TextView t_year,t_hour,day,t_date,nomeetings;
    ArrayList<CallModel> conference_call_model=new ArrayList<>();
    ArrayList<String>phonenumberList;
    ListView listView;
    String minute;
    ImageButton refreah_button;
    ListviewInsideListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_list_add);
        nomeetings=(TextView)findViewById(R.id.no_meeting_display);
        listView = (ListView) findViewById(R.id.list);
        t_year=(TextView) findViewById(R.id.tool_year_title);
        t_hour=(TextView) findViewById(R.id.tool_time_display);
        t_date=(TextView) findViewById(R.id.tool_date_display);
        refreah_button=(ImageButton) findViewById(R.id.refresh);
    }

    @Override
    protected void onStart() {
        super.onStart();

        timeDateUpadte();


        readCurrentEventsFromCallender();
    }

 private void timeDateUpadte(){
     String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
     Log.e(TAG,mydate);

     t_year.setText(" "+mydate.substring(7,11));

     if(Integer.parseInt(mydate.substring(11,14).replaceAll("[^0-9]", ""))<10){
         t_hour.setText("0"+mydate.substring(11,16).replace(" ",""));
     }if (Integer.parseInt(mydate.substring(11,14).replaceAll("[^0-9]", ""))>=10) {
         //timeFormat(mydate.substring(11,13).toString());
         t_hour.setText(mydate.substring(11, 17));
     }
     Log.e(TAG+"time single digits",mydate.substring(11,11));
     Calendar calendar = Calendar.getInstance();
     int dayOfWeek = calendar.get(Calendar.getInstance().DAY_OF_WEEK);

     t_date.setText(dayGenerate(dayOfWeek)+" "+mydate.substring(0,6));
 }


    @Override
    protected void onResume() {
        super.onResume();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do your work
                timeDateUpadte();

            }
        },60000);*/
        PendingIntent service = null;
        Intent intentForService = new Intent(this.getApplicationContext(), EventsService.class);
        final AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        final Calendar time = Calendar.getInstance();
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (service == null) {
            service = PendingIntent.getService(this, 0,
                    intentForService,    PendingIntent.FLAG_CANCEL_CURRENT);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 60, service);

        refreah_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCurrentEventsFromCallender();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    readCurrentEventsFromCallender();
                   // readEventsFromCallender();
                } else {
                    //code for deny
                }
                break;
        }
    }
    public void readCurrentEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};
        Cursor cursor=null;
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
            cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
           // Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {

               // Log.i(TAG, cursor.moveToFirst() + "kkkk");
                if (!cursor.moveToFirst()){
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
                conference_call_model.clear();
                if (cursor.moveToFirst()) {
                    listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);
                  //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {
                        String title = cursor.getString(1);
                        CallModel model = new CallModel();
                        model.setTitle(cursor.getString(1));
                        Log.i(TAG, "detailed: " + cursor.getString(2));
                        //print values on log
                        Log.i(TAG, "title: " + title);
                        // titleList.add(title);
                        Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());

                       // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTimezone("(" + (new Date(cursor.getLong(3))).toString().substring(20, 29) + ")");
                        model.setDate((new Date(cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34));

                        // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                        model.setDateandtime((new Date(cursor.getLong(3))).toString());
                        if (getConferenceId(cursor.getString(2)) != null) {
                            Log.d(TAG, "Conference ID" + getConferenceId(cursor.getString(2)));
                            model.setConference(getConferenceId(cursor.getString(2)));
                        }
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
                            Log.i(TAG + "Phone Number ", plist.get(i));

                        }
                        conference_call_model.add(model);

                        //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                        Log.d(TAG, "-------------------------------------");


                    } while (cursor.moveToNext());
                    adapter = new ListviewInsideListAdapter(this, conference_call_model);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }


            }
        } catch (Exception ex) {

        } finally {
            try {
                if (cursor != null && !cursor.isClosed())
                    cursor.close();
                /*if( db.isOpen() )
                    db.close();*/
            } catch (Exception ex) {
            }


        }
    }
    public String getConferenceId(String mystring){

        String conferenceid="";
        try {

            String[] questionMarkTokens = mystring.split("Conference ID");
            String beforeQuestionMark = questionMarkTokens[0];

            String aftertext = questionMarkTokens[1];
            conferenceid = aftertext.substring(1, 12).replace(" ", "");
            conferenceid=conferenceid.replaceAll("[^0-9]", "");


        }catch (Exception e){

        }

        return conferenceid;
    }
    private String timeFormat(String hour){
        if(Integer.parseInt(hour)<=9){
            hour=0+""+hour;
        }
        return hour;
    }
    private String  dayGenerate(int dayNum){
        String day=null;
        switch (dayNum) {
            case Calendar.MONDAY:

                day="Mon";
                break;
            case Calendar.TUESDAY:

                day="Tue";
                break;
            case Calendar.WEDNESDAY:

                day="Wed";
                break;
            case Calendar.THURSDAY:

                day="Thu";
                break;
            case Calendar.FRIDAY:

                day="Fri";
                break;
            case Calendar.SATURDAY:

                day="Sat";
            case Calendar.SUNDAY:

                day="Sun";
                break;

        }

        return day;
    }

    public ArrayList<String>  extractPhoneNumber(String input){

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
    public void readEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        // 0 = January, 1 = February, ...
        //Log.i(TAG,"readEventsFromCallender");
        Calendar startTime = Calendar.getInstance();
        startTime.set(2017, 05, 27);
        //Log.i(TAG,"startTime");
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 07, 29);
       // Log.i(TAG,"endTime");
        /*startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        // SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);*/
        // the range is all data from 2014
        // int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

       // Log.i(TAG,"selection");
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
        // it will also works
        // Uri.parse("content://com.android.calendar/events")
        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
       // Log.i(TAG,cursor+"iiiiii");
        if(cursor!=null){
           // Log.i(TAG,cursor+"");
            conference_call_model.clear();
            if (cursor.moveToFirst()) {

              //  Log.i(TAG,cursor.moveToFirst()+"");
                do {
                    String title = cursor.getString(1);
                    CallModel model = new CallModel();
                    model.setTitle(cursor.getString(1));

                    //print values on log
                   // Log.i(TAG, "title: " + title);

                    // titleList.add(title);
                  //  Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                   // Log.i(TAG, "detailed: " + cursor.getString(2));
                    model.setTime((new Date(cursor.getLong(3))).toString().substring(11,16));
                    model.setTimezone("("+(new Date(cursor.getLong(3))).toString().substring(20,29)+")");
                    model.setDate((new Date(cursor.getLong(3))).toString().substring(0,10)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));

                    // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                    model.setDateandtime((new Date(cursor.getLong(3))).toString());
                    if (getConferenceId(cursor.getString(2))!=null) {
                    //    Log.d(TAG, "Conference ID" + getConferenceId(cursor.getString(2)));
                        model.setConference(getConferenceId(cursor.getString(2)));
                    }
                    ArrayList<String>plist=extractPhoneNumber(cursor.getString(2));
                    // model.setPhNumber(cursor.getString(2));
                    //remove the duplicates
                    Set<String> hs = new HashSet<>();
                    hs.addAll(plist);
                    plist.clear();
                    plist.addAll(hs);
                    Collections.reverse(plist);
                    model.setNumberList(plist);

                    for (int i = 0; i <model.getNumberList().size() ; i++) {

                        // Log.i(Tag+"Phone Number list",plist.get(i).replace(" ",""));
                        model.setPhNumber(plist.get(i));
                      //  Log.i(TAG+"Phone Number --",model.getNumberList().get(i));
                    }
                    conference_call_model.add(model);

                    //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                   // Log.d(TAG,"-------------------------------------");


                } while (cursor.moveToNext());
            }
            adapter=new ListviewInsideListAdapter(this,conference_call_model);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        }



    }
}
