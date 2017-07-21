package com.juniper.jconference.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.juniper.jconference.R;
import com.juniper.jconference.model.CallModel;

import java.util.ArrayList;

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
            convertView = layoutInflater.inflate(R.layout.event_row, null);
            viewHolder = new ViewHolder();


            viewHolder.time_display=(TextView)convertView.findViewById(R.id.time_display);
            viewHolder.date_display=(TextView)convertView.findViewById(R.id.date_display);
            viewHolder.time_zone_display=(TextView)convertView.findViewById(R.id.time_zone_display);
            viewHolder.title_display=(TextView)convertView.findViewById(R.id.title_display);
            viewHolder.dialnumber_display=(TextView)convertView.findViewById(R.id.dialnumber_display);
            viewHolder.conferenceid_display=(TextView)convertView.findViewById(R.id.conferenceid_display);

            viewHolder.conferenceid=(TextView)convertView.findViewById(R.id.conferenceid);
            viewHolder.dialnumber=(TextView)convertView.findViewById(R.id.dialnumber);

            viewHolder.ib_call=(ImageButton)convertView.findViewById(R.id.ib_call);


            convertView.setTag(viewHolder);
        }else {
            viewHolder = (InnerCallAdapter.ViewHolder) convertView.getTag();
        }

        final CallModel conferencecallmodel = (CallModel) eventlist.get(position);

        viewHolder.time_display.setText(conferencecallmodel.getTime());
        viewHolder.time_zone_display.setText(conferencecallmodel.getTimezone());
        viewHolder.date_display.setText(conferencecallmodel.getDate());
        viewHolder.title_display.setText(conferencecallmodel.getTitle());
        viewHolder.dialnumber_display.setText(conferencecallmodel.getPhNumber());
        viewHolder.conferenceid_display.setText(conferencecallmodel.getConference());


        if (conferencecallmodel.getConference().isEmpty()){
            viewHolder.ib_call.setVisibility(View.GONE);
            viewHolder.dialnumber.setVisibility(View.GONE);
            viewHolder.conferenceid.setVisibility(View.GONE);
            viewHolder.conferenceid_display.setText("No Conference Call Available");
        }
        if (!conferencecallmodel.getConference().isEmpty()){
            viewHolder.ib_call.setVisibility(View.VISIBLE);
            viewHolder.dialnumber.setVisibility(View.VISIBLE);
            viewHolder.conferenceid.setVisibility(View.VISIBLE);

        }
        viewHolder.ib_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhoneNumber= conferencecallmodel.getPhNumber();
                callConferenceId= conferencecallmodel.getConference();
                dialNumberWithPermision(callPhoneNumber,callConferenceId);
            }
        });


        notifyDataSetChanged();
        return convertView;
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
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+pnmu+";"+conf+"#"));
        context.startActivity(callIntent);

    }

    static class ViewHolder{

        TextView time_display;
        TextView date_display;
        TextView time_zone_display;
        TextView title_display;
        TextView dialnumber_display;
        TextView conferenceid_display;
        TextView conferenceid;
        TextView dialnumber;
        ImageButton ib_call;



    }
}
