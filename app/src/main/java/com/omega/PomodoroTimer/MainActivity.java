package com.omega.PomodoroTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.omega.PomodoroTimer.Services.TimerService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    private enum ButtonStates{
        Play,Pause
    }

    public enum States{
        Resumed,Playing,Paused,ShortBreak,LongBreak,Interval
    }

    @BindView(R.id.button_start)
    ImageButton btnStart;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.text_time)
    TextView tvTime;

    ServiceConnection mServiceConnection = new TimerServiceConnection();
    TimerService mTimerService = null;

    ButtonStates buttonState = ButtonStates.Play;

    @BindView(R.id.layout_main)
    ViewGroup viewGroup ;

    States mProgressBarStatus = States.Interval;

    private Handler progressHandler = new Handler();

    private Thread mUpdateProgressThread = new Thread(new Runnable() {



        @Override
        public void run() {
            setTime();
            setProgress();
            changeProgressBarDrawable();
            progressHandler.postDelayed(this, 500);
        }

        private void changeProgressBarDrawable() {
            States curState = mTimerService.getState();
            if ( mProgressBarStatus != curState) {
                setDrawable(curState);
                setButtonResource();
            }
        }

        private void setDrawable(States curState) {
            if (curState == States.Interval) {
                initTimerBackground(curState,R.drawable.tomato_progress_bar);
            } else if (curState == States.ShortBreak) {
                initTimerBackground(curState, R.drawable.coffee_break_bar);
            } else if (curState == States.LongBreak) {
                initTimerBackground(curState, R.drawable.orange_drink_bar);
            }
        }

        private void setButtonResource() {
            btnStart.setImageResource(R.drawable.ic_play);
            buttonState = ButtonStates.Play;
        }

        private void setProgress() {
            int progress =(int) mTimerService.getProgress();
            progressBar.setProgress(progress);
        }

        private void setTime() {
            long time = mTimerService.getCurTime();
            int seconds = (int) (time / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            tvTime.setText(String.format("%d:%02d", minutes, seconds));
        }

        private void initTimerBackground(States curState, int p) {
            progressBar.animate().scaleX(0).scaleY(0).setDuration(250).withEndAction(() -> {
                progressBar.setProgressDrawable(getDrawable(p));
                progressBar.animate().scaleX(1).scaleY(1).setDuration(250);
            });
            mProgressBarStatus = curState;
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
        bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
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
            btnStart.animate().scaleY(0).scaleX(0).setDuration(250).withEndAction(()->{
                btnStart.setImageResource(R.drawable.ic_pause);
                btnStart.animate().scaleX(1).scaleY(1);});

            buttonState = ButtonStates.Pause;
            States state = mTimerService.startTimer();
            handleState(state);
        } else if (buttonState == ButtonStates.Pause) {
            btnStart.animate().scaleY(0).scaleX(0).setDuration(250).withEndAction(()->{
                btnStart.setImageResource(R.drawable.ic_play);
                btnStart.animate().scaleX(1).scaleY(1);});
            buttonState = ButtonStates.Play;

            mTimerService.pauseTimer();
        }
    }

    private void handleState(States state) {
        if (state == States.Playing) {
            setProgressbarListener();
        }
    }

    private void setProgressbarListener() {
        tvTime.postDelayed(mUpdateProgressThread, 0);
    }


    private class TimerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.ServiceBinder timerService = (TimerService.ServiceBinder) service;
            mTimerService = timerService.getService();
            Log.d(TAG, "onServiceConnected: service alive ?  " + mTimerService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
        }
    }
}
