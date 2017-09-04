package com.juniper.jconference.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.juniper.jconference.service.ClockAppService;
import com.juniper.jconference.service.EventsService;

import java.util.Calendar;

public class OnAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        WakefulIntentService.sendWakefulWork(context, ClockAppService.class);
        PendingIntent service = null;
        Intent intentForService = new Intent(context.getApplicationContext(), EventsService.class);
        final AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        final Calendar time = Calendar.getInstance();
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        if (service == null) {
            service = PendingIntent.getService(context, 0,
                    intentForService, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 10000, service);
        Log.i("OnAlarmReceiver","onReceive");
    }
}
