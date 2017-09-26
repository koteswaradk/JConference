package com.juniper.jconference;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.adapter.InnerCallAdapter;
import com.juniper.jconference.adapter.JDialerBaseAdapter;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.adapter.SpinnerAdapter;
import com.juniper.jconference.db.EventsDBHelper;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.model.ItemData;
import com.juniper.jconference.provider.Provider;
import com.juniper.jconference.util.NoDefaultSpinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;

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
    InnerCallAdapter innsercallAdapter;
    ArrayList<String> phonenumberList;
    JDialerBaseAdapter jdialerdapter;
    private HorizontalCalendar horizontalCalendar;
    NoDefaultSpinner spinner;  boolean isSpinnerInitial = false;
    Toolbar toolbar;
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
       // signout=(ImageButton)findViewById(R.id.signout);
        ArrayList<ItemData> list=new ArrayList<>();

        list.add(new ItemData("Reset",R.drawable.settings));
        list.add(new ItemData("Exit",R.drawable.signoutc));
        list.add(new ItemData("About App",R.drawable.infosign));

        spinner=(NoDefaultSpinner)findViewById(R.id.spinner);


       /* SpinnerAdapter adapter=new SpinnerAdapter(this, R.layout.spinner_row,R.id.txt,list);
        spinner.setAdapter(adapter);*/

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_npi_selected_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
            mSwipeRefreshLayout.setNestedScrollingEnabled(true);
        }
       // signout.setOnClickListener(this);
        refreah_button.setOnClickListener(this);
        Date today = Calendar.getInstance().getTime();
            Log.i("Activity",""+today);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/"," ");
        Log.i("devicedate",""+devicedate);
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
               // Log.i("onDateSelected",""+date);

               // Log.v(TAG,datefrompicker);

                loadMeetingFromDB(datefrompicker);
            }

        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                horizontalCalendar.goToday(true);
                updateDBFromCalender(devicedate);
                loadMeetingFromDB(devicedate);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                spinner.setBackgroundResource(R.drawable.hamburgerwhite);

                switch (position){
                    case 1:
                        //Toast.makeText(JdialerStartupActivity.this,"Reset",Toast.LENGTH_SHORT).show();
                        dropTable();
                        insertDataToDb(devicedate);
                        readEventsFromDB(devicedate);
                        break;
                    case 0:
                       // Toast.makeText(JdialerStartupActivity.this,"reset",Toast.LENGTH_SHORT).show();

                        break;

                    case 2:
                       // Toast.makeText(JdialerStartupActivity.this,"Exit",Toast.LENGTH_SHORT).show();
                        showDialog("Are You Sure Want To Exit JDialer...?");
                        break;
                    case 3:
                       // Toast.makeText(JdialerStartupActivity.this,"About App",Toast.LENGTH_SHORT).show();


                        aboutAppDialog(getResources().getString(R.string.aboutapp_basic));
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                parentView.setFocusable(false);
                spinner.setBackgroundResource(R.drawable.hamburgerwhite);

            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

   private void aboutAppDialog(String text){

       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
               JdialerStartupActivity.this);

       LayoutInflater inflater= LayoutInflater.from(this);
       View view=inflater.inflate(R.layout.dialog_alert_scrollable, null);

        TextView textview=(TextView)view.findViewById(R.id.textmsg);
                 textview.setText(text);
         TextView textmore=(TextView)view.findViewById(R.id.textmore);
         TextView textfeature=(TextView)view.findViewById(R.id.textfeature);
                 textfeature.setText(getResources().getString(R.string.app_feature));

       // set dialog message
       alertDialogBuilder
               .setTitle("About JDialer")
               .setView(view)
               .setCancelable(false)
               .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog,int id) {
                       // if this button is clicked, close
                       // current activity
                       dialog.cancel();
                   }
               });

       // create alert dialog
       final AlertDialog alertDialog = alertDialogBuilder.create();
       textmore.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               alertDialog.dismiss();
               appMoreDetails(getResources().getString(R.string.aboutapp));



           }
       });
       // show it
       alertDialog.show();

    }

    private void appMoreDetails(String details_text){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                JdialerStartupActivity.this);

        LayoutInflater inflater= LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.app_more_details_dialog, null);
        TextView textmore=(TextView)view.findViewById(R.id.more_details);
        textmore.setText(details_text);
        // set dialog message
        alertDialogBuilder
                .setTitle("About JDialer")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        dialog.cancel();
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
       // horizontalCalendar.goToday(true);
        try {
            Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);

            if (!cursor.moveToFirst() || cursor.getCount() == 0){
                // Log.v(TAG,"cursor.getCount() == 0");
                insertDataToDb(devicedate);
            }else
            {
                //  Log.v(TAG,"cursor.getCount() == 0");
                updateDBFromCalender(devicedate);

            }
        }catch (SQLiteException eq){

        }
        readEventsFromDB(devicedate);
    }


    private void loadMeetingFromDB(String datafromcallandar) {


        //  Log.d("service",""+devicedate);
        Cursor cursor = getContentResolver().query(Provider.CONTENT_CURRENT_EVENTS_URI, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
              /*  String date_from_evet = cursor.getString(
                        cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8, 10) + " " +
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4, 7) + " " +
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30, 34);*/
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                Date datee= new Date(cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)));
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                String date = formatter.format(datee).toString().replace("/"," ");
                if (datafromcallandar.equalsIgnoreCase(date)) {
                    readEventsFromDB(datafromcallandar);
                    // Log.d("service ","date equal");
                    mSwipeRefreshLayout.setEnabled(true);

                }
                if (!datafromcallandar.equalsIgnoreCase(date)) {
                    // readCurrentEventsFromCallender(datafromcallandar);
                    mSwipeRefreshLayout.setEnabled(false);
                   // Log.d("calendar date ",datafromcallandar);
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
               /* String date_from_evet =cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8,10)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4,7)+" "+
                        cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30,34);*/


               // Log.d("service ",date_from_evet);
                Date datee= new Date(cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)));
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                String date = formatter.format(datee).toString().replace("/"," ");
               // Log.d("service ",date);
                //   Log.d("service ","date_from_evet: "+date_from_evet);
                if (!s_devicedate.equalsIgnoreCase(date))
                {
                     // Log.d("service ","date not equal");
                    dropTable();
                    insertDataToDb(s_devicedate);

                }
                if (s_devicedate.equalsIgnoreCase(date))
                {
                   // Log.d("service ","date equal");
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
    //
    private void insertDataToDb(String device_date) {
        // Uri  uri=null;
       // long now = System.currentTimeMillis();
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        currentTime.set(Calendar.ZONE_OFFSET, TimeZone.getTimeZone("UTC").getRawOffset());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY));
        long now = calendar.getTimeInMillis();

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
                       /*  Log.d(TAG,"3");

                        Log.d(TAG+"tttt",cursor.getString(1));
                        Log.d(TAG+"dddd",cursor.getString(2));
                        Log.d(TAG,"device_date "+device_date);*/
                        Date datee= new Date(cursor.getLong(3));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                       String date = formatter.format(datee).toString().replace("/"," ");
                       // Log.d(TAG,"yyyyyyyyyyy "+date);
                        // String date_from_evet=new Date(cursor.getLong(3)).toString().substring(0, 10) + " " + (new Date(cursor.getLong(3))).toString().substring(30, 34);
                       // String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                       //  Log.d(TAG,"date_from_evet insert titles"+date_from_evet);
                      //   Log.d(TAG,"device_date insert details"+device_date);
                        if (date.equalsIgnoreCase(device_date.replace("-"," "))){

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
               // String date_from_evet = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(8, 10) + " " + cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(4, 7) + " " + cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)).substring(30, 34);
                Date datee= new Date(cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)));
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                String date = formatter.format(datee).toString().replace("/"," ");
               // Log.d("service ",date);
                do {
                  /*  Log.i(TAG, "event"+date_from_evet);
                    Log.i(TAG, "device"+devicedate);*/
                    // devicedate="06 Aug 2017";
                    if (date.equalsIgnoreCase(currentdate.replace("-", " "))) {
                        // Log.i(TAG, "6");
                        CallModel model = new CallModel();
                        String eventtitle = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_EVENT));
                        String eventdateandtime = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME));
                        String eventdetails = cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DETAILS));

                        model.setTitle(eventtitle);
                       // Log.i(TAG + "fromdb", "eventdetails: " + eventdetails);
                     /*   Log.i(TAG + "fromdb", "eventtitle: " + eventtitle);
                        Log.i(TAG, "fromdb: " + eventdateandtime);
                        Log.i(TAG + "fromdb", "eventdetails: " + eventdetails);*/
                        //print values on log

                        // titleList.add(title);
//                        Log.i(TAG+"fromdb", "date and time: " + (new Date(cursor.getLong(3))).toString());
                       /* Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));*/
                        // model.setTime((new Date(cursor.getLong(3))).toString().substring(11, 16));
                        model.setTime(eventdateandtime.substring(11, 16));
                       // model.setTimezone("(" + eventdateandtime.substring(20, 29) + ")");
                        Calendar cal = Calendar.getInstance();
                        TimeZone tz = cal.getTimeZone();
                        model.setTimezone("("+tz.getDisplayName(false, TimeZone.SHORT)+")");

                        Date datee1= new Date(cursor.getString(cursor.getColumnIndex(EventsDBHelper.KEY_CURRE_DATE_TIME)));
                        SimpleDateFormat formatter1 = new SimpleDateFormat("dd/MMM/yyyy");
                        String date2 = formatter1.format(datee1).toString().replace("/"," ");
                        model.setDate(date2);
                       try {
                           String[] s_leadershi = eventdetails.split("Leadership");
                           String ss_leadershi=s_leadershi[1];

                           String substring=ss_leadershi.substring(1,15);

                           String leadershipnumber=substring.replaceAll("[^0-9]", "");
                           if (leadershipnumber.matches("[0-9]+") && leadershipnumber.length() > 2) {
                               model.setLeadershipnumber(leadershipnumber);
                           }if (leadershipnumber.isEmpty()) {
                              // model.setLeadershipnumber("Not Found");
                           }

                       }catch (ArrayIndexOutOfBoundsException e){

                       }

                        String[] questionJoin_by_Phone = eventdetails.split("Join by Phone");
                        String beforeQuestionMark = questionJoin_by_Phone[0];
                        if(beforeQuestionMark.contains("https://meet.juniper.net")){
                          //  Log.d(TAG,"present");
                            model.setMeetJuniperPresent(true);
                        }else {
                           // Log.d(TAG,"not present");
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
                    //jdialerdapter=new JDialerBaseAdapter(this,conference_call_model);
                    innsercallAdapter =new InnerCallAdapter(this,conference_call_model);
                   // adapter = new ListviewInsideListAdapter(this,conference_call_model);
                    // timezoneadapter=new TimeZoneCallAdapter(this,conference_call_model);
                   // listView.setAdapter(adapter);
                   // listView.setAdapter(jdialerdapter);
                    listView.setAdapter(innsercallAdapter);
                   // jdialerdapter.notifyDataSetChanged();
                   // adapter.notifyDataSetChanged();
                    innsercallAdapter.notifyDataSetChanged();

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
       /* Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        currentTime.set(Calendar.ZONE_OFFSET, TimeZone.getTimeZone("UTC").getRawOffset());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY));
        long now=calendar.getTimeInMillis();*/
       // Log.d("date2 recurence", ""+now);

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


                    //String evetdate = new Date(instance_cursor.getLong(3)).toString().substring(0, 3) + " " + new Date(instance_cursor.getLong(3)).toString().substring(8, 10) + " " + new Date(instance_cursor.getLong(3)).toString().substring(4, 7) + " " + new Date(instance_cursor.getLong(3)).toString().substring(30, 34);
                   // String date_from_evet =new Date(instance_cursor.getLong(3)).toString().substring(8,10)+" "+new Date(instance_cursor.getLong(3)).toString().substring(4,7)+" "+new Date(instance_cursor.getLong(3)).toString().substring(30,34);
                    Date datee=  new Date(instance_cursor.getLong(3));
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
                    String date = formatter.format(datee).toString().replace("/"," ");
                   // Log.d(TAG, "instance date"+date);
                   // Log.d(TAG, "device date"+devicedate);
                    if (date.equalsIgnoreCase(devicedate.replace("-", " "))) {
                      //  Log.d(TAG, "device date"+devicedate);
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
                       // Log.d(TAG, "title"+title);
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
                       // model.setTimezone("(" + (new Date(instance_cursor.getLong(3))).toString().substring(20, 29) + ")");
                        Calendar cal = Calendar.getInstance();
                        TimeZone tz = cal.getTimeZone();
                        model.setTimezone("("+tz.getDisplayName(false, TimeZone.SHORT)+")");
                        Date datee2=  new Date(instance_cursor.getLong(3));
                        SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MMM/yyyy");
                        String date2 = formatter2.format(datee2).toString().replace("/"," ");
                        /*model.setDate((new Date(instance_cursor.getLong(3))).toString().substring(0, 10) + " " + (new Date(instance_cursor.getLong(3))).toString().substring(30, 34));*/
                        model.setDate(date2);
                        // String[] questionMarkTokens= instance_cursor.getString(2).split(".........................................................................................................................................");
                       // model.setLeadershipnumber("2345123");
                        // String beforeQuestionMark = questionMarkTokens[0];
                        // beforeQuestionMark.replace("."," ");
                        try {
                            String[] s_leadershi = instance_cursor.getString(2).split("Leadership");
                            String ss_leadershi=s_leadershi[1];

                            String substring=ss_leadershi.substring(1,15);

                             String leadershipnumber=substring.replaceAll("[^0-9]", "");
                            if (leadershipnumber.matches("[0-9]+") && leadershipnumber.length() > 2) {
                                model.setLeadershipnumber(leadershipnumber);
                            }else{
                               // model.setLeadershipnumber("Not Set");
                            }

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
                   // jdialerdapter=new JDialerBaseAdapter(this,conference_call_model);
                    innsercallAdapter =new InnerCallAdapter(this,conference_call_model);
                   // adapter = new ListviewInsideListAdapter(this, conference_call_model);
                   // listView.setAdapter(adapter);
                    //listView.setAdapter(jdialerdapter);
                    listView.setAdapter(innsercallAdapter);
                   // adapter.notifyDataSetChanged();
                    innsercallAdapter.notifyDataSetChanged();
                   //jdialerdapter.notifyDataSetChanged();
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

   /* @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onpause");
        this.finish();
        System.exit(0);
    }*/


    private String convertDateTimeZone(long originalDate) {
        String newDate = "";
        Date date = new Date(originalDate);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Date parsed = null;
        try {
            parsed = formatter.parse(formatter.format(date).toString());
            TimeZone tz = TimeZone.getTimeZone("GMT");
            SimpleDateFormat destFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            destFormat.setTimeZone(tz);

            newDate = destFormat.format(parsed);
        } catch (Exception e) {
        }
        return newDate;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.refresh:
                horizontalCalendar.goToday(false);

                readEventsFromDB(devicedate);
                break;
         /* *//*  case R.id.signout:
                showDialog("Are You Sure Want To Exit JDialer...?");*//*

                break;*/
        }
    }
    public void showDialog(String msg){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.customdialog);
      /*  ImageView imageViewyes=(ImageView) dialog.findViewById(R.id.a);
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);*/

        Button button_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button button_yes = (Button) dialog.findViewById(R.id.btn_yes);
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                System.exit(0);

            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}
