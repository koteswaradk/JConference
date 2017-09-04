package com.juniper.jconference.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by koteswara on 8/31/17.
 */

public class TimeZoneCallAdapter extends BaseAdapter{
    private static final String TAG= "JcallBaseAdapter";
    private LayoutInflater layoutInflater;
    public ArrayList<CallModel> eventlist;

    HashMap<String,String>number_place_map=new HashMap<>();

    Context context; String callPhoneNumber,callConferenceId;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE=143;
    public TimeZoneCallAdapter(Context context, ArrayList<CallModel> eventlist){
        this.context=context;
        this.eventlist=eventlist;

        number_place_map.put("+61289139885","Australia Sydney");
        number_place_map.put("+3224033300","Belgium Brussels");
        number_place_map.put("+16135912790","Canada Kanata");
        number_place_map.put("+861058126200","China_Beijing");
        number_place_map.put("+862085115999","China_Guangzhou");
        number_place_map.put("+85231022166","China_Hong_Kong");
        number_place_map.put("+862161415199","China_Shanghai");
        number_place_map.put("+33147756336","France_Paris");
        number_place_map.put("+4989203012111","Germany_Munich");
        number_place_map.put("18002660610","India_Bangalore");
        number_place_map.put("+35318903656","Ireland_Dublin");
        number_place_map.put("+390236014377","Italy_Milan");
        number_place_map.put("+81353330594","Japan_Tokyo");
        number_place_map.put("+82234833444","Korea_Seoul");
        number_place_map.put("+60320790002","Malaysia_Kuala_Lumpur");
        number_place_map.put("+31207125790","Netherlands_Amsterdam");
        number_place_map.put("+966112804200","Saudi_Arabia_Riyadh");
        number_place_map.put("+6565113580","Singapore_Singapore");
        number_place_map.put("+34914143451","Spain_Madrid");
        number_place_map.put("+46850644666","Sweeden_Stockhol");
        number_place_map.put("+886221756353","Taiwan_Taipei");
        number_place_map.put("+97143611505","UAE_Dubai");
        number_place_map.put("+441372389100","UK_Addlestone");
        number_place_map.put("+19785898300","USA_East_Coast");
        number_place_map.put("+14089369000","USA_West_Coast");




        Collections.sort(this.eventlist, new TimeZoneCallAdapter.TimeComparator());
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
            convertView = layoutInflater.inflate(R.layout.timezonecallrow, null);
            viewHolder = new TimeZoneCallAdapter.ViewHolder();


            viewHolder.time_display=(TextView)convertView.findViewById(R.id.time_display);
            viewHolder.date_display=(TextView)convertView.findViewById(R.id.date_display);
            viewHolder.time_zone_display=(TextView)convertView.findViewById(R.id.time_zone_display);
            viewHolder.title_display=(TextView)convertView.findViewById(R.id.title_display);
            viewHolder.mainlayout=(LinearLayout)convertView.findViewById(R.id.main_layout);
            viewHolder.liner_layout=(LinearLayout) convertView.findViewById(R.id.add_list_layout);
            //viewHolder.conferenceid_display=(TextView) convertView.findViewById(R.id.confeernce_id_time_display);
            viewHolder.dialnumber_rellayout= (RelativeLayout)convertView.findViewById(R.id.dialnumber_rellayout);
            viewHolder.ib_call_timezone=(ImageButton) convertView.findViewById(R.id.ib_dial_call);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (TimeZoneCallAdapter.ViewHolder) convertView.getTag();
        }

        final CallModel conferencecallmodel = (CallModel) eventlist.get(position);

        viewHolder.time_display.setText(conferencecallmodel.getTime());
        viewHolder.time_zone_display.setText(conferencecallmodel.getTimezone());
        viewHolder.date_display.setText(conferencecallmodel.getDate());
        viewHolder.title_display.setText(conferencecallmodel.getTitle());
       // viewHolder.conferenceid_display.setText(conferencecallmodel.getConference());


                //+" Timezon id :: " +tz.getID());
        setItems(conferencecallmodel,position,viewHolder);

        if (!conferencecallmodel.getConference().isEmpty()){
            viewHolder.dialnumber_rellayout.setVisibility(View.VISIBLE);
        }
        if (conferencecallmodel.getConference().isEmpty()){
            viewHolder.dialnumber_rellayout.setVisibility(View.GONE);
        }
        viewHolder.mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Log.d(TAG+"uuuuuuu",conferencecallmodel.getDetails());
                showInfoDialog(conferencecallmodel.getDetails());
            }
        });
        viewHolder.ib_call_timezone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeZone tz = TimeZone.getDefault();
                System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT));
              //String number=  getnumberBasedOnTimeZone(tz.getDisplayName(false, TimeZone.SHORT));



            }
        });
        notifyDataSetChanged();

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

    private void setItems(CallModel eventlist, int position, TimeZoneCallAdapter.ViewHolder holder) {
        holder.liner_layout.removeAllViews();
        //  System.out.println("===============================");

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < eventlist.getNumberList().size(); i++) {
            final View view = inflater.inflate(R.layout.dynamicadd_row, null);
            holder.dialnumber_display = (TextView) view.findViewById(R.id.dialnumber_display);
            holder.conferenceid_display = (TextView) view.findViewById(R.id.conferenceid_display);
            holder.ib_call = (ImageButton) view.findViewById(R.id.ib_call);
            // Log.d(TAG+"number",eventlist.getNumberList().get(i));
            holder.dialnumber_display.setText(eventlist.getNumberList().get(i));
            holder.conferenceid_display.setText(eventlist.getConference());
            if (!eventlist.getConference().isEmpty()){
                holder.liner_layout.addView(view);

            }
            holder.liner_layout.setVisibility(View.GONE);
            holder.ib_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callPhoneNumber=  ((TextView) view.findViewById(R.id.dialnumber_display)).getText().toString();
                    callConferenceId= ((TextView) view.findViewById(R.id.conferenceid_display)).getText().toString();

                    dialNumberWithPermision(callPhoneNumber,callConferenceId);
                }
            });

        }

        //  System.out.println("===============================");
    }


    public void dialNumberWithPermision(String pnmu, String conf){

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
        Log.v(TAG+"0,1",pnmu.substring(0,2));

        if(pnmu.substring(0,2).equalsIgnoreCase("+1")){

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            // callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
            callIntent.setData(Uri.parse("tel:"+pnmu+","+conf+"#"));
            context.startActivity(callIntent);
        }
        if(pnmu.substring(0,2).equalsIgnoreCase("18")){
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            // callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
            callIntent.setData(Uri.parse("tel:"+pnmu+",,,,,"+conf+"#"));
            context.startActivity(callIntent);
        }else{
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+pnmu+",,,,"+conf+"#"));
            // callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
            context.startActivity(callIntent);
        }


    }

    static class ViewHolder{
        LinearLayout liner_layout;
        TextView time_display;
        TextView date_display;
        TextView time_zone_display;
        TextView title_display;
        TextView dialnumber_display;
        TextView conferenceid_display;
        ImageButton ib_call;
        ImageButton ib_call_timezone;
        LinearLayout mainlayout;
        RelativeLayout dialnumber_rellayout;

    }
    public class TimeComparator implements Comparator<CallModel> {
        @Override
        public int compare(CallModel o1, CallModel o2) {
            return o1.getTime().compareToIgnoreCase(o2.getTime());
        }
    }
}
