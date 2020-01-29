package com.example.timer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;


/**
 * A simple {@link Fragment} subclass.
 */
public class StopWatchFragment extends Fragment {


    public StopWatchFragment() {
        // Required empty public constructor
    }

    Chronometer chronometer;
    Button start;
    Button stop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        start = view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.start();
            }
        });
        stop = view.findViewById(R.id.timer_key_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.stop();
            }
        });
        chronometer = view.findViewById(R.id.chronometer);
        return view;
    }

}
