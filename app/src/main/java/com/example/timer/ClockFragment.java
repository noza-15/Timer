package com.example.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClockFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    public ClockFragment() {
        // Required empty public constructor
    }

    SharedPreferences sp;
    MediaPlayer mp;
    AudioManager manager;
    Vibrator vibrator;
    Speaker speaker;
    Timer timer;
    TextClock textClock;
    TextView time;
    TextView info;

    Button button;
    Switch ttsSwitch;
    int m, s, interval;
    long[] pat = {300, 300, 300, 500};
    Boolean isRingtoneOn, isVibrationOn, isCanceled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_clock, container, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        manager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        textClock = (TextClock) view.findViewById(R.id.textClock);

        time = (TextView) view.findViewById(R.id.time);
        info = (TextView) view.findViewById(R.id.stateInfo);
        button = (Button) view.findViewById(R.id.speak);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = DateFormat.format("k時m分 s秒", new Date()).toString();
                if (isRingtoneOn) {
                    if (mp.isPlaying()) {
                        mp.stop();
                    }
                    mp.seekTo(0);
                    mp.start();
                } else {
                    speaker.speechText(date);
                }
                time.setText(date);
            }
        });
        ttsSwitch = (Switch) view.findViewById(R.id.ttsswitch);
        ttsSwitch.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        interval = Integer.parseInt(sp.getString("clock_default_interval", "0"));
        if (interval == 0) {
            interval = Integer.parseInt(sp.getString("clock_default_interval_custom", "0"));
        }
        isRingtoneOn = sp.getBoolean("clock_switch_ringtone", false);
        isVibrationOn = sp.getBoolean("clock_switch_vibration", false);
        String vib;
        if (isVibrationOn) {
            vib = "バイブON/";
        } else {
            vib = "バイブOFF/";
        }
        String url = sp.getString("clock_selected_ringtone", null);
        if (isRingtoneOn) {
            info.setText("通知音/" + vib + interval + "分毎");
            if (url != null) {
                if (mp != null && mp.isPlaying()) {
                    mp.stop();
                }
                Uri uri = Uri.parse(url);
                mp = MediaPlayer.create(getContext(), uri);
            }
        } else {
            info.setText("合成音声/" + vib + interval + "分毎");
            speaker = new Speaker(getActivity());
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        speaker.shutdown();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        isCanceled = isChecked;

        if (isChecked) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    timer = new Timer();
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, 1);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    System.out.println(calendar.getTime());
                    Log.d("Clock", "Timer set");
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d("Clock", "Timer active");
                            m = Calendar.getInstance().get(Calendar.MINUTE);
                            if (m % interval == 0) {
                                timeNotify();
                            }
                        }
                    }, calendar.getTime(), 60000);
                }
            }).start();
            if (manager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                Toast.makeText(getActivity(), "マナーモードに設定されています", Toast.LENGTH_SHORT).show();
            } else if (manager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                Toast.makeText(getActivity(), "音量が0に設定されています", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (timer != null) {
                Log.d("Clock", "Timer canceled");
                timer.cancel();
                timer = null;
            }
        }
    }

    void timeNotify() {
        if (isRingtoneOn) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.seekTo(0);
            mp.start();
        } else {
            speaker.speechText(DateFormat.format("k時m分", new Date()).toString());
        }
        if (isVibrationOn) {
            vibrator.vibrate(pat, -1);
        }
    }

}
