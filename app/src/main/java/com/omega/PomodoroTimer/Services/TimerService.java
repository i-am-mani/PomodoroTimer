package com.omega.PomodoroTimer.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.omega.PomodoroTimer.MainActivity;
import com.omega.PomodoroTimer.MainActivity.States;
import com.omega.PomodoroTimer.R;

public class TimerService extends Service {

    private IBinder serviceBinder = new ServiceBinder();
    private boolean PAUSE = false;
    private long mStartTime;
    private int mIntervals = 4; // default intervals in each Pomodoro
    private int mCurInterval = 0; // current interval number
    private long mCurTime = 0; // current time in seconds, updated by thread.
    private long mIntervalLength = 0; // interval end time for thread

    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID_IS_TIMER")
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

        public TimerService getService() {
            return TimerService.this;
        }
    }

    private void buildNotification() {
        startForeground(1337, notification.build());
    }

    public void startInterval() {
        if (mCurInterval % 2 == 0) {
            mIntervalLength = convertMinToMillis(25);
        } else if (mCurInterval == mIntervals) {
            mIntervalLength = convertMinToMillis(15);
        } else {
            mIntervalLength = convertMinToMillis(5);
        }
        mStartTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCurTime >= mIntervalLength) {
                    try {
                        Thread.sleep(500);
                        // If timer isn't paused, play the timer.
                        if (!PAUSE) {
                            mCurTime = System.currentTimeMillis() - mStartTime;  // Update time wrt to started time in MILLIS
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mCurInterval++; // The interval is over, increment to next interval.
                mCurTime = 0;
            }
        }).start();
    }

    public void pauseTimer() {
        PAUSE = true;
    }

    public void resumeTimer() {
        PAUSE = false;
    }

    public int getProgress() {
        int progress;
        // Current time wrt to starting time divided by interval time both in millis
        progress = (int) ( mCurTime / mCurInterval) * 100;
        return progress;
    }

    public long getCurTime(){
        return mCurTime;
    }

    public States startTimer() {
        if (mCurTime != 0) {
            startInterval();  // Since the timer count isn't zero it's already running, start if paused
            return States.Resumed;
        } else if(mCurTime == 0){
            startInterval(); // start the timer
            return States.Playing;
        } else{
            pauseTimer();
            return States.Paused;
        }
    }

    private long convertMinToMillis(int i) {
        return i * 60 * 1000;
    }
}
