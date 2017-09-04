package com.juniper.jconference.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;



import java.util.Calendar;


/**
 * Created by koteswara on 8/29/17.
 */

public class ClockAppService extends WakefulIntentService {
    public ClockAppService(String name) {
        super(name);
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        PendingIntent service = null;
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
        Log.i("ClockAppService","doWakefulWork");
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTime().getTime(), 10000, service);

    }
}
