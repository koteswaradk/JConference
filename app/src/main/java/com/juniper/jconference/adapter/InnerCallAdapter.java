package com.juniper.jconference.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by koteswara on 29/06/17.
 */

public class InnerCallAdapter extends BaseAdapter {
    private static final String TAG= "JcallBaseAdapter";
    private LayoutInflater layoutInflater;
    public ArrayList<CallModel> eventlist;
    Context context; String callPhoneNumber,callConferenceId;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE=143;
    public InnerCallAdapter(Context context, ArrayList<CallModel> eventlist){
        this.context=context;
        this.eventlist=eventlist;
        Collections.sort(this.eventlist, new InnerCallAdapter.TimeComparator());
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
        setItems(conferencecallmodel,position,viewHolder);
        for (int i = 0; i < conferencecallmodel.getNumberList().size(); i++) {
            // Log.d(TAG+"list",conferencecallmodel.getNumberList().get(i));
        }
        viewHolder.mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG+"uuuuuuu",conferencecallmodel.getDetails());
                showInfoDialog(conferencecallmodel.getDetails());
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
                    public void onClick(DialogInterface dialog, int id) {
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
            final View view = inflater.inflate(R.layout.dynamicadd_list_old, null);
            holder.dialnumber_display = (TextView) view.findViewById(R.id.dialnumber_display);
            holder.conferenceid_display = (TextView) view.findViewById(R.id.conferenceid_display);
            holder.ib_call = (ImageButton) view.findViewById(R.id.ib_call);
            // Log.d(TAG+"number",eventlist.getNumberList().get(i));
            holder.dialnumber_display.setText(eventlist.getNumberList().get(i));
            holder.conferenceid_display.setText(eventlist.getConference());
            if (!eventlist.getConference().isEmpty()){
                holder.liner_layout.addView(view);
            }
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
        LinearLayout mainlayout;



    }
    public class TimeComparator implements Comparator<CallModel> {
        @Override
        public int compare(CallModel o1, CallModel o2) {
            return o1.getTime().compareToIgnoreCase(o2.getTime());
        }
    }

}
