package com.omega.PomodoroTimer.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.omega.PomodoroTimer.MainActivity;
import com.omega.PomodoroTimer.R;

public class TimerService extends Service {

    private IBinder serviceBinder = new ServiceBinder();
    private long mStartTime;
    private int mIntervals = 4 ; // default intervals in each Pomodoro
    private int mCurInterval = 0; // current interval number
    private long mCurTime; // current time in seconds, updated by thread.
    private long mIntervalLength; // interval end time for thread

    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),"CHANNEL_ID_IS_TIMER")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("My Awesome App")
            .setContentIntent(getPendingIntent());

    private PendingIntent getPendingIntent() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        return pendingIntent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class ServiceBinder extends Binder {

        public TimerService getService(){
            return TimerService.this;
        }
    }

    private void buildNotification() {


        startForeground(1337, notification.build());
    }

    public void startInterval() {
        if (mCurInterval % 2 == 0) {
            mIntervalLength = convertMinToMillis(25);
        } else if (mCurInterval == mIntervals){
            mIntervalLength = convertMinToMillis(15);
        } else{
            mIntervalLength = convertMinToMillis(5);
        }
        mStartTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCurTime >= mIntervalLength) {
                    try {
                        Thread.sleep(500);
                        mCurTime = System.currentTimeMillis() - mStartTime;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mCurInterval++;
            }
        }).start();
    }

    private long convertMinToMillis(int i) {
        return i*60*1000;
    }
}
