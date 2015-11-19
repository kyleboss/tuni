package tuni.tuni;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TuneActivity extends Activity implements OnClickListener {
    RecordAudio recordTask;
    Button startRecordingButton, stopRecordingButton;
    File recordingFile;
    private static final String TAG = TuneActivity.class.getSimpleName();

    boolean isRecording = false;

    private static final int SAMPLE_RATE    = 48000;
    private static final int SAMPLES        = 1024;
    private static final MPM mpm            = new MPM(SAMPLE_RATE, SAMPLES, 0.93);
    private static AudioRecord recorder;
    private static short[] data;
    private static int N;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune);

        startRecordingButton    = (Button) this.findViewById(R.id.StartRecordingButton);
        stopRecordingButton     = (Button) this.findViewById(R.id.StopRecordingButton);

        startRecordingButton.setOnClickListener(this);
        stopRecordingButton.setOnClickListener(this);

        stopRecordingButton.setEnabled(false);
        File path = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/tuniFiles/");
        path.mkdirs();
        try {
            recordingFile = File.createTempFile("recording", ".pcm", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
    }

    public void onClick(View v) {
        if (v == startRecordingButton) {
            record();
        } else if (v == stopRecordingButton) {
            stopRecording();
        }
    }

    public void record() {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void stopRecording() {
        isRecording = false;
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                N           = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                data        = new short[SAMPLES];
                recorder    = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10);
                recorder.startRecording();
                while (isRecording) {
                }
                recorder.stop();
                recorder.release();
                recorder.read(data, 0, data.length);
                double pitch = mpm.getPitchFromShort(data);
                Log.d("wear", "PITCH: " + pitch);
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Void result) {
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
        }
    }
}