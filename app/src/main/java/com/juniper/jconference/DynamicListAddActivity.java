package com.juniper.jconference;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.adapter.InnerCallAdapter;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.adapter.TimeZoneCallAdapter;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.receiver.OnBootReceiver;
import com.juniper.jconference.receiver.RepeatAlaramBootReceiver;
import com.juniper.jconference.receiver.RepeatingAlarmReceiver;
import com.juniper.jconference.service.EventsService;

import java.text.DateFormat;
import java.text.ParseException;
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

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

public class DynamicListAddActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    private static int BASIC_NOTIFICATION_ID = 100;
    TextView t_year, t_hour, day, t_date, nomeetings;
    HashSet<ArrayList<CallModel>> setremovedup=new HashSet<>();
    ArrayList<CallModel> conference_call_model = new ArrayList<>();
    ArrayList<CallModel> conference_call_model1 = new ArrayList<>();
    String devicedate;
    ArrayList<String> phonenumberList;
    ListView listView;
    String mydate;
    Cursor cursor;
    String date_check;
    private static final String DEBUG_TAG = "DynamicListAddActivity";
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    public static ArrayList<String> descriptions = new ArrayList<String>();
    ImageButton refreah_button;
    ListviewInsideListAdapter adapter;
    TimeZoneCallAdapter timezoneadapter;
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
                .selectorColor(Color.WHITE)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
       // Log.v(TAG,"onstart");
        timeDateUpadte();
        TimeZone tz = TimeZone.getDefault();
       // System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());
        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
      //  Log.v(TAG,"date check=="+devicedate);
        horizontalCalendar.goToday(false);

        devicedate = formatter.format(today).replace("/"," ");

    }

    private void timeDateUpadte() {
        /*String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        Log.e(TAG, mydate);

        Log.e(TAG + "time single digits", mydate.substring(11, 11));
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.getInstance().DAY_OF_WEEK);

        t_date.setText(dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6)+" "+mydate.substring(7, 11));
        date_check=dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6)+" " + mydate.substring(7, 11);*/
        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("EEE/dd/MMM/yyyy");
       //formatter.format(today).replace("/", " ");
        t_date.setText(formatter.format(today).replace("/", " "));
    }


    @Override
    protected void onResume() {
        super.onResume();
      //  Log.v(TAG,"resume");
      //
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);

        if (!cursor.moveToFirst() || cursor.getCount() == 0){
          //  Log.v(TAG,"inside on resume of .moveToFirst");
            insertDataToDb(devicedate);
        }
       /* else{
           // updateDBFromServer();
            readEventsFromDB(devicedate);
        }*/
        readEventsFromDB(devicedate);


        /*PendingIntent service = null;
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

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 10000, service);*/

       /* ComponentName receiver = new ComponentName(this, RepeatAlaramBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
*/
       horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                // Date today = Calendar.getInstance().getTime();

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

                String datefrompicker = formatter.format(date);

                // String datefromsystem = formatter.format(today);
                Log.v(TAG,datefrompicker);
                // Log.v(TAG,datefromsystem);

                //loadMeetingOnDateRequest(datefrompicker);

                // readCurrentEventsFromCallender(datefrompicker);
                loadMeetingFromDB(datefrompicker);
            }

        });
        refreah_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalCalendar.goToday(false);

                readEventsFromDB(devicedate);

            }
        });

    }
    private void updateDBFromServer(){
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                    Log.d("raw",mydate.substring(0,26));

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        String devicedate = formatter.format(today).replace("/", " ");

        //  devicedate="15 Aug 2017";
        // Log.d("receiver",""+devicedate);
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
                  //  Log.d("receiver ","date equal");
                    readEventsFromDB(devicedate);

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
                          /*  Log.i(TAG,"insert date and time "+date_and_time_full);
                            Log.d(TAG,"insert titles"+titles);
                            Log.d(TAG,"insert details"+details);
                            Log.d(TAG, "-------------------------------------");*/
                            ContentValues selectedValues = new ContentValues();
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DATE_TIME, date_and_time_full);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_EVENT, titles);
                            selectedValues.put(EventsDBHelper.KEY_CURRE_DETAILS,details);

                            Uri selectedUri =getContentResolver().insert(Provider.CONTENT_CURRENT_EVENTS_URI, selectedValues);
                            /*if (selectedUri!=null){
                                if (ContentUris.parseId(selectedUri)>0);

                            }else{

                            }*/

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
    private void loadMeetingFromDB(String datafromcallandar){
        mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        //  Log.d("service",""+devicedate);
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor!=null) {
            if (cursor.moveToFirst()) {
                String date_from_evet =cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (datafromcallandar.equalsIgnoreCase(date_from_evet))
                {
                    readEventsFromDB(datafromcallandar);
                   // Log.d("service ","date equal");

                }
                if (!datafromcallandar.equalsIgnoreCase(date_from_evet))
                {
                   // readCurrentEventsFromCallender(datafromcallandar);
                    readInstances(datafromcallandar);
                   // Log.d("service ","date not equal");

                }
            }
        }

    }
    public void readEventsFromDB(String currentdate) {
        // Log.i(TAG, "1");
        conference_call_model.clear();
        //  Log.i(TAG, "2");
      String[] column=  new String[] {"Distinct "+ EventsDBHelper.KEY_CURRE_EVENT,EventsDBHelper.KEY_CURRE_DATE_TIME,EventsDBHelper.KEY_CURRE_DETAILS};
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, column, null, null, null, null);
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
                      //  Log.i(TAG, "eventtitle: " + eventtitle);
                       /* Log.i(TAG, "date and time: " + eventdateandtime);*/
                        Log.i(TAG, "eventdetails: " + eventdetails);
                        //print values on log

                        // titleList.add(title);
                        /*Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());*/
                       /* Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));*/
                        // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime(eventdateandtime.substring(11, 16));
                        model.setTimezone("(" +eventdateandtime.substring(20, 29) + ")");
                        model.setDate(eventdateandtime.substring(0, 10) + " " + eventdateandtime.substring(30, 34));

                        String[] questionMarkTokens = eventdetails.split("Join online meeting");
                        String beforeQuestionMark = questionMarkTokens[0];
                       // beforeQuestionMark.replace("."," ");
                        model.setDetails(eventdetails);

                      //  Log.i(TAG, "details: " + beforeQuestionMark.replace(".",""));
                        // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                        model.setDateandtime(eventdateandtime);
                        if (getConferenceId(eventdetails) != null) {
                            // Log.d(TAG, "Conference ID" + getConferenceId(eventdetails));
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
                            // Log.i(TAG + "Phone Number ", plist.get(i));

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
                    //  Log.d(TAG, "not empty-");
                    listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);
                    adapter = new ListviewInsideListAdapter(this,conference_call_model);
                   // timezoneadapter=new TimeZoneCallAdapter(this,conference_call_model);
                    listView.setAdapter(adapter);
                   // listView.setAdapter(timezoneadapter);
                    adapter.notifyDataSetChanged();
                   // timezoneadapter.notifyDataSetChanged();
                }
                if (conference_call_model.isEmpty()) {
                    //  Log.d(TAG, " empty-");
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
            }
        }if (cursor==null){
            //  Log.d(TAG+"cursor==null","cursor==null");

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


                    readEventsFromDB(devicedate);

                } else {
                    //code for deny
                }
                break;
        }
    }

    private void readInstances(String devicedate) {

        conference_call_model.clear();
        long now = System.currentTimeMillis();

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);
        String[] projection = new String[]{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.EVENT_ID};

        Uri eventsUri = eventsUriBuilder.build();
        Cursor instance_cursor = getContentResolver().query(
                eventsUri, projection, CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + (now + 2592000000L) + " and " + CalendarContract.Instances.VISIBLE + " = 1",
                null, CalendarContract.Instances.DTSTART + " ASC");

        try {

            if (instance_cursor != null) {
                while (instance_cursor.moveToNext()) {
                    // final String title = cursor.getString(0);
                    //String evetdate = new Date(instance_cursor.getLong(3)).toString().substring(0, 3) + " " + new Date(instance_cursor.getLong(3)).toString().substring(8, 10) + " " + new Date(instance_cursor.getLong(3)).toString().substring(4, 7) + " " + new Date(instance_cursor.getLong(3)).toString().substring(30, 34);
                    String date_from_evet =new Date(instance_cursor.getLong(3)).toString().substring(8,10)+" "+new Date(instance_cursor.getLong(3)).toString().substring(4,7)+" "+new Date(instance_cursor.getLong(3)).toString().substring(30,34);
                    //  Log.d("date recurence", date_from_evet);
                    //  Log.d("date2 recurence", devicedate);
                    // devicedate="02 Aug 2017";
                    if (date_from_evet.equalsIgnoreCase(devicedate.replace("-", " "))) {
              /* String title = instance_cursor.getString(1);
                String details = instance_cursor.getString(2);
                String date = new Date((instance_cursor.getLong(3))).toString();


                final Date begin = new Date(instance_cursor.getLong(1));
                final Date end = new Date(instance_cursor.getLong(2));
                final Boolean allDay = !instance_cursor.getString(3).equals("0");


                System.out.println("Title: " + title +"\n"+"details"+ details +"\n"+ " End: " + date+"\n" + " All Day: " + allDay);
               // Log.d("Main Activty","--------------------------------------------------------------------------------------------");*/

                        //  Log.d(TAG, "inside if instance");
                        String title = instance_cursor.getString(1);
                        CallModel model = new CallModel();
                        model.setTitle(instance_cursor.getString(1));
                        //  Log.i(TAG, "detailed: instance " + instance_cursor.getString(2));
                        //print values on log
                         // Log.i(TAG, "title: " + title);
                        // titleList.add(title);
                        // Log.i(TAG, " instance date and time: " + (new Date(instance_cursor.getLong(3))).toString());
                        //  Log.i(TAG, " instance date and time---: " + (new Date(instance_cursor.getLong(3))).toString().substring(0, 3) + " " + (new Date(instance_cursor.getLong(3))).toString().substring(8, 10) + " " + (new Date(instance_cursor.getLong(3))).toString().substring(4, 7) + " " + (new Date(instance_cursor.getLong(3))).toString().substring(30, 34));
                        // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime((new Date(instance_cursor.getLong(3))).toString().substring(11, 16));
                        model.setTimezone("(" + (new Date(instance_cursor.getLong(3))).toString().substring(20, 29) + ")");
                        model.setDate((new Date(instance_cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(instance_cursor.getLong(3))).toString().substring(30, 34));
                       // String[] questionMarkTokens= instance_cursor.getString(2).split(".........................................................................................................................................");

                       // String beforeQuestionMark = questionMarkTokens[0];
                        // beforeQuestionMark.replace("."," ");
                        model.setDetails(instance_cursor.getString(2));
                        // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                        model.setDateandtime((new Date(instance_cursor.getLong(3))).toString());
                        if (getConferenceId(instance_cursor.getString(2)) != null) {
                            //  Log.d(TAG, "Conference ID" + getConferenceId(instance_cursor.getString(2)));
                            model.setConference(getConferenceId(instance_cursor.getString(2)));
                        }
                        ArrayList<String> plist = extractPhoneNumber(instance_cursor.getString(2));
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
                            // Log.i(TAG + "Phone Number ", plist.get(i));

                        }
                        conference_call_model.add(model);

                        //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                        //  Log.d(TAG, "-------------------------------------");
                    }

           /* adapter = new ListviewInsideListAdapter(getActivity(), conference_call_model);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();*/
                }

                if (!conference_call_model.isEmpty()) {
                    //  Log.d(TAG, "not empty-");
                    listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);
                    adapter = new ListviewInsideListAdapter(this, conference_call_model);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                if (conference_call_model.isEmpty()) {
                    //  Log.d(TAG, " empty-");
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
            }else {

            }

        }
        catch (Exception e) {

        } finally {
            try {
                if (instance_cursor != null && !instance_cursor.isClosed())
                    instance_cursor.close();

            } catch (Exception ex) {
            }
        }
    }
    public void readCurrentEventsFromCallender(String device_date) {
       // String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};

       /* // 0 = January, 1 = February, ...
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

               // conference_call_model.clear();
                if (cursor.moveToFirst()) {

                    do {
                       // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                      //  String date_from_evet =new Date(cursor.getLong(3)).toString().substring(0,3)+" "+new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                      //  Log.d(TAG,"event date"+date_from_evet);
                      //  Log.d(TAG," date of current"+device_date);
                       // date_check="29 Jul 2017";
                        if (date_from_evet.equalsIgnoreCase(device_date.replace("-"," "))){
                          //  Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                          //  Log.i(TAG, "title: " + title);
                           // Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log

                            // titleList.add(title);
                           // Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                           // Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));
                            // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                            model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                            model.setTimezone("(" + (new Date(cursor.getLong(3))).toString().substring(20, 29) + ")");
                            model.setDate((new Date(cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34));

                            // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                            model.setDateandtime((new Date(cursor.getLong(3))).toString());
                            if (getConferenceId(cursor.getString(2)) != null) {
                              //  Log.d(TAG, "Conference ID" + getConferenceId(cursor.getString(2)));
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
                               // Log.i(TAG + "Phone Number ", plist.get(i));

                            }

                           // conference_call_model.add(model);

                            //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                           // Log.d(TAG, "-------------------------------------");
                        }

                    } while (cursor.moveToNext());

                  /*  if (!conference_call_model.isEmpty()) {
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
                    }*/
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
        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, null, null, null);
        // Log.i(TAG,cursor+"iiiiii");
        try {

            cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
            // Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {

                // Log.i(TAG, cursor.moveToFirst() + "kkkk");
                if (!cursor.moveToFirst()) {
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
                conference_call_model.clear();
                if (cursor.moveToFirst()) {
                    listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);
                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                        String date_from_evet = new Date(cursor.getLong(3)).toString().substring(0, 3) + " " + new Date(cursor.getLong(3)).toString().substring(8, 10) + " " + new Date(cursor.getLong(3)).toString().substring(4, 7) + " " + new Date(cursor.getLong(3)).toString().substring(30, 34);
                      //  Log.d(TAG, "event date" + date_from_evet);
                      //  Log.d(TAG, " date of current" + date_check);
                        if (date_from_evet.equalsIgnoreCase(date_check)) {
                          //  Log.d(TAG, "inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                           // Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log
                           // Log.i(TAG, "title: " + title);
                            // titleList.add(title);
                           // Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                           // Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0, 3) + " " + (new Date(cursor.getLong(3))).toString().substring(8, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(4, 7) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34));
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

            } catch (Exception ex) {
            }


        }
    }
    public void readCurrentEventsFromCallender() {
        String[] projection = new String[]{CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION};
        Cursor cursor = null;
        // 0 = January, 1 = February, ...
        Log.i(TAG, "readEventsFromCallender");
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
            Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {

                Log.i(TAG, cursor.moveToFirst() + "kkkk");
               /* if (!cursor.moveToFirst()) {
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }*/

                if (cursor.moveToFirst()) {
                 /*   listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);*/
                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {
                        //String date_from_evet =new Date(cursor.getLong(3)).toString().substring(0,3)+" "+new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        Log.d(TAG,"event date"+date_from_evet);
                        Log.d(TAG," date of current"+devicedate);
                        if (date_from_evet.equalsIgnoreCase(devicedate.replace("-"," "))){
                            //    Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                             Log.i(TAG, "title: " + title);
                              Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log

                            // titleList.add(title);
                             Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                            // Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));
                            // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                            model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                            model.setTimezone("(" + (new Date(cursor.getLong(3))).toString().substring(20, 29) + ")");
                            model.setDate((new Date(cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34));

                            // dataAndTimeList.add((new Date(cursor.getLong(3))).toString());
                            model.setDateandtime((new Date(cursor.getLong(3))).toString());
                            if (getConferenceId(cursor.getString(2)) != null) {
                                //   Log.d(TAG, "Conference ID" + getConferenceId(cursor.getString(2)));
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
                                // Log.i(TAG + "Phone Number ", plist.get(i));

                            }
                            conference_call_model.add(model);

                            //  Log.i(Tag,"second parameter: "+ cursor.getString(2));
                            //  Log.d(TAG, "-------------------------------------");
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
