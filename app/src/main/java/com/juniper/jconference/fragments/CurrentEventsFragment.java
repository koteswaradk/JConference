package com.juniper.jconference.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.juniper.jconference.JDialerMainActivity;
import com.juniper.jconference.R;
import com.juniper.jconference.adapter.ListviewInsideListAdapter;
import com.juniper.jconference.model.CallModel;
import com.juniper.jconference.service.EventsService;

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

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CurrentEventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CurrentEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentEventsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    String TAG = getClass().getSimpleName();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ListView listView;
    TextView nomeetings;
    String devicedate;
    ArrayList<String> phonenumberList;
    ListviewInsideListAdapter adapter;
    ArrayList<CallModel> conference_call_model = new ArrayList<>();
    TextView noevent_textView,t_date;

    String strtext;
    private OnFragmentInteractionListener mListener;
    private HorizontalCalendar horizontalCalendar;
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 121;
    public CurrentEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CurrentEventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CurrentEventsFragment newInstance(String param1, String param2) {
        CurrentEventsFragment fragment = new CurrentEventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle bundle = getArguments();
        if (bundle!=null){
            String myValue = bundle.getString("edttext");
          //  Log.d("sssssssss",myValue);
            if (myValue.equalsIgnoreCase("refresh")){
                horizontalCalendar.goToday(false);
            }
        }

        View view= inflater.inflate(R.layout.fragment_current_events, container, false);

        listView=(ListView) view.findViewById(R.id.list);
        noevent_textView=(TextView) view.findViewById(R.id.no_meeting_display);
        nomeetings = (TextView) view.findViewById(R.id.no_meeting_display);
        t_date = (TextView) view.findViewById(R.id.tool_date_display);
        //refresh=(ImageButton) view.findViewById(R.id.refresh);


        /** end after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);

        /** start before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        // startDate.add(Calendar.MONTH, -1);

        final Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.MONTH, -1);
        defaultDate.add(Calendar.DAY_OF_WEEK, +5);

        horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
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


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
       // Log.v(TAG,"onstart");

        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy");
         devicedate = formatter.format(today).replace("/"," ");
       // Log.v(TAG,"date check=="+devicedate);
        readCurrentEventsFromCallender();

    }

    @Override
    public void onResume() {
        super.onResume();
        readCurrentEventsFromCallender();
       // Log.v(TAG,"resume");
        PendingIntent service = null;
        Intent intentForService = new Intent(getActivity().getApplicationContext(), EventsService.class);
        final AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);
        final Calendar time = Calendar.getInstance();
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (service == null) {
            service = PendingIntent.getService(getActivity(), 0,
                    intentForService, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 60, service);
        getActivity().startService(intentForService);

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                Date today = Calendar.getInstance().getTime();

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                String datefrompicker = formatter.format(date);
                String datefromsystem = formatter.format(today);
               // Log.v(TAG,datefrompicker);
               // Log.v(TAG,datefromsystem);
                loadMeetingOnDateRequest(datefrompicker);
            }

        });



    }

    private void loadMeetingOnDateRequest(String date) {
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
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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

            cursor = getActivity().getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, null, null, null);
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
                      //  Log.d(TAG,"ii"+date_from_evet);
                       // Log.d(TAG," iii"+date);
                        if (date_from_evet.equalsIgnoreCase(date)){
                          //  Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                           // Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log
                           // Log.i(TAG, "title: " + title);
                            // titleList.add(title);
                           // Log.i(TAG, "date and time: " + (new Date(cursor.getLong(3))).toString());
                          //  Log.i(TAG, "date and time---: " + (new Date(cursor.getLong(3))).toString().substring(0,3)+" "+(new Date(cursor.getLong(3))).toString().substring(8,10)+" "+(new Date(cursor.getLong(3))).toString().substring(4,7)+" "+(new Date(cursor.getLong(3))).toString().substring(30,34));
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
                           // Log.d(TAG, "-------------------------------------");
                        }



                    } while (cursor.moveToNext());
                    if (!conference_call_model.isEmpty()){
                     //   Log.d(TAG, "not empty-");
                        listView.setVisibility(View.VISIBLE);
                        nomeetings.setVisibility(View.GONE);
                        adapter = new ListviewInsideListAdapter(getActivity(), conference_call_model);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                    if (conference_call_model.isEmpty()){
                      //  Log.d(TAG, " empty-");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //
                   // Toast.makeText(getActivity(),"inside permision",Toast.LENGTH_SHORT).show();

                } else {
                    //code for deny
                }
                break;
        }
    }
    public void readCurrentEventsFromCallender() {
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
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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

            cursor = getActivity().getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null);
            // Log.i(TAG, cursor + "iiiiii");
            if (cursor != null) {

                // Log.i(TAG, cursor.moveToFirst() + "kkkk");
               /* if (!cursor.moveToFirst()) {
                    listView.setVisibility(View.GONE);
                    nomeetings.setVisibility(View.VISIBLE);
                }*/
                conference_call_model.clear();
                if (cursor.moveToFirst()) {
                 /*   listView.setVisibility(View.VISIBLE);
                    nomeetings.setVisibility(View.GONE);*/
                    //  Log.i(TAG, cursor.moveToFirst() + "ssssssss");
                    do {
                        //String date_from_evet =new Date(cursor.getLong(3)).toString().substring(0,3)+" "+new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                        String date_from_evet =new Date(cursor.getLong(3)).toString().substring(8,10)+" "+new Date(cursor.getLong(3)).toString().substring(4,7)+" "+new Date(cursor.getLong(3)).toString().substring(30,34);
                      //  Log.d(TAG,"event date"+date_from_evet);
                      //  Log.d(TAG," date of current"+devicedate);
                        if (date_from_evet.equalsIgnoreCase(devicedate.replace("-"," "))){
                        //    Log.d(TAG,"inside if");
                            String title = cursor.getString(1);
                            CallModel model = new CallModel();
                            model.setTitle(cursor.getString(1));
                          //  Log.i(TAG, "detailed: " + cursor.getString(2));
                            //print values on log
                           // Log.i(TAG, "title: " + title);
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
                    if (!conference_call_model.isEmpty()){
                      //  Log.d(TAG, "not empty-");
                        listView.setVisibility(View.VISIBLE);
                        nomeetings.setVisibility(View.GONE);
                        adapter = new ListviewInsideListAdapter(getActivity(), conference_call_model);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                    if (conference_call_model.isEmpty()){
                      //  Log.d(TAG, " empty-");
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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
