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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.omega.PomodoroTimer.MainActivity;
import com.omega.PomodoroTimer.MainActivity.States;
import com.omega.PomodoroTimer.R;

public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 25515;
    private static final String ACTION_STOP_SERVICE = "0";
    private static final String ACTION_PAUSE_TIMER = "1";
    private static final String ACTION_START_TIMER = "2";
    private boolean PAUSE = false;
    private IBinder serviceBinder = new ServiceBinder();
    private long mStartTime;
    private int mIntervals = 4; // default intervals in each Pomodoro
    private int mCurInterval = 0; // current interval number
    private long mCurTime = 0; // current time in seconds, updated by thread.
    private long mIntervalLength = 0; // interval end time for thread,could be 5 or 15 or 25
    private String TAG = getClass().getSimpleName();


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
            systemService.cancel(NOTIFICATION_ID);
            stopSelf();
        } else if (ACTION_PAUSE_TIMER.equals(intent.getAction())) {
            pauseTimer();
            updateNotification();
        } else if (ACTION_START_TIMER.equals(intent.getAction())) {
            startTimer();
            updateNotification();
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
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(this, "interval")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(getPendingIntent());

        String title = getNotificationTitle();
        String content = getNotificationContent();
        int progress = (int)getProgress();

        notification.setContentTitle(title);
        notification.setContentText(content);
        notification.setProgress(100, progress, false);

        setNotificationActions(notification);
        return notification;
    }

    private String getNotificationContent() {
        long time = getCurTime();
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    private String getNotificationTitle() {
        String title = "";
        States state = getState();

        if (state == States.Interval) {
            title = "Pomodoro";
        } else if (state == States.LongBreak) {
            title = "Long Break : Break-Time!";
        } else if (state == States.ShortBreak) {
            title = "Short Break";
        }
        return title;
    }

    private void setNotificationActions(NotificationCompat.Builder notification) {
        Intent pauseTimer = new Intent(this, TimerService.class);
        pauseTimer.setAction(this.ACTION_PAUSE_TIMER);
        Intent startTimer = new Intent(this, TimerService.class);
        startTimer.setAction(this.ACTION_START_TIMER);
        Intent stopSelf = new Intent(this, TimerService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);

        notification.addAction(R.drawable.ic_pause, "Close", PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT));
        if (PAUSE || mCurTime == 0) {
            notification.addAction(R.drawable.ic_play, "Start", PendingIntent.getService(this, 0, startTimer, PendingIntent.FLAG_CANCEL_CURRENT));
        } else{
            notification.addAction(R.drawable.ic_pause, "Pause", PendingIntent.getService(this, 0, pauseTimer, PendingIntent.FLAG_CANCEL_CURRENT));
        }
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

    private void updateNotification() {
        NotificationCompat.Builder notification = getNotificationBuilder();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID,notification.build());
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
                        updateNotification();
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
                updateNotification();
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
