package com.omega.PomodoroTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.omega.PomodoroTimer.Services.TimerService;

public class MainActivity extends AppCompatActivity {

    ServiceConnection mServiceConnection = new TimerServiceConnection();
    TimerService mTimerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private class TimerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.ServiceBinder timerService = (TimerService.ServiceBinder) service;
            mTimerService = timerService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
        }
    }
}
