package com.example.timer;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment implements Switch.OnCheckedChangeListener {

    public TimerFragment() {
        // Required empty public constructor
    }

    SharedPreferences sp;
    MediaPlayer mpGoing, mpUp;
    AudioManager manager;
    Vibrator vibrator;
    Speaker speaker;
    TextView timerText;
    Button[] key;
    Button start_stop, clear;
    Switch ttsSwitch;
    ProgressBar progressBar;
    ObjectAnimator animation;
    Timer timer;
    boolean isPaused, isTimeUp, isStarted, isCountdownOn,
            isGoingRingtoneOn, isGoingVibrationOn, isUpRingtoneOn, isUpVibrationOn, useSameSound;
    int[] id = new int[]{R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
            R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9};
    int h, m, s, setSecTime, interval, countStart;
    long[] pat = {300, 300, 300, 500};
    long hh, mm, ss, ms;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        key = new Button[10];
        for (int i = 0; i < key.length; i++) {
            key[i] = view.findViewById(id[i]);
            key[i].setOnClickListener(new TenKeyClickListener());
        }
        progressBar = view.findViewById(R.id.progressBar);
        ttsSwitch = view.findViewById(R.id.timer_tts_switch);
        ttsSwitch.setOnCheckedChangeListener(this);
        start_stop = view.findViewById(R.id.timer_key_start);
        start_stop.setEnabled(false);
        start_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    //一時停止再開
                    if (isPaused) {
                        isPaused = false;
                        setSecTime = (int) (hh * 3600 + mm * 60 + ss);
                        timer = new Timer(setSecTime * 1000 + ms, 100);

                        animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getMax());
                        animation.setDuration(setSecTime * 1000 + ms);
                        animation.setInterpolator(new LinearInterpolator());
                        animation.start();
                        timer.start();
                        start_stop.setText(R.string.timer_pause);
                    }
                    //停止
                    else if (isTimeUp) {
                        isTimeUp = false;
                        isStarted = false;
                        progressBar.setProgress(0);
                        timerText.setText(String.format("%02d:%02d:%02d", h, m, s));
                        start_stop.setText(R.string.timer_start);
                    } else {
                        isPaused = true;
                        timer.cancel();
                        animation.cancel();
                        start_stop.setText(R.string.timer_start);
                    }
                } else {
                    //開始
                    isStarted = true;
                    m += s / 60;
                    h += m / 60;
                    s %= 60;
                    m %= 60;
                    setSecTime = h * 3600 + m * 60 + s;
                    timer = new Timer(setSecTime * 1000, 100);
                    progressBar.setProgress(0);
                    progressBar.setMax(setSecTime * 10);
                    for (Button button : key) {
                        button.setEnabled(false);
                    }
                    animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.getMax());
                    animation.setDuration(setSecTime * 1000);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                    timer.start();
                    Log.d("Timer", "Timer start!");
                    start_stop.setText(R.string.timer_pause);

                }
            }
        });

        clear = view.findViewById(R.id.timer_key_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (animation != null) {
                    animation.cancel();
                    animation = null;
                }
                isStarted = false;
                isTimeUp = false;
                isPaused = false;
                h = 0;
                m = 0;
                s = 0;
                timerText.setText(R.string.timer_initial_time);
                start_stop.setText(R.string.timer_start);
                start_stop.setEnabled(false);
                progressBar.setProgress(0);
                for (Button button : key) {
                    button.setEnabled(true);
                }
            }
        });
        speaker = new Speaker(getActivity());
        timerText = view.findViewById(R.id.time);
        timerText.setText(R.string.timer_initial_time);
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && manager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            Toast.makeText(getActivity(), "マナーモードに設定されています", Toast.LENGTH_SHORT).show();
        } else if (isChecked && manager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(getActivity(), "音量が0に設定されています", Toast.LENGTH_SHORT).show();
        }
    }

    private class TenKeyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            start_stop.setText(R.string.timer_start);
            Button button = (Button) v;
            if (h < 10) {
                s = s * 10 + Integer.parseInt(button.getText().toString());
                m = m * 10 + s / 100;
                s %= 100;
                h = h * 10 + m / 100;
                m %= 100;
            }
            if (h + m + s > 0) {
                start_stop.setEnabled(true);
            }
            timerText.setText(String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    @Override
    public void onResume() {
        isGoingVibrationOn = sp.getBoolean("timer_going_switch_vibration", false);
        isGoingRingtoneOn = sp.getBoolean("timer_going_switch_ringtone", false);
        isUpVibrationOn = sp.getBoolean("timer_up_switch_vibration", false);
        isUpRingtoneOn = sp.getBoolean("timer_up_switch_ringtone", false);
        isCountdownOn = sp.getBoolean("timer_switch_countdown", false);
        useSameSound = !sp.getBoolean("timer_up_checkbox", false);
        countStart = sp.getInt("timer_picker_countdown", 10);
        interval = Integer.parseInt(sp.getString("timer_default_interval", "60"));
        if (interval == 0) {
            interval = sp.getInt("timer_picker_interval", 60);
        }
        String url = sp.getString("timer_going_selected_ringtone", null);
        if (isGoingRingtoneOn) {
            if (url != null) {
                if (mpGoing != null && mpGoing.isPlaying()) {
                    mpGoing.stop();
                }
                Uri uri = Uri.parse(url);
                mpGoing = MediaPlayer.create(getContext(), uri);
            }
        } else {
            speaker = new Speaker(getActivity());
        }
        if (useSameSound) {
            mpUp = mpGoing;
            isUpRingtoneOn = isGoingRingtoneOn;
            isUpVibrationOn = isGoingVibrationOn;
        } else {
            url = sp.getString("timer_up_selected_ringtone", null);
            if (isUpRingtoneOn) {
                if (url != null) {
                    if (mpUp != null && mpUp.isPlaying()) {
                        mpUp.stop();
                    }
                    Uri uri = Uri.parse(url);
                    mpUp = MediaPlayer.create(getContext(), uri);
                }
            } else {
                speaker = new Speaker(getActivity());
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        speaker.shutdown();
        super.onDestroy();
    }

    private class Timer extends CountDownTimer {

        private Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Log.d("Timer", "Timer finish!");
            if (isUpVibrationOn) {
                vibrator.vibrate(pat, -1);
            }
            if (isUpRingtoneOn) {
                mpUp.seekTo(0);
                mpUp.start();
            } else {
                speaker.speechText("時間です");
            }
            isTimeUp = true;
            timer = null;
            timerText.setText(R.string.timer_finish_time);
            start_stop.setText(R.string.timer_stop);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("Timer", "onTick");
            hh = millisUntilFinished / 1000 / 60 / 60;
            mm = millisUntilFinished / 1000 / 60 % 60;
            ss = millisUntilFinished / 1000 % 60;
            ms = millisUntilFinished % 1000;
            long mss = ms / 100;
            long remainSec = millisUntilFinished / 1000;
            timerText.setText(String.format("%02d:%02d:%02d.%01d", hh, mm, ss, mss));
            if (ttsSwitch.isChecked() && isCountdownOn && remainSec <= countStart && remainSec != 0 && mss == 2) {
                speaker.speechText(String.valueOf(remainSec));
            } else if (ttsSwitch.isChecked() && isGoingRingtoneOn && remainSec > countStart && remainSec % interval == 0 && mss == 0) {
                if (isGoingVibrationOn) {
                    vibrator.vibrate(pat, -1);
                }
                mpGoing.seekTo(0);
                mpGoing.start();
                Log.d("Timer", "Ring");
            } else if (ttsSwitch.isChecked() && !isGoingRingtoneOn && remainSec > countStart && remainSec % interval == 0 && mss == 6) {
                if (isGoingVibrationOn) {
                    vibrator.vibrate(pat, -1);
                }
                if (hh > 0) {
                    speaker.speechText(String.format("残り%d時間,%d分です", hh, mm));
                } else if (mm > 0) {
                    speaker.speechText(String.format("残り%d分%d秒です", mm, ss));
                } else if (ss > 0) {
                    speaker.speechText(String.format("残り%d秒です", ss));
                }
            }
        }
    }

}
