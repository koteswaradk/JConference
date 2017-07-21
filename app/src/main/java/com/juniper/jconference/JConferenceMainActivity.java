package com.juniper.jconference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.adapter.InnerCallAdapter;
import com.juniper.jconference.model.CallModel;

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
import java.util.TimeZone;

public class JConferenceMainActivity extends AppCompatActivity {
    String Tag = getClass().getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 111;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    private Toolbar toolbar;
    InnerCallAdapter innercalladapter;
    Context context;
    TextView title,nomeetings;
    ArrayList<CallModel> conference_call_model=new ArrayList<>();
    ArrayList<String>phonenumberList=new ArrayList<>();
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jconference_main);
        listView = (ListView) findViewById(R.id.edisplay_list);
         title=(TextView) findViewById(R.id.title);
        nomeetings=(TextView) findViewById(R.id.t_no_data);

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = df.format(c.getTime());
        
        title.setText(currentDate);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    readCurrentEventsFromCallender();
                    //todaysEventFromCalender();
                } else {
                    //code for deny
                }
                break;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(Tag,"onstart");
       // readEventsFromCallender();
        readCurrentEventsFromCallender();

    }
    public void readEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        // 0 = January, 1 = February, ...
        Log.i(Tag,"readEventsFromCallender");
        Calendar startTime = Calendar.getInstance();
        startTime.set(2017, 05, 27);
        Log.i(Tag,"startTime");
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 07, 29);
        Log.i(Tag,"endTime");
        /*startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        // SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);*/
        // the range is all data from 2014
         // int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

        Log.i(Tag,"selection");
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

        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
        Log.i(Tag,cursor+"iiiiii");
        if(cursor!=null){
            Log.i(Tag,cursor+"");
            conference_call_model.clear();
            if (cursor.moveToFirst()) {

                Log.i(Tag,cursor.moveToFirst()+"");
                do {
                    String title = cursor.getString(1);
                    CallModel model = new CallModel();
                    model.setTitle(cursor.getString(1));

                    //print values on log
                    Log.i(Tag, "title: " + title);
                   // titleList.add(title);
                    Log.i(Tag, "date and time: " + (new Date(cursor.getLong(3))).toString());

                    model.setTime((new Date(cursor.getLong(3))).toString().substring(11,16));
                    model.setTimezone("("+(new Date(cursor.getLong(3))).toString().substring(20,29)+")");
                    model.setDate((new Date(cursor.getLong(3))).toString().substring(0,10)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));

                   // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                    model.setDateandtime((new Date(cursor.getLong(3))).toString());
                    if (getConferenceId(cursor.getString(2))!=null) {
                        Log.d(Tag, "Conference ID" + getConferenceId(cursor.getString(2)));
                        model.setConference(getConferenceId(cursor.getString(2)));
                    }
                    ArrayList<String>plist=   extractPhoneNumber(cursor.getString(2));
                    // model.setPhNumber(cursor.getString(2));
                    Set<String> hs = new HashSet<>();
                    hs.addAll(plist);
                    plist.clear();
                    plist.addAll(hs);
                    Collections.reverse(plist);
                    //  model.setNumberList(plist);

                    for (int i = 0; i <plist.size() ; i++) {

                        // Log.i(Tag+"Phone Number list",plist.get(i).replace(" ",""));
                        model.setPhNumber(plist.get(i));
                        Log.i(Tag+"Phone Number ",plist.get(i));
                    }
                    conference_call_model.add(model);

                    //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                    Log.d(Tag,"-------------------------------------");


                } while (cursor.moveToNext());
            }
            innercalladapter=new InnerCallAdapter(JConferenceMainActivity.this,conference_call_model);
            listView.setAdapter(innercalladapter);
            innercalladapter.notifyDataSetChanged();


        }


    }
    public String getConferenceId(String mystring){

        String conferenceid="";
        try {

            String[] questionMarkTokens = mystring.split("Conference ID");
            String beforeQuestionMark = questionMarkTokens[0];

            String aftertext = questionMarkTokens[1];

            conferenceid = aftertext.substring(1, 10).replace(" ", "");


        }catch (Exception e){

        }

        return conferenceid;
    }
    public ArrayList<String>  extractPhoneNumber(String input){


        Iterator<PhoneNumberMatch> existsPhone= PhoneNumberUtil.getInstance().findNumbers(input, "IN").iterator();
        //  Log.d(Tag+"Raw String",existsPhone.next().rawString());
        try {
            // callNumb=existsPhone.next().rawString();
            phonenumberList.clear();
            while (existsPhone.hasNext()){
                // System.out.println("Phone == " + existsPhone.next().rawString());

                // String formatted = PhoneNumberUtil.getInstance().format(existsPhone.next().number(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL);


                phonenumberList.add(existsPhone.next().rawString());

                // devidePhoneNumber(input);

                // Log.d(Tag+"phone number formated--",formatted);
            }


        }catch (NoSuchElementException e){

        }


        return phonenumberList;
    }
    public void readCurrentEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        // 0 = January, 1 = February, ...
        Log.i(Tag,"readEventsFromCallender");
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DATE, 1);

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

        Log.i(Tag,"selection");
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

        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
        Log.i(Tag,cursor+"iiiiii");
        if(cursor!=null){
            Log.i(Tag,cursor+"");
            conference_call_model.clear();
            if (cursor.moveToFirst()) {

                Log.i(Tag,cursor.moveToFirst()+"");
                do {
                    String title = cursor.getString(1);
                    CallModel model = new CallModel();
                    model.setTitle(cursor.getString(1));

                    //print values on log
                    Log.i(Tag, "title: " + title);
                    // titleList.add(title);
                    Log.i(Tag, "date and time: " + (new Date(cursor.getLong(3))).toString());

                    model.setTime((new Date(cursor.getLong(3))).toString().substring(11,16));
                    model.setTimezone("("+(new Date(cursor.getLong(3))).toString().substring(20,29)+")");
                    model.setDate((new Date(cursor.getLong(3))).toString().substring(0,10)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));

                    // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                    model.setDateandtime((new Date(cursor.getLong(3))).toString());
                    if (getConferenceId(cursor.getString(2))!=null) {
                        Log.d(Tag, "Conference ID" + getConferenceId(cursor.getString(2)));
                        model.setConference(getConferenceId(cursor.getString(2)));
                    }
                    ArrayList<String>plist=   extractPhoneNumber(cursor.getString(2));
                    // model.setPhNumber(cursor.getString(2));
                    Set<String> hs = new HashSet<>();
                    hs.addAll(plist);
                    plist.clear();
                    plist.addAll(hs);
                    Collections.reverse(plist);
                    model.setNumberList(plist);

                    for (int i = 0; i <plist.size() ; i++) {

                        // Log.i(Tag+"Phone Number list",plist.get(i).replace(" ",""));
                        model.setPhNumber(plist.get(i));
                        Log.i(Tag+"Phone Number ",plist.get(i));
                    }
                    conference_call_model.add(model);

                    //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                    Log.d(Tag,"-------------------------------------");


                } while (cursor.moveToNext());
            }
            innercalladapter=new InnerCallAdapter(JConferenceMainActivity.this,conference_call_model);
            listView.setAdapter(innercalladapter);
            innercalladapter.notifyDataSetChanged();


        }


    }

}
