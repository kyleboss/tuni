package tuni.tuni;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;

/**
 * Created by vasu on 11/19/15.
 */
public class TunerMainActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    RecordAudio recordTask;
    Button startRecordingButton, stopRecordingButton;
    File recordingFile;
    File file;
    private static final String TAG = TunerMainActivity.class.getSimpleName();

    boolean isRecording = false;

    private static final int SAMPLE_RATE = 48000;
    private static final int SAMPLES = 1024;
    private static final MPM mpm = new MPM(SAMPLE_RATE, SAMPLES, 0.93);
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
        file = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/tuniFiles/");
        file.mkdirs();
        try {
            recordingFile = File.createTempFile("recording", ".pcm", file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
    }

    public void onClick(View v) {
        if (v == startRecordingButton) {
            isRecording = true;
            record();
        } else if (v == stopRecordingButton) {
            stopRecording();
        }
    }

    public void resetRecording() {
        stopRecording();
        record();
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
            if (!isRecording) return null;
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
                        if (i > 10000) resetRecording();
                        recorder.read(data, 0, data.length);
                        pitch = mpm.getPitchFromShort(data);
                        if (pitch > 300) {
                            Log.d("wear", "PITCH: " + pitch);
                            percentDiff = PitchComparison.displayNoteOf(pitch, "E");
                            System.out.println("PERCENTDIFF: " + percentDiff);
                            if (percentDiff < 0) {
                                if (percentDiff != -1.0) {
                                    System.out.println("TIGHTEN STRING");
                                }
                            } else {
                                System.out.println("LOOSEN STRING");
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
            startRecordingButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "CONNECTED");
        Toast.makeText(this, "Connected.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
