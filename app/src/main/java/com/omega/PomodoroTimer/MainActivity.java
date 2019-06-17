package com.omega.PomodoroTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.omega.PomodoroTimer.Services.TimerService;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    ServiceConnection mServiceConnection = new TimerServiceConnection();
    TimerService mTimerService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent,mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        mTimerService = null;
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
