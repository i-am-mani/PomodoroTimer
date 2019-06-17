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
        mIntervalLength = getIntervalLength();
        if (mIntervalLength != -1) {
            runTimer();
        }
    }

    private void runTimer() {
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
                incrementTimer();
            }

            private void incrementTimer() {
                if (mCurInterval == mIntervals) {  // Update Interval
                    mCurInterval = 0;
                } else{
                    mCurInterval ++;
                }
                mCurTime = 0; // Reset Timer
            }
        }).start();
    }

    private long getIntervalLength() {
        States currentState = getState();

        switch (currentState) {
            case Interval:
                return convertMinToMillis(25);
            case LongBreak:
                return convertMinToMillis(15);
            case ShortBreak:
                return convertMinToMillis(5);
        }

        return -1;
    }

    public void pauseTimer() {
        PAUSE = true;
    }

    public void resumeTimer() {
        PAUSE = false;
    }

    public float getProgress() {
        float progress;
        // Current time wrt to starting time divided by interval time both in millis
        progress = (float) ( mCurTime / mCurInterval) * 100;
        return progress;
    }

    public long getCurTime(){
        return mCurTime;
    }

    public States getState() {
        if (mCurInterval == mIntervals) {
            return States.LongBreak;
        } else if (mCurInterval % 2 != 0) {
            return States.ShortBreak;
        } else if (mCurInterval % 2 == 0) {
            return States.Interval;
        }

        return null;
    }

    public States startTimer() {
        if (mCurTime != 0) {
            resumeTimer();  // start if paused
            return States.Resumed;
        } else if(mCurTime == 0){
            startInterval(); // start the -timer-
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
