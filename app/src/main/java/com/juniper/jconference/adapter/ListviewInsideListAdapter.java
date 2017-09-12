package com.juniper.jconference.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by koteswara on 05/07/17.
 */

public class ListviewInsideListAdapter extends BaseAdapter{
    private static final String TAG= "JcallBaseAdapter";
    private LayoutInflater layoutInflater;
    public ArrayList<CallModel> eventlist;
    HashMap<String,String> number_place_map=new HashMap<>();
    Context context; String callPhoneNumber,callConferenceId,leadership;

    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE=143;
    public ListviewInsideListAdapter(Context context, ArrayList<CallModel> eventlist){
        this.context=context;
        this.eventlist=eventlist;
        number_place_map.put("+61289139885","Australia Sydney");
        number_place_map.put("+3224033300","Belgium Brussels");
        number_place_map.put("+16135912790","Canada Kanata");
        number_place_map.put("+861058126200","China Beijing");
        number_place_map.put("+862085115999","China Guangzhou");
        number_place_map.put("+85231022166","China Hong_Kong");
        number_place_map.put("+862161415199","China Shanghai");
        number_place_map.put("+33147756336","France Paris");
        number_place_map.put("+4989203012111","Germany Munich");
        number_place_map.put("18002660610","India Bangalore");
        number_place_map.put("+35318903656","Ireland Dublin");
        number_place_map.put("+390236014377","Italy Milan");
        number_place_map.put("+81353330594","Japan Tokyo");
        number_place_map.put("+82234833444","Korea Seoul");
        number_place_map.put("+60320790002","Malaysia Kuala Lumpur");
        number_place_map.put("+31207125790","Netherlands Amsterdam");
        number_place_map.put("+966112804200","Saudi Arabia Riyadh");
        number_place_map.put("+6565113580","Singapore Singapore");
        number_place_map.put("+34914143451","Spain Madrid");
        number_place_map.put("+46850644666","Sweeden Stockhol");
        number_place_map.put("+886221756353","Taiwan Taipei");
        number_place_map.put("+97143611505","UAE Dubai");
        number_place_map.put("+441372389100","UK Addlestone");
        number_place_map.put("+19785898300","USA East Coast");
        number_place_map.put("+18446454399","USA East Coast");
        number_place_map.put("+14089369000","USA West Coast");
        number_place_map.put("+18446454398","USA West Coast");
        number_place_map.put("+18002660323","USA West Coast");
        number_place_map.put("+1 844 645 4399","USA West Coast");
        number_place_map.put("+1 408 936 9000","USA West Coast");
        number_place_map.put("1800 266 0610","India Bangalore");
        number_place_map.put("+1 978 589 8300","USA East Coast");


        Collections.sort(this.eventlist, new ListviewInsideListAdapter.TimeComparator());
    }
    @Override
    public int getCount() {
        return eventlist.size();
    }

    @Override
    public Object getItem(int position) {
        return eventlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.dynamic_add_list, null);
            viewHolder = new ViewHolder();
            viewHolder.time_display=(TextView)convertView.findViewById(R.id.time_display);
            viewHolder.date_display=(TextView)convertView.findViewById(R.id.date_display);
            viewHolder.time_zone_display=(TextView)convertView.findViewById(R.id.time_zone_display);
            viewHolder.title_display=(TextView)convertView.findViewById(R.id.title_display);
            viewHolder.mainlayout=(LinearLayout)convertView.findViewById(R.id.main_layout);
            viewHolder.leadership_pin_display=(TextView)convertView.findViewById(R.id.leadership_pin_display);
            viewHolder.leadership_pin=(TextView)convertView.findViewById(R.id.leadership_pin);
            viewHolder.liner_layout=(LinearLayout) convertView.findViewById(R.id.add_list_layout);


            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CallModel conferencecallmodel = (CallModel) eventlist.get(position);

        viewHolder.time_display.setText(conferencecallmodel.getTime());
        viewHolder.time_zone_display.setText(conferencecallmodel.getTimezone());
        viewHolder.date_display.setText(conferencecallmodel.getDate());
        viewHolder.title_display.setText(conferencecallmodel.getTitle());
        try{

            {
                if (conferencecallmodel.getLeadershipnumber()==null){
                   // viewHolder.leadership_pin_display.setText("Not Found");
                    viewHolder.leadership_pin.setVisibility(View.GONE);
                    viewHolder.leadership_pin_display.setVisibility(View.GONE);

                }
                if (conferencecallmodel.getLeadershipnumber()!=null) {
                    viewHolder.leadership_pin.setVisibility(View.VISIBLE);
                    viewHolder.leadership_pin_display.setVisibility(View.VISIBLE);
                    viewHolder.leadership_pin_display.setText(conferencecallmodel.getLeadershipnumber());
                }

            }

        }catch (NullPointerException e){

        }



        setItems(conferencecallmodel,position,viewHolder);

        viewHolder.title_display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              // Log.d(TAG+"uuuuuuu",conferencecallmodel.getDetails());
                showInfoDialog(conferencecallmodel.getDetails());
            }
        });


       // notifyDataSetChanged();

        return convertView;
    }
    private void showInfoDialog(String reason) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater= LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.dialog_alert_scrollable, null);

        TextView textview=(TextView)view.findViewById(R.id.textmsg);
        textview.setText(reason);


        // set dialog message
        alertDialogBuilder
                .setTitle("Meeting Details:")
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

        AlertDialog alertDialog = alertDialogBuilder.create();


       // positive.setBackgroundColor());
        // show it
        alertDialog.show();
    }
    private void setItems(CallModel eventlist, int position, ViewHolder holder) {
        holder.liner_layout.removeAllViews();
      //  System.out.println("===============================");

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < eventlist.getNumberList().size(); i++) {
          int size=  eventlist.getNumberList().size();
            final View view = inflater.inflate(R.layout.dynamicadd_row, null);
            final View view_us_east = inflater.inflate(R.layout.dynamicadd_us_east, null);
            final View view_us_west = inflater.inflate(R.layout.dynamicadd_us_west, null);
            final View view_india = inflater.inflate(R.layout.dynamicadd_india, null);
            holder.display_place=(TextView)view.findViewById(R.id.display_place);
            holder.dialnumber_display = (TextView) view.findViewById(R.id.dialnumber_display);
            holder.conferenceid=(TextView)view.findViewById(R.id.conferenceid);
            holder.conferenceid_display = (TextView) view.findViewById(R.id.conferenceid_display);
            holder.leadership_display=(TextView)view.findViewById(R.id.leadership_display);
            holder.leadership=(TextView)view.findViewById(R.id.leadership);
            holder.ib_call = (ImageButton) view.findViewById(R.id.ib_call);
            holder.us_west_ib_call=(ImageButton)view_us_west.findViewById(R.id.west_ib_call);
            holder.us_east_ib_call=(ImageButton)view_us_east.findViewById(R.id.east_ib_call);

            String number=eventlist.getNumberList().get(i);

            holder.display_place.setText(number_place_map.get(eventlist.getNumberList().get(i)));
            try{
                if( number_place_map.get(eventlist.getNumberList().get(i)).toString().equalsIgnoreCase("India Bangalor")){
                    holder.display_place.setText(number_place_map.get(eventlist.getNumberList().get(i)));
                    String s1=holder.display_place.getText().toString();
                    String s2=s1.concat(" (TollFree)");
                    holder.display_place.setText(s2);
                }else{
                    holder.display_place.setText(number_place_map.get(eventlist.getNumberList().get(i)));
                }
            }catch (NullPointerException e){

            }

            holder.dialnumber_display.setText(eventlist.getNumberList().get(i));
//            holder.leadership_display.setText(eventlist.getLeadershipnumber().toString());
            try {

                if (number_place_map.get(eventlist.getNumberList().get(i)).equalsIgnoreCase("USA East Coast")) {
                    holder.dialnumber_display.setText("+18446454399");
                    String s1=holder.display_place.getText().toString();
                    String s2=s1.concat(" (TollFree)");
                    holder.display_place.setText(s2);
                }
                if (number_place_map.get(eventlist.getNumberList().get(i)).equalsIgnoreCase("USA West Coast")) {
                    holder.dialnumber_display.setText("+18446454398");
                    String s3=holder.display_place.getText().toString();
                    String s4=s3.concat(" (TollFree)");
                    holder.display_place.setText(s4);
                }
                if (number_place_map.get(eventlist.getNumberList().get(i)).equalsIgnoreCase("India Bangalore")) {
                    holder.dialnumber_display.setText("18002660610");
                }

                if (number_place_map.get(eventlist.getNumberList().get(i)).equalsIgnoreCase("USA West Coast")&&(eventlist.getNumberList().get(i).equalsIgnoreCase("+18446454398"))) {
                    holder.display_place.setText("India Bangalore");
                    holder.dialnumber_display.setText("18002660610");
                }

                if (number_place_map.get(eventlist.getNumberList().get(i)).equalsIgnoreCase("USA East Coast")&&(eventlist.getNumberList().get(i).equalsIgnoreCase("+18446454399"))) {
                    holder.display_place.setText("India Bangalore");

                    holder.dialnumber_display.setText("18002660610");

                }

            }catch (NullPointerException e){

            }


            if ((holder.dialnumber_display.getText().toString().equalsIgnoreCase("18002660323"))){
                holder.dialnumber_display.setText("+18446454398");
                holder.display_place.setText(number_place_map.get(holder.dialnumber_display.getText().toString()));
                String s3=holder.display_place.getText().toString();
                String s4=s3.concat(" (TollFree)");
                holder.display_place.setText(s4);
                holder.us_east_displayplace=(TextView)view_us_east.findViewById(R.id.us_east_display_place);
                holder.us_east_dialnumber_display=(TextView)view_us_east.findViewById(R.id.us_east_dialnumber_display);
                holder.us_east_conferenceid=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid);
                holder.us_east_conferenceid_display=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid_display);
                holder.us_east_leadership=(TextView)view_us_east.findViewById(R.id.us_east_leadership);
                holder.us_east_leadership_display=(TextView)view_us_east.findViewById(R.id.us_east_leadership_display);

                holder.us_east_displayplace.setText("USA East Coast"+" (TollFree)");
                holder.us_east_dialnumber_display.setText("+18446454399");
                holder.us_east_conferenceid_display.setText(eventlist.getConference());

                try {
                    if (!eventlist.getLeadershipnumber().isEmpty()){
                        holder.us_east_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_east_leadership.setText("#;");

                    }if (eventlist.getLeadershipnumber().isEmpty()) {
                        holder.us_east_leadership_display.setVisibility(View.GONE);
                        holder.us_east_leadership.setVisibility(View.GONE);

                    }
                }catch (NullPointerException e){

                }



               // holder.liner_layout.addView(view_us_east);

            }
            if(holder.dialnumber_display.getText().toString().equalsIgnoreCase("18002660610")){
               String indtolle= holder.display_place.getText().toString();
                String ss=indtolle.concat("( TollFree)");
                holder.display_place.setText(ss);
            }
            holder.conferenceid_display.setText(eventlist.getConference());
                try{

                    if (!eventlist.getLeadershipnumber().toString().isEmpty()){
                        holder.leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.leadership.setText("#;");
                    }if (eventlist.getLeadershipnumber().toString().isEmpty()){
                        holder.leadership_display.setVisibility(View.GONE);
                        holder.leadership.setVisibility(View.GONE);
                    }

                }catch (NullPointerException e){

                }

            if ((size==1)&&(!eventlist.getConference().isEmpty())&&(number.equalsIgnoreCase("18002660323"))){

                holder.us_east_displayplace=(TextView)view_us_east.findViewById(R.id.us_east_display_place);
                holder.us_east_dialnumber_display=(TextView)view_us_east.findViewById(R.id.us_east_dialnumber_display);
                holder.us_east_conferenceid=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid);

                holder.us_east_conferenceid_display=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid_display);
                holder.us_east_leadership=(TextView)view_us_east.findViewById(R.id.us_east_leadership);
                holder.us_east_leadership_display=(TextView)view_us_east.findViewById(R.id.us_east_leadership_display);

                holder.us_west_displayplace=(TextView)view_us_west.findViewById(R.id.us_west_display_place);
                holder.us_west_dialnumber_display=(TextView)view_us_west.findViewById(R.id.us_west_dialnumber_display);
                holder.us_west_conferenceid=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid);
                holder.us_west_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.us_west_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.us_west_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);

             /*   holder.india_displayplace=(TextView)view_india.findViewById(R.id.us_west_display_place);
                holder.india_dialnumber_display=(TextView)view_india.findViewById(R.id.us_west_dialnumber_display);
                holder.india_conferenceid=(TextView)view_india.findViewById(R.id.us_west_conferenceid);
                holder.india_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.india_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.india_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);*/

                holder.display_place.setText("India Bangalore");
                holder.dialnumber_display.setText("18002660610");
                holder.dialnumber_display.setText("+18446454399");
                String s1=holder.display_place.getText().toString();
                String s2=s1.concat(" (TollFree)");
                holder.display_place.setText(s2);

                holder.us_east_displayplace.setText("USA East Coast"+" (TollFree)");
                holder.us_east_dialnumber_display.setText("+18446454399");
                holder.us_east_conferenceid_display.setText(eventlist.getConference());

                holder.us_west_displayplace.setText("USA West Coast"+" (TollFree)");
                holder.us_west_dialnumber_display.setText("+18446454398");
                holder.us_west_conferenceid_display.setText(eventlist.getConference());
                try {
                    if (!eventlist.getLeadershipnumber().isEmpty()){
                        holder.us_east_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_east_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_west_leadership.setText("#;");
                        holder.us_east_leadership.setText("#;");
                    }if (eventlist.getLeadershipnumber().isEmpty()) {
                        holder.us_east_leadership_display.setVisibility(View.GONE);
                        holder.us_east_leadership.setVisibility(View.GONE);
                        holder.us_west_leadership_display.setVisibility(View.GONE);
                        holder.us_west_leadership.setVisibility(View.GONE);
                    }
                }catch (NullPointerException e){

                }

                holder.liner_layout.addView(view_us_west);
                holder.liner_layout.addView(view_us_east);

            }
            if ((size==1)&&(!eventlist.getConference().isEmpty())&&(number.equalsIgnoreCase("18002660610"))){

                holder.us_east_displayplace=(TextView)view_us_east.findViewById(R.id.us_east_display_place);
                holder.us_east_dialnumber_display=(TextView)view_us_east.findViewById(R.id.us_east_dialnumber_display);
                holder.us_east_conferenceid=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid);

                holder.us_east_conferenceid_display=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid_display);
                holder.us_east_leadership=(TextView)view_us_east.findViewById(R.id.us_east_leadership);
                holder.us_east_leadership_display=(TextView)view_us_east.findViewById(R.id.us_east_leadership_display);

                holder.us_west_displayplace=(TextView)view_us_west.findViewById(R.id.us_west_display_place);
                holder.us_west_dialnumber_display=(TextView)view_us_west.findViewById(R.id.us_west_dialnumber_display);
                holder.us_west_conferenceid=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid);
                holder.us_west_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.us_west_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.us_west_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);

             /*   holder.india_displayplace=(TextView)view_india.findViewById(R.id.us_west_display_place);
                holder.india_dialnumber_display=(TextView)view_india.findViewById(R.id.us_west_dialnumber_display);
                holder.india_conferenceid=(TextView)view_india.findViewById(R.id.us_west_conferenceid);
                holder.india_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.india_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.india_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);*/



                holder.us_east_displayplace.setText("USA East Coast"+" (TollFree)");
                holder.us_east_dialnumber_display.setText("+18446454399");
                holder.us_east_conferenceid_display.setText(eventlist.getConference());

                holder.us_west_displayplace.setText("USA West Coast"+" (TollFree)");
                holder.us_west_dialnumber_display.setText("+18446454398");
                holder.us_west_conferenceid_display.setText(eventlist.getConference());
                try {
                    if (!eventlist.getLeadershipnumber().isEmpty()){
                        holder.us_east_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_east_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_west_leadership.setText("#;");
                        holder.us_east_leadership.setText("#;");
                    }if (eventlist.getLeadershipnumber().isEmpty()) {
                        holder.us_east_leadership_display.setVisibility(View.GONE);
                        holder.us_east_leadership.setVisibility(View.GONE);
                        holder.us_west_leadership_display.setVisibility(View.GONE);
                        holder.us_west_leadership.setVisibility(View.GONE);
                    }
                }catch (NullPointerException e){

                }

                holder.liner_layout.addView(view_us_west);
                holder.liner_layout.addView(view_us_east);



            }
            if ((size==2)&&(!eventlist.getConference().isEmpty())&&(number.equalsIgnoreCase("18002660610"))){

                holder.us_east_displayplace=(TextView)view_us_east.findViewById(R.id.us_east_display_place);
                holder.us_east_dialnumber_display=(TextView)view_us_east.findViewById(R.id.us_east_dialnumber_display);
                holder.us_east_conferenceid=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid);

                holder.us_east_conferenceid_display=(TextView)view_us_east.findViewById(R.id.us_east_conferenceid_display);
                holder.us_east_leadership=(TextView)view_us_east.findViewById(R.id.us_east_leadership);
                holder.us_east_leadership_display=(TextView)view_us_east.findViewById(R.id.us_east_leadership_display);

                holder.us_west_displayplace=(TextView)view_us_west.findViewById(R.id.us_west_display_place);
                holder.us_west_dialnumber_display=(TextView)view_us_west.findViewById(R.id.us_west_dialnumber_display);
                holder.us_west_conferenceid=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid);
                holder.us_west_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.us_west_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.us_west_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);

             /*   holder.india_displayplace=(TextView)view_india.findViewById(R.id.us_west_display_place);
                holder.india_dialnumber_display=(TextView)view_india.findViewById(R.id.us_west_dialnumber_display);
                holder.india_conferenceid=(TextView)view_india.findViewById(R.id.us_west_conferenceid);
                holder.india_conferenceid_display=(TextView)view_us_west.findViewById(R.id.us_west_conferenceid_display);
                holder.india_leadership=(TextView)view_us_west.findViewById(R.id.us_west_leadership);
                holder.india_leadership_display=(TextView)view_us_west.findViewById(R.id.us_west_leadership_display);*/



                holder.us_east_displayplace.setText("USA East Coast"+" (TollFree)");
                holder.us_east_dialnumber_display.setText("+18446454399");
                holder.us_east_conferenceid_display.setText(eventlist.getConference());

                holder.us_west_displayplace.setText("USA West Coast"+" (TollFree)");
                holder.us_west_dialnumber_display.setText("+18446454398");
                holder.us_west_conferenceid_display.setText(eventlist.getConference());
                try {
                    if (!eventlist.getLeadershipnumber().isEmpty()){
                        holder.us_east_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_leadership_display.setText(eventlist.getLeadershipnumber().toString());
                        holder.us_west_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_east_ib_call.setBackgroundResource(R.drawable.dialer_color_selector);
                        holder.us_west_leadership.setText("#;");
                        holder.us_east_leadership.setText("#;");
                    }if (eventlist.getLeadershipnumber().isEmpty()) {
                        holder.us_east_leadership_display.setVisibility(View.GONE);
                        holder.us_east_leadership.setVisibility(View.GONE);
                        holder.us_west_leadership_display.setVisibility(View.GONE);
                        holder.us_west_leadership.setVisibility(View.GONE);
                    }
                }catch (NullPointerException e){

                }

                holder.liner_layout.addView(view_us_west);
                holder.liner_layout.addView(view_us_east);



            }


            if (!eventlist.getConference().isEmpty()){

                holder.liner_layout.addView(view);

            }

            holder.ib_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callPhoneNumber=  ((TextView) view.findViewById(R.id.dialnumber_display)).getText().toString();
                    callConferenceId= ((TextView) view.findViewById(R.id.conferenceid_display)).getText().toString();
                    leadership=((TextView) view.findViewById(R.id.leadership_display)).getText().toString();

                    Log.i("conferenceid",callConferenceId);
                    Log.d("ttttt",callPhoneNumber);
                    Log.d("leadership",leadership);
                    if (leadership.isEmpty()) {
                        dialNumberWithPermision(callPhoneNumber, callConferenceId);
                        Log.d("empty","--");
                    }else {
                        dialNumberWithLeader(callPhoneNumber, callConferenceId,leadership);
                    }
                }
            });
            holder.us_west_ib_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callPhoneNumber=((TextView) view_us_west.findViewById(R.id.us_west_dialnumber_display)).getText().toString();
                    callConferenceId= ((TextView) view_us_west.findViewById(R.id.us_west_conferenceid_display)).getText().toString();
                    leadership=((TextView) view_us_west.findViewById(R.id.us_west_leadership_display)).getText().toString();
                    Log.i("conferenceid",callConferenceId);
                    Log.i("callPhoneNumber",callPhoneNumber);
                   Log.d("leadership",leadership);
                    if (leadership.isEmpty()) {
                        dialNumberWithPermision(callPhoneNumber, callConferenceId);
                        Log.d("empty","--");
                    }else {
                        dialNumberWithLeader(callPhoneNumber, callConferenceId,leadership);
                    }
                }
            });
            holder.us_east_ib_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callPhoneNumber = ((TextView) view_us_east.findViewById(R.id.us_east_dialnumber_display)).getText().toString();
                    callConferenceId = ((TextView) view_us_east.findViewById(R.id.us_east_conferenceid_display)).getText().toString();

                        leadership = ((TextView) view_us_east.findViewById(R.id.us_east_leadership_display)).getText().toString();


                        Log.i("conferenceid", callConferenceId);
                        Log.i("callPhoneNumber", callPhoneNumber);
                        Log.d("leadership",leadership);
                        if (leadership.isEmpty()) {
                            dialNumberWithPermision(callPhoneNumber, callConferenceId);
                            Log.d("empty", "--");
                        } else {
                            dialNumberWithLeader(callPhoneNumber, callConferenceId, leadership);
                        }


                }
            });

        }

      //  System.out.println("===============================");
    }

    private void dialNumberWithLeader(String callPhoneNumber, String callConferenceId, String leadership) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
       // callIntent.setData(Uri.parse("tel:"+callPhoneNumber+";"+callConferenceId+"#"+","+"*"+";"+leadership+"#"));
        callIntent.setData(Uri.parse("tel:"+callPhoneNumber+"%3B"+callConferenceId+"%23"+"%3B"+"%2A"+"%2C"+leadership+"%23"));
       // callIntent.setData(Uri.parse("tel:"+callPhoneNumber+"%3B"+callConferenceId+"%23"+"%2C"+"%2A"+"%2C"+leadership+"%23"));
        //callIntent.setData(Uri.parse("tel:"+pnmu+","+conf+"#"));
        context.startActivity(callIntent);
    }


    public void dialNumberWithPermision(String pnmu, String callConferenceId){

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
       // Log.v(TAG+"0,1",pnmu.substring(0,2));

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        //callIntent.setData(Uri.parse("tel:"+pnmu+",,,,"+conf+"#"));
        callIntent.setData(Uri.parse("tel:"+pnmu+";"+callConferenceId+"#"));
        context.startActivity(callIntent);
       /* if(pnmu.substring(0,2).equalsIgnoreCase("+1")){

            Intent callIntent = new Intent(Intent.ACTION_CALL);
             callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
            //callIntent.setData(Uri.parse("tel:"+pnmu+","+conf+"#"));
            context.startActivity(callIntent);
          //  ((Activity) context).finish();
        }
        if(pnmu.substring(0,2).equalsIgnoreCase("18")){
            Intent callIntent = new Intent(Intent.ACTION_CALL);
             callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
           // callIntent.setData(Uri.parse("tel:"+pnmu+",,,,,"+conf+"#"));
            context.startActivity(callIntent);

        }else{*/

        //}


    }

    static class ViewHolder  {
        LinearLayout liner_layout;
        TextView time_display;
        TextView date_display;
        TextView time_zone_display;
        TextView title_display;
        TextView dialnumber_display;
        TextView conferenceid;
        TextView conferenceid_display;
        TextView leadership;
        ImageButton ib_call;
        LinearLayout mainlayout;
        TextView leadership_pin_display;
        TextView leadership_pin;
        TextView leasdership;
        TextView display_place;
        TextView leadership_display;
        TextView us_west_displayplace;
        TextView us_west_conferenceid;
        TextView us_west_dialnumber_display;
        ImageButton us_west_ib_call;
        TextView us_west_conferenceid_display;
        TextView us_west_leadership;
        TextView us_west_leadership_display;

        TextView us_east_displayplace;
        TextView us_east_conferenceid;
        TextView us_east_dialnumber_display;
        ImageButton us_east_ib_call;
        TextView us_east_conferenceid_display;
        TextView us_east_leadership;
        TextView us_east_leadership_display;


        TextView india_displayplace;
        TextView india_conferenceid;
        TextView india_dialnumber_display;
        ImageButton india_ib_call;
        TextView india_conferenceid_display;
        TextView india_leadership;
        TextView india_leadership_display;



    }
    public class TimeComparator implements Comparator<CallModel> {
        @Override
        public int compare(CallModel o1, CallModel o2) {
            return o1.getTime().compareToIgnoreCase(o2.getTime());
        }
    }
}
