package com.omega.PomodoroTimer.Services;

import android.app.Notification;
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

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
    }

    private void buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),"CHANNEL_ID_IS_TIMER")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent);

        startForeground(1337, notification.build());
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
}
