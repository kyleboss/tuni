package tuni.tuni;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vasu on 11/19/15.
 */
public class TuneNoteActivity extends Activity {

    // fields for FFT
    RecordAudio recordTask;
    File recordingFile;
    File file;

    boolean isRecording = false;

    private static final int SAMPLE_RATE = 48000;
    private static final int SAMPLES = 1024;
    private static final MPM mpm = new MPM(SAMPLE_RATE, SAMPLES, 0.93);
    private static AudioRecord recorder;
    private static short[] data;
    private static int N;

    private static String TIGHTEN_STRING = "tighten_string";
    private static String LOOSEN_STRING = "loosen_string";
    private static String PERFECT = "perfect";

    // fields for UI
    public static String NOTE = "note";
    public static String A = "a";
    public static String B = "b";
    public static String D = "d";
    public static String E_HIGH = "e_high";
    public static String E_LOW = "e_low";
    public static String G = "g";

    private String tuning_note;

    private static String FONT = "Helvetica-Condensed.otf";
    private TextView pluckText;
    private TextView tightenText;
    private TextView loosenText;
    private TextView perfectText;
    private ImageView indicators;
    private TunerArcView tunerArcView;

    private static int ANIMATION_TIME = 1500;
    public static int BLUE = Color.argb(210, 1, 105, 186);
    public static int RED = Color.argb(220, 170, 0, 0);
    public static int ORANGE = Color.argb(220, 255, 130, 20);
    public static int GREEN = Color.argb(210, 3, 157, 54);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_note);

        file = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/tuniFiles/");
        file.mkdirs();
        try {
            recordingFile = File.createTempFile("recording", ".pcm", file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }

        pluckText = (TextView) findViewById(R.id.pluckText);
        tightenText = (TextView) findViewById(R.id.tightenText);
        loosenText = (TextView) findViewById(R.id.loosenText);
        perfectText = (TextView) findViewById(R.id.perfectText);

        pluckText.setTypeface(FontCache.get(FONT, this));
        tightenText.setTypeface(FontCache.get(FONT, this));
        loosenText.setTypeface(FontCache.get(FONT, this));
        perfectText.setTypeface(FontCache.get(FONT, this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        tuning_note = extras.getString(NOTE);

        View view = findViewById(R.id.tune_note);
        view.setBackground(getResources().getDrawable(
                getResources().getIdentifier("tuning_" + tuning_note, "drawable", getPackageName()),
                getTheme()));

        indicators = new ImageView(this);
        indicators.setImageResource(R.drawable.indicators);
        addContentView(indicators, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        drawArc();
    }

    private void drawArc() {
        tunerArcView = new TunerArcView(this);
        addContentView(tunerArcView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        indicators.bringToFront();

    }

    private void animateArc(final float newStart, final float newStop, final int newColor) {
        TunerArcAnimation animation = new TunerArcAnimation(tunerArcView, newStart, newStop);
        animation.setDuration(ANIMATION_TIME);

        ArgbEvaluator evaluator = new ArgbEvaluator();
        ObjectAnimator animator;
        animator = ObjectAnimator.ofObject(tunerArcView, "color", evaluator,
                tunerArcView.getColor(), newColor);
        animator.setDuration(ANIMATION_TIME);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (newColor == GREEN) {
                    animateArc(0, 180, BLUE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tunerArcView.startAnimation(animation);
        animator.start();
    }

    private void animateArc(int newColor) {
        ArgbEvaluator evaluator = new ArgbEvaluator();
        ObjectAnimator animator = ObjectAnimator.ofObject(tunerArcView, "color", evaluator,
                tunerArcView.getColor(), newColor);
        animator.setDuration(ANIMATION_TIME).start();
    }

    /*
        Converts from [0.5, 1.5] to [180, 0].
     */
    private float convertToCenter(float noteOffset) {
        if (noteOffset < 0.5) {
            return 180;
        }
        if (noteOffset > 1.5) {
            return 0;
        }

        float rangePercent = (float) (noteOffset - 0.5);
        return 180 - (rangePercent * 180);
    }

    private float convertToStart(float noteOffset) {
        float start = convertToCenter(noteOffset) - 10;

        if (start < 0) {
            return 0;
        }
        if (start + 20 > 180) {
            return 160;
        }

        return start;
    }

    private float convertToStop(float noteOffset) {
        float stop = convertToCenter(noteOffset) + 10;

        if (stop > 180) {
            return 180;
        }
        if (stop - 20 < 0) {
            return 20;
        }

        return stop;
    }

    private void updatePluckText() {
        tightenText.setVisibility(View.INVISIBLE);
        loosenText.setVisibility(View.INVISIBLE);
        perfectText.setVisibility(View.INVISIBLE);
        pluckText.setVisibility(View.VISIBLE);
    }

    private void updateTightenText() {
        loosenText.setVisibility(View.INVISIBLE);
        perfectText.setVisibility(View.INVISIBLE);
        pluckText.setVisibility(View.INVISIBLE);
        tightenText.setVisibility(View.VISIBLE);
    }

    private void updateLoosenText() {
        tightenText.setVisibility(View.INVISIBLE);
        perfectText.setVisibility(View.INVISIBLE);
        pluckText.setVisibility(View.INVISIBLE);
        loosenText.setVisibility(View.VISIBLE);
    }

    private void updatePerfectText() {
        tightenText.setVisibility(View.INVISIBLE);
        loosenText.setVisibility(View.INVISIBLE);
        pluckText.setVisibility(View.INVISIBLE);
        perfectText.setVisibility(View.VISIBLE);
    }

    /**
     * FUNCTIONS FOR FFT
     **/

    public void resetRecording() {
        stopRecording();
        record();
    }

    public void record() {
        isRecording = true;
        recordTask = new RecordAudio();
        recordTask.execute();
    }

    public void stopRecording() {
        isRecording = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onResume() {
        super.onResume();

        record();
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (!isRecording) {
                return null;
            }

            try {
                int i = 0;
                N = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                data = new short[SAMPLES];
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10);
                recorder.startRecording();

                double pitch = -1;
                double percentDiff = 0.0;
                while (isRecording) {
                    try {
                        i++;
                        if (i > 10000) {
                            resetRecording();
                        }

                        recorder.read(data, 0, data.length);
                        pitch = mpm.getPitchFromShort(data);
                        if (pitch > 300) {
                            Log.d("wear", "PITCH: " + pitch);
                            percentDiff = PitchComparison.displayNoteOf(pitch, tuning_note.substring(0, 1).toUpperCase());
                            System.out.println("PERCENTDIFF: " + percentDiff);

                            if (percentDiff == -1.0) {
                                animateArc(Color.TRANSPARENT);
                                updatePluckText();
                            }

                            /*
                                some assumptions about percentDiff - if changed, update convertToCenter
                                between 0.0 and 0.95, too flat
                                between 0.95 and 1.05, considered good enough?
                                between 1.05 and 2.0, too sharp
                             */
                            if (percentDiff >= 0.0 && percentDiff < 0.95) {
                                animateArc(convertToStart((float) percentDiff),
                                        convertToStop((float) percentDiff),
                                        RED);
                                updateTightenText();

                                Log.d("wear", TIGHTEN_STRING);

                            } else if (percentDiff >= 0.95 && percentDiff < 1.05) {
                                animateArc(convertToStart((float) percentDiff),
                                        convertToStop((float) percentDiff),
                                        GREEN);
                                updatePerfectText();

                                Log.d("wear", PERFECT);

                            } else if (percentDiff >= 1.05 && percentDiff < 2.0) {
                                animateArc(convertToStart((float) percentDiff),
                                        convertToStop((float) percentDiff),
                                        ORANGE);
                                updateLoosenText();

                                Log.d("wear", LOOSEN_STRING);

                            }
                        }
                    } catch (Throwable t) {
                        Log.e("AudioRecord", "Recording Failed");
                    }
                }
                recorder.stop();
                recorder.release();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Void result) {
        }
    }
}
