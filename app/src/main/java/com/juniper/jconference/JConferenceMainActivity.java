package com.juniper.jconference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.adapter.InnerCallAdapter;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.service.EventsService;

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

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

public class JConferenceMainActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    private static int BASIC_NOTIFICATION_ID = 100;
    TextView t_year, t_hour, day, t_date, nomeetings;
    ArrayList<CallModel> conference_call_model = new ArrayList<>();
    ArrayList<CallModel> conference_call_model1 = new ArrayList<>();
    String devicedate;
    ArrayList<String> phonenumberList;
    ListView listView;
    String title;
    Cursor cursor;
    String date_check;
    private static final String DEBUG_TAG = "DynamicListAddActivity";
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    public static ArrayList<String> descriptions = new ArrayList<String>();
    ImageButton refreah_button;
    boolean firstRun;
    ListviewInsideListAdapter adapter;
    private HorizontalCalendar horizontalCalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_list_add);
        nomeetings = (TextView) findViewById(R.id.no_meeting_display);
        listView = (ListView) findViewById(R.id.list);
        t_year = (TextView) findViewById(R.id.tool_year_title);
        t_hour = (TextView) findViewById(R.id.tool_time_display);
        t_date = (TextView) findViewById(R.id.tool_date_display);
        refreah_button = (ImageButton) findViewById(R.id.refresh);
        /** end after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        /** start before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        // startDate.add(Calendar.MONTH, -1);

        final Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.MONTH, -1);
        defaultDate.add(Calendar.DAY_OF_WEEK, +5);

        horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .datesNumberOnScreen(5)
                .dayNameFormat("EEE")
                .dayNumberFormat("dd")
                .monthFormat("MMM")
                .showDayName(true)
                .showMonthName(true)
                // .defaultSelectedDate(defaultDate.getTime())
                .textColor(Color.LTGRAY, Color.WHITE)
                .selectedDateBackground(Color.TRANSPARENT)
                .build();




    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG,"onstart");
        timeDateUpadte();

        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
        Log.v(TAG,"date check=="+devicedate);


    }

    private void timeDateUpadte() {
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        Log.e(TAG, mydate);

        Log.e(TAG + "time single digits", mydate.substring(11, 11));
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.getInstance().DAY_OF_WEEK);

        t_date.setText(dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6)+" "+mydate.substring(7, 11));
        date_check=dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6)+" " + mydate.substring(7, 11);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG,"resume");
        conference_call_model.clear();

        insertDataToDb(devicedate);
        firstRun = getSharedPreferences("preferences", MODE_PRIVATE).getBoolean("firstrun", true);
        if(firstRun){

            //set the firstrun to false so the next run can see it.
            getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstrun", false).commit();
            Toast.makeText(getApplicationContext(), "First time to open the app", Toast.LENGTH_SHORT).show();


        }
        else{
            readEventsFromcursor();
            Toast.makeText(getApplicationContext(), "Not the first time to open it", Toast.LENGTH_SHORT).show();

        }


      //  readEventsFromcursor();
       // readCurrentEventsFromCallender(devicedate);
       // readEventsFromcursor();
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
                    intentForService, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 60, service);

        refreah_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalCalendar.goToday(false);

               // readCurrentEventsFromCallender(devicedate);

            }
        });
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                Date today = Calendar.getInstance().getTime();

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                String datefrompicker = formatter.format(date);
                String datefromsystem = formatter.format(today);
               /* Log.v(TAG,datefrompicker);
                Log.v(TAG,datefromsystem);*/


               // readCurrentEventsFromCallender(datefrompicker);
            }

        });


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
            Log.d(TAG,"1");
            if (cursor != null) {

                Log.d(TAG,"2");
                if (cursor.moveToFirst()) {

                    do {
                        Log.d(TAG,"3");
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.d(TAG,"date_from_evet insert titles"+date_from_evet);
                        Log.d(TAG,"device_date insert details"+device_date);
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))){

                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
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

                            //readEventsFromcursor();
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

    /*private void loadMeetingOnDateRequest(String date) {
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

                conference_call_model.clear();
                if (cursor.moveToFirst()) {

                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                       // new Date(cursor.getLong(3)).toString().substring(0,3)+" "+
                        //Log.d(TAG,"ii"+date_from_evet);
                       // Log.d(TAG," iii"+date);
                        if (date_from_evet.equalsIgnoreCase(date)){
                            Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                            Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log
                            Log.i(TAG, "title: " + title);
                            // titleList.add(title);
                            Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                            Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));
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
                        }



                    } while (cursor.moveToNext());
                   if (!conference_call_model.isEmpty()){
                       Log.d(TAG, "not empty-");
                       listView.setVisibility(View.VISIBLE);
                       nomeetings.setVisibility(View.GONE);
                       adapter = new ListviewInsideListAdapter(this, conference_call_model);
                       listView.setAdapter(adapter);
                       adapter.notifyDataSetChanged();
                   }
                    if (conference_call_model.isEmpty()){
                        Log.d(TAG, " empty-");
                        listView.setVisibility(View.GONE);
                        nomeetings.setVisibility(View.VISIBLE);
                    }

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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    // readCurrentEventsFromCallender();
                    // readEventsFromCallender();
                    // retriveReminder();
                    ;
                } else {
                    //code for deny
                }
                break;
        }
    }

    public void readCurrentEventsFromCallender(String device_date) {
       /* String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

        // 0 = January, 1 = February, ...
        // Log.i(TAG, "readEventsFromCallender");
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

       /* long now = System.currentTimeMillis();

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);
        String[] instanceprojection= new String[] {CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.EVENT_ID};

        Uri eventsUri = eventsUriBuilder.build();*/
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
        try {

           /* Cursor merCur = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
            Cursor instance_cursor = getContentResolver().query(
                    eventsUri,instanceprojection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                            + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                    null, CalendarContract.Instances.DTSTART + " ASC");*/

            // Cursor cursor = new MergeCursor(new Cursor[]{merCur,instance_cursor});

            if (cursor != null) {


                if (cursor.moveToFirst()) {

                    do {
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        //  String date_from_evet =new Date(cursor.getLong(3)).toString().substring(0,3)+" "+new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.d(TAG,"event date"+date_from_evet);
                        Log.d(TAG," date of current"+device_date);
                        // date_check="29 Jul 2017";
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))){
                            Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                            Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log
                            Log.i(TAG, "title: " + title);
                            // titleList.add(title);
                            Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                            Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));
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
                        }

                    } while (cursor.moveToNext());

                    if (!conference_call_model.isEmpty()) {
                        Log.d(TAG, "not empty-");
                        listView.setVisibility(View.VISIBLE);
                        nomeetings.setVisibility(View.GONE);
                        adapter = new ListviewInsideListAdapter(this, conference_call_model);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                    if (conference_call_model.isEmpty()) {
                        Log.d(TAG, " empty-");
                        listView.setVisibility(View.GONE);
                        nomeetings.setVisibility(View.VISIBLE);
                    }
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


    public String getConferenceId(String mystring) {

        String conferenceid = "";
        try {

            String[] questionMarkTokens = mystring.split("Conference ID");
            String beforeQuestionMark = questionMarkTokens[0];

            String aftertext = questionMarkTokens[1];
            conferenceid = aftertext.substring(1, 12).replace(" ", "");
            conferenceid = conferenceid.replaceAll("[^0-9]", "");


        } catch (Exception e) {

        }

        return conferenceid;
    }

    private String timeFormat(String hour) {
        if (Integer.parseInt(hour) <= 9) {
            hour = 0 + "" + hour;
        }
        return hour;
    }

    private String dayGenerate(int dayNum) {
        String day = null;
        switch (dayNum) {
            case Calendar.MONDAY:

                day = "Mon";
                break;
            case Calendar.TUESDAY:

                day = "Tue";
                break;
            case Calendar.WEDNESDAY:

                day = "Wed";
                break;
            case Calendar.THURSDAY:

                day = "Thu";
                break;
            case Calendar.FRIDAY:

                day = "Fri";
                break;
            case Calendar.SATURDAY:

                day = "Sat";
            case Calendar.SUNDAY:

                day = "Sun";
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
    public void readEventsFromcursor() {

        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            conference_call_model.clear();
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                do {
                    if (date_from_evet.equalsIgnoreCase(devicedate)){
                        CallModel model = new CallModel();
                        String eventtitle = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                        String eventdateandtime = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME));
                        String eventdetails = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DETAILS));

                        model.setTitle(eventtitle);

                        //print values on log

                        // titleList.add(title);
                        /*Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());*/
                       /* Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));*/
                        // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime(eventdateandtime.substring(11, 16));
                        model.setTimezone("(" +eventdateandtime.substring(20, 29) + ")");
                        model.setDate(eventdateandtime.substring(0, 10) + " " + eventdateandtime.substring(30, 34));

                        // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                        model.setDateandtime(eventdateandtime);
                        if (getConferenceId(eventdetails) != null) {
                            Log.d(TAG, "Conference ID" + getConferenceId(eventdetails));
                            model.setConference(getConferenceId(eventdetails));
                        }
                        ArrayList<String> plist = extractPhoneNumber(eventdetails);
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


                        /*HashSet<CallModel> modelHashSet=new HashSet<>();
                        modelHashSet.addAll(conference_call_model);*/

                        conference_call_model.add(model);

                        /*Log.d("Title"+"date from cursor",eventtitle);
                        Log.d("DateAndTime"+"from cursor",eventdateandtime);
                        Log.d("deatils"+"from cursor",eventdetails);*/
                    }



                } while (cursor.moveToNext());

                if (!conference_call_model.isEmpty()) {
                    Log.d(TAG, "not empty-");
                    listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);
                    adapter = new ListviewInsideListAdapter(this, conference_call_model);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                if (conference_call_model.isEmpty()) {
                    Log.d(TAG, " empty-");
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
            }
        }if (cursor==null){
            Log.d(TAG+"cursor==null","cursor==null");

        }

    }
    public void dropTable(){
        int result= this.getContentResolver().delete(Provider.CONTENT_EVENTS_URI,null,null);
        int result1= this.getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);

        if (result!=0){
            // Log.i(TAG,"rows affected"+result);
        }
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
        }

    }


}
