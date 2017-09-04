package com.juniper.jconference.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.juniper.jconference.service.EventsService;

public class PowerAlaramReceiver extends WakefulBroadcastReceiver {
    private PowerManager.WakeLock wakeLock;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE,"Wake Lock");
        wakeLock.acquire(15*1000);
       if (pm.isInteractive()){
           Intent service = new Intent(context, EventsService.class);
           startWakefulService(context, service);
           Log.i("PowerAlaramReceiver","isScreenOn");
       }
        Log.i("PowerAlaramReceiver","on receiver");

        Intent service = new Intent(context, EventsService.class);
        startWakefulService(context, service);
        // Start the service, keeping the device awake while it is launching.
        Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());

    }
}
