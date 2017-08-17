package com.juniper.jconference;

import android.net.Uri;
import android.nfc.Tag;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.juniper.jconference.fragments.RecurrenceEventsFragment;
import com.juniper.jconference.fragments.CurrentEventsFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JDialerMainActivity extends AppCompatActivity implements RecurrenceEventsFragment.OnFragmentInteractionListener,CurrentEventsFragment.OnFragmentInteractionListener{

    SwitchCompat myswitch;
    ImageButton refersh;
    TextView datesettext;
    String devicedate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jdialer_main);
        myswitch= (SwitchCompat)findViewById(R.id.myswitch);
        refersh=(ImageButton) findViewById(R.id.b_synch);
        datesettext=(TextView) findViewById(R.id.tool_date_display);

        //refreah_button = (ImageButton) findViewById(R.id.refresh);
        if (savedInstanceState == null) {
            // on first time display view for first nav item

            android.support.v4.app.Fragment home = new CurrentEventsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, home);
            ft.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        timeUpadte();



    }

    private void timeUpadte() {
        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
           // Log.d("mydate",mydate);
        Date today = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("EEE/dd/MMM/yyyy");
        devicedate = formatter.format(today).replace("/", " ");


        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.getInstance().DAY_OF_WEEK);

       // datesettext.setText(dayGenerate(dayOfWeek) + " "+devicedate);
        datesettext.setText(devicedate);
       // date_check=dayGenerate(dayOfWeek) + " " + mydate.substring(0, 6)+" " + mydate.substring(7, 11);
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
    @Override
    protected void onResume() {
        super.onResume();

        myswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    refersh.setVisibility(View.GONE);
                    android.support.v4.app.Fragment home = new RecurrenceEventsFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, home);

                    ft.commit();
                      //Toast.makeText(JDialerMainActivity.this,"1",Toast.LENGTH_SHORT).show();

                }else {
                    refersh.setVisibility(View.VISIBLE);
                    android.support.v4.app.Fragment home = new CurrentEventsFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, home);
                    ft.commit();
                    // Toast.makeText(JDialerMainActivity.this,"2",Toast.LENGTH_SHORT).show();

                }
            }
        });
        refersh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.support.v4.app.Fragment home = new CurrentEventsFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, home);
                ft.commit();
               // Toast.makeText(JDialerMainActivity.this,"2",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
