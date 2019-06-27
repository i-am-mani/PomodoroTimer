package com.omega.PomodoroTimer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.omega.PomodoroTimer.MainActivity;
import com.omega.PomodoroTimer.MainActivity.States;
import com.omega.PomodoroTimer.R;

public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 25515;
    private static final String ACTION_STOP_SERVICE = "123";
    private IBinder serviceBinder = new ServiceBinder();
    private boolean PAUSE = false;
    private long mStartTime;
    private int mIntervals = 4; // default intervals in each Pomodoro
    private int mCurInterval = 0; // current interval number
    private long mCurTime = 0; // current time in seconds, updated by thread.
    private long mIntervalLength = 0; // interval end time for thread,could be 5 or 15 or 25
    private String TAG = getClass().getSimpleName();
    NotificationCompat.Builder notification;


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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            NotificationManager systemService = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d(TAG,"called to cancel service");
            systemService.cancel(NOTIFICATION_ID);
            stopSelf();
        }
        return START_NOT_STICKY;
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
        createNotificationChannel();
        Intent stopSelf = new Intent(this, TimerService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.finish_dialog);
//        remoteViews.setInt(R.id.imageView2,"setBackgroundResource",R.layout.finish_dialog);
        notification = new NotificationCompat.Builder(this, "interval")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentIntent(getPendingIntent()).setProgress(100, 30, false)
//                .setCustomBigContentView(remoteViews)
                .addAction(R.drawable.ic_pause, "Close", PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT));
        startForeground(NOTIFICATION_ID, notification.build());
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Interval";
            String description = "Timer is running";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("interval", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startInterval() {
        mIntervalLength = getIntervalLength();
        if (mIntervalLength != -1) {
            runTimer();
        }
    }

    /**
     * Starts Timer thread to update time until it is greater then specified interval length
     */
    private void runTimer() {
        mStartTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCurTime <= (mIntervalLength + 1000)) {
                    try {
                        Thread.sleep(500);
                        // If timer isn't paused, play the timer.
                        if (!PAUSE) {
                            mCurTime = System.currentTimeMillis() - mStartTime;  // Update time wrt to started time in MILLIS
                            Log.d(TAG, "run: mCurtime " + mCurTime);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                incrementTimer();
            }

            private void incrementTimer() {
                // Update Interval
                if (mCurInterval == mIntervals) {
                    mCurInterval = 0;
                } else{
                    mCurInterval ++;
                }
                mCurTime = 0; // Reset Timer
            }
        }).start();
    }

    /**
     * Returns Interval length based on current state ( States - enum )
     * @return millis in current interval
     */
    private long getIntervalLength() {
        States currentState = getState();

        switch (currentState) {
            case Interval:
                return convertMinToMillis(1);
            case LongBreak:
                return convertMinToMillis(15);
            case ShortBreak:
                return convertMinToMillis(1);
        }

        return -1;
    }

    public void pauseTimer() {
        PAUSE = true;
    }

    public void resumeTimer() {
        PAUSE = false;
        // Calculate relative time, (current time - time before pause)
        mStartTime = System.currentTimeMillis()  - mCurTime;
    }

    public float getProgress() {
        float progress;
        // Current time wrt to starting time divided by interval time both in millis
        Log.d(TAG, "getProgress: mCurTime = " + mCurTime + " mLength " + mIntervalLength);
        progress = ((float)mCurTime) / mIntervalLength * 100;
        Log.d(TAG, "getProgress: " + progress);
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
            startInterval(); // start the timer
            return States.Playing;
        } else{
            pauseTimer();
            return States.Paused;
        }
    }

    private long convertMinToMillis(float i) {
        return  10 * 1000;
    }
}
