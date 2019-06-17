package com.omega.PomodoroTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.omega.PomodoroTimer.Services.TimerService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private enum ButtonStates{
        Play,Pause;
    }

    public enum States{
        Resumed,Playing,Paused,ShortBreak,LongBreak,Interval;
    }

    @BindView(R.id.button_start)
    ImageButton btnStart;

    @BindView(R.id.progress_bar_outer)
    CircularProgressBar pbOuter;

    @BindView(R.id.progress_bar_inner)
    CircularProgressBar pbInner;

    ServiceConnection mServiceConnection = new TimerServiceConnection();
    TimerService mTimerService = null;

    ButtonStates buttonState = ButtonStates.Play;

    private Handler progressHandler = new Handler();

    private Thread mUpdateProgressThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int progress = mTimerService.getProgress();
            long time = mTimerService.getCurTime();

        }
    });

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
        //TODO Resume existing timer if found running
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        mTimerService = null;
    }

    @OnClick(R.id.button_start)
    public void manageTimerState(View view) {
        if (buttonState == ButtonStates.Play) {
            btnStart.setImageResource(R.drawable.ic_pause);
            States state = mTimerService.startTimer();
            handleState(state);
        }
    }

    private void handleState(States state) {
        if (state == States.Playing) {
            setProgressbarListener();
        }
    }

    private void setProgressbarListener() {

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
