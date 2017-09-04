package com.juniper.jconference;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.provider.Provider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

public class JdialerStartupActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = getClass().getSimpleName();
    ArrayList<CallModel> conference_call_model = new ArrayList<>();
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    String devicedate;
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageButton refreah_button,signout;
    TextView t_date, nomeetings;
    ListviewInsideListAdapter adapter;
    ArrayList<String> phonenumberList;
    private HorizontalCalendar horizontalCalendar;
    InnerCallAdapter innsercallAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jdialer_startup);
        init();


    }

    private void init(){
        nomeetings = (TextView) findViewById(R.id.no_meeting_display);
        listView = (ListView) findViewById(R.id.list);
        refreah_button = (ImageButton) findViewById(R.id.refresh);
        t_date = (TextView) findViewById(R.id.tool_date_display);
        signout=(ImageButton)findViewById(R.id.signout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_npi_selected_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
            mSwipeRefreshLayout.setNestedScrollingEnabled(true);
        }
        signout.setOnClickListener(this);
        refreah_button.setOnClickListener(this);
        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");

        SimpleDateFormat formatterday = new SimpleDateFormat("EEE/dd/MMM/yyyy");

        t_date.setText(formatterday.format(today).replace("/", " "));

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
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {


                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

                String datefrompicker = formatter.format(date);


                Log.v(TAG,datefrompicker);

                loadMeetingFromDB(datefrompicker);
            }

        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);

            if (!cursor.moveToFirst() || cursor.getCount() == 0){
                Log.v(TAG,"cursor.getCount() == 0");
                insertDataToDb(devicedate);
            }else
            {
                Log.v(TAG,"cursor.getCount() == 0");
                updateDBFromCalender(devicedate);

            }
        }catch (SQLiteException eq){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readEventsFromDB(devicedate);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                horizontalCalendar.goToday(true);
                updateDBFromCalender(devicedate);
                loadMeetingFromDB(devicedate);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });



    }


    private void loadMeetingFromDB(String datafromcallandar) {


        //  Log.d("service",""+devicedate);
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String date_from_evet = cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8, 10) + " " +
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4, 7) + " " +
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30, 34);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (datafromcallandar.equalsIgnoreCase(date_from_evet)) {
                    readEventsFromDB(datafromcallandar);
                    // Log.d("service ","date equal");
                    mSwipeRefreshLayout.setEnabled(true);

                }
                if (!datafromcallandar.equalsIgnoreCase(date_from_evet)) {
                    // readCurrentEventsFromCallender(datafromcallandar);
                    mSwipeRefreshLayout.setEnabled(false);
                    readInstances(datafromcallandar);
                    // Log.d("service ","date not equal");

                }
            }
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

                conference_call_model.clear();
                if (cursor.moveToFirst()) {

                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {

                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);

                        if (date_from_evet.equalsIgnoreCase(date)){
                            String titles = cursor.getString(1);
                            String details =cursor.getString(2);
                            String date_and_time_full= new Date((cursor.getLong(3))).toString();
                            Log.i(TAG+"toady","insert date and time "+date_and_time_full);
                            Log.d(TAG+"today","insert titles"+titles);
                            Log.d(TAG+"today","insert details"+details);
                            Log.d(TAG, "-------------------------------------");
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
    public void readEventsFromDB(String currentdate) {
        // Log.i(TAG, "1");
        conference_call_model.clear();
        //  Log.i(TAG, "2");
        try{
        String[] column = new String[]{"Distinct "+EventsDBHelper.KEY_CURRE_EVENT, EventsDBHelper.KEY_CURRE_DATE_TIME, EventsDBHelper.KEY_CURRE_DETAILS};
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, column, null, null, null, null);
        if (cursor != null) {
            //  Log.i(TAG, "3");
            if (cursor.moveToFirst()) {
                // Log.i(TAG, "4");
                String date_from_evet = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8, 10) + " " + cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4, 7) + " " + cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30, 34);
                do {
                  /*  Log.i(TAG, "event"+date_from_evet);
                    Log.i(TAG, "device"+devicedate);*/
                    // devicedate="06 Aug 2017";
                    if (date_from_evet.equalsIgnoreCase(currentdate.replace("-", " "))) {
                        // Log.i(TAG, "6");
                        CallModel model = new CallModel();
                        String eventtitle = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                        String eventdateandtime = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME));
                        String eventdetails = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DETAILS));

                        model.setTitle(eventtitle);
                        Log.i(TAG + "fromdb", "eventtitle: " + eventtitle);
                        Log.i(TAG, "fromdb: " + eventdateandtime);
                        Log.i(TAG + "fromdb", "eventdetails: " + eventdetails);
                        //print values on log

                        // titleList.add(title);
//                        Log.i(TAG+"fromdb", "date and time: " + (new Date(cursor.getLong(3))).toString());
                       /* Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));*/
                        // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime(eventdateandtime.substring(11, 16));
                        model.setTimezone("(" + eventdateandtime.substring(20, 29) + ")");
                        model.setDate(eventdateandtime.substring(0, 10) + " " + eventdateandtime.substring(30, 34));
                       try {
                           String[] s_leadershi = eventdetails.split("Leadership");
                           String ss_leadershi=s_leadershi[1];
                           String substring=ss_leadershi.substring(1,7);
                           model.setLeadershipnumber(substring);
                       }catch (ArrayIndexOutOfBoundsException e){

                       }

                        String[] questionJoin_by_Phone = eventdetails.split("Join by Phone");
                        String beforeQuestionMark = questionJoin_by_Phone[0];
                        if(beforeQuestionMark.contains("https://meet.juniper.net")){
                            Log.d(TAG,"present");
                            model.setMeetJuniperPresent(true);
                        }else {
                            Log.d(TAG,"not present");
                            model.setMeetJuniperPresent(false);
                        }
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
                   // innsercallAdapter =new InnerCallAdapter(this,conference_call_model);
                    adapter = new ListviewInsideListAdapter(this,conference_call_model);
                    // timezoneadapter=new TimeZoneCallAdapter(this,conference_call_model);
                    listView.setAdapter(adapter);
                   //  listView.setAdapter(innsercallAdapter);
//                    innsercallAdapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                    // timezoneadapter.notifyDataSetChanged();
                }
                if (conference_call_model.isEmpty()) {
                    //  Log.d(TAG, " empty-");
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }
            }
        }
        if (cursor == null) {
            //  Log.d(TAG+"cursor==null","cursor==null");

        }
    }
     catch(SQLiteException e)   {

        }

    }
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
    public void dropTable(){

        int result1= getContentResolver().delete(Provider.CONTENT_CURRENT_EVENTS_URI,null,null);
        if (result1!=0){
            // Log.i(TAG,"rows affected"+result1);
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
                       // model.setLeadershipnumber("2345123");
                        // String beforeQuestionMark = questionMarkTokens[0];
                        // beforeQuestionMark.replace("."," ");
                        try
                        {
                            String[] s_leadershi = instance_cursor.getString(2).split("Leadership");
                            String ss_leadershi=s_leadershi[1];
                            String substring=ss_leadershi.substring(1,8);
                            ss_leadershi.substring(1,7);
                        }catch (ArrayIndexOutOfBoundsException e){

                        }


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
                   // innsercallAdapter =new InnerCallAdapter(this,conference_call_model);
                    adapter = new ListviewInsideListAdapter(this, conference_call_model);
                    listView.setAdapter(adapter);
                    //listView.setAdapter(innsercallAdapter);
                    adapter.notifyDataSetChanged();
                   // innsercallAdapter.notifyDataSetChanged();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.refresh:
                horizontalCalendar.goToday(false);

                readEventsFromDB(devicedate);
                break;
            case R.id.signout:
                this.finish();
                System.exit(0);
                break;
        }
    }
}
