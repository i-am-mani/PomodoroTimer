package com.omega.PomodoroTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
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

    private Handler progressHandler = new Handler();

    private Thread mUpdateProgressThread = new Thread(new Runnable() {
        @Override
        public void run() {
            float progress = mTimerService.getProgress();
            long time = mTimerService.getCurTime();

            States curState = mTimerService.getState();

            if (curState == States.Interval) {
                if (progressBar.getProgressDrawable() == getDrawable(R.drawable.tomato_progress_bar)) {
                  progressBar.setProgress((int)progress);
                }
                else{
                    progressBar.setProgressDrawable(getDrawable(R.drawable.tomato_progress_bar));
                    progressBar.setProgress((int)progress);
                }
            } else{

            }

            int seconds = (int) (time / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvTime.setText(String.format("%d:%02d", minutes, seconds));

            Log.d(TAG, "run: time = " + String.format("%d:%02d", minutes, seconds));

            if (progress >= 100) {
                btnStart.setImageResource(R.drawable.ic_play);
                buttonState = ButtonStates.Play;
            }

            progressHandler.postDelayed(this, 500);
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
            btnStart.animate().scaleY(0).scaleX(0).setDuration(500).withEndAction(()->{
                btnStart.setImageResource(R.drawable.ic_pause);
                btnStart.animate().scaleX(1).scaleY(1);});

            buttonState = ButtonStates.Pause;
            States state = mTimerService.startTimer();
            handleState(state);
        } else if (buttonState == ButtonStates.Pause) {
            btnStart.animate().scaleY(0).scaleX(0).setDuration(500).withEndAction(()->{
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
