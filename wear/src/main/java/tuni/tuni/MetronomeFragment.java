package tuni.tuni;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by vasu on 11/19/15.
 */
public class MetronomeFragment extends Fragment {

    private TextView bpm;
    private static String FONT = "Helvetica-Condensed.otf";
    private Vibrator vib;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_metronome, container, false);

        bpm = (TextView) view.findViewById(R.id.bpm);
        bpm.setTypeface(FontCache.get(FONT, getActivity().getApplicationContext()));

        MetronomeArcSeekBar metronomeArcSeekBar = new MetronomeArcSeekBar(getActivity().getApplicationContext());
        ((ViewGroup) view).addView(metronomeArcSeekBar, container.getLayoutParams());

        metronomeArcSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentProgress = progress / 180f;
                int beats = (int) (percentProgress * 220);
                bpm.setText(String.valueOf(beats));

                int sleepTime = 60000 / beats;
                long[] pattern = {0, 100, sleepTime};
                vib.vibrate(pattern, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);;

    }

    @Override
    public void onStop() {
        super.onStop();

        vib.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();

        vib.cancel();
    }
}
