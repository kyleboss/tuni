package tuni.tuni;

import android.app.Fragment;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vasu on 11/19/15.
 */
public class RecorderFragment extends Fragment
    implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks  {

//44100Hz is currently the only rate that is guaranteed to work on all devices
//but other rates such as 22050, 16000, and 11025 may work on some devices.

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final String TAG = "WEAR";
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    int BufferElements2Rec = 1024;
    int BytesPerElement = 2;
    boolean isRecording = false;
    AudioRecord recorder;
    private MediaRecorder myRecorder;
    private String outputFile = null;
    private int bufferSize;
    private AudioRecord audioRecord;
    private byte[] audioData;
    GoogleApiClient mGoogleApiClient;
    private String nodeId = null;


    private TextView textView;
    private ImageButton recordCircle;
    private ImageButton recordSquare;
    private static String FONT = "Helvetica-Condensed.otf";
    private static String RECORD = "Record";
    private static String RECORDING = "Recording...";
    private static String SAVED= "Saved to Your Phone!";

    protected static final int DEFAULTSECONDS = 60;
    /* The value of these IDs is random!
     * they are just needed to be recognized */
    protected static final int SECONDPASSEDIDENTIFIER = 0x1337;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    private static int UPDATE_INTERVAL = 100;

    /** is the countdown running at the moment ?*/
    protected boolean running = false;
    /** Seconds passed so far */
    protected float mySecondsPassed = 0;
    /** Seconds to be passed totally */
    protected float mySecondsTotal = DEFAULTSECONDS;

    /* Thread that sends a message
     * to the handler every second */
    Thread myRefreshThread = null;
    // One View is all that we see.
    RecorderArcView myRecorderView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_recorder, container, false);

        textView = (TextView) view.findViewById(R.id.textView);
        textView.setTypeface(FontCache.get(FONT, getActivity().getApplicationContext()));

        recordCircle = (ImageButton) view.findViewById(R.id.recordCircleButton);
        recordSquare = (ImageButton) view.findViewById(R.id.recordSquareButton);

        recordCircle.setOnClickListener(this);
        recordSquare.setOnClickListener(this);

        this.myRecorderView = new RecorderArcView(getActivity().getApplicationContext());
        this.myRecorderView.updateSecondsTotal(DEFAULTSECONDS);
        ((ViewGroup) view).addView(myRecorderView);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recordCircleButton) {
            recordCircle.setVisibility(View.INVISIBLE);
            recordSquare.setVisibility(View.VISIBLE);
            textView.setText(RECORDING);

            startArcTimer();
        }

        if (v.getId() == R.id.recordSquareButton) {
            recordSquare.setVisibility(View.INVISIBLE);
            recordCircle.setVisibility(View.VISIBLE);
            textView.setText(RECORD);

            stopArcTimer();
        }
    }

    private void startArcTimer() {
        running = true;
        this.myRefreshThread = new Thread(new secondCountDownRunner());
        this.myRefreshThread.start();

        startRecording();
    }

    private void stopArcTimer() {
        running = false;
        stopRecording();

        this.myRefreshThread.interrupt();

        recordSquare.setVisibility(View.INVISIBLE);
        recordCircle.setVisibility(View.VISIBLE);

        Toast.makeText(getActivity().getApplicationContext(), SAVED, Toast.LENGTH_LONG).show();
        resetTimer();
    }

    public void resetTimer() {
        textView.setText(RECORD);
        mySecondsPassed = 0f;
        myRecorderView.updateSecondsPassed(mySecondsPassed);
        myRecorderView.invalidate();
    }

    @Override
    public void onResume() {
        resetTimer();

        super.onResume();
    }


    private static class ArcTimerHandler extends Handler {
        private final WeakReference<RecorderFragment> mFragment;

        public ArcTimerHandler(RecorderFragment fragment) {
            mFragment = new WeakReference<RecorderFragment>(fragment);
        }

        /** Gets called on every message that is received */
        @Override
        public void handleMessage(Message msg) {
            RecorderFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case RecorderFragment.SECONDPASSEDIDENTIFIER:
                        // We identified the Message by its What-ID
                        if (fragment.running) {
                            // One second has passed
                            fragment.mySecondsPassed = fragment.mySecondsPassed + (UPDATE_INTERVAL / 1000f);
                            if(fragment.mySecondsPassed > fragment.mySecondsTotal){
                                Log.i("arc", "stopping timer");
                                fragment.stopArcTimer();
                            }
                        }
                        // No break here --> runs into the next case
                    case RecorderFragment.GUIUPDATEIDENTIFIER:
                        // Redraw our Pizza !!
                        fragment.myRecorderView.updateSecondsPassed(fragment.mySecondsPassed);
                        fragment.myRecorderView.updateSecondsTotal(fragment.mySecondsTotal);
                        fragment.myRecorderView.invalidate();
                        break;
                }
                super.handleMessage(msg);
            }
        }
    }

    /* The Handler that receives the messages
                 * sent out by myRefreshThread every second */
    final Handler myPizzaViewUpdateHandler = new ArcTimerHandler(this);

    class secondCountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                Message m = new Message();
                m.what = RecorderFragment.SECONDPASSEDIDENTIFIER;
                myPizzaViewUpdateHandler.sendMessage(m);
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d("WEAR", "ConnectionCallback onConnected");
                        Thread recordingThread = new Thread(new Runnable() {
                            public void run() {
                                Log.d("wear", "opening new thrad for nodes");
                                updateNodeId();
                            }
                        }, "AudioRecorder Thread");
                        recordingThread.start();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("WEAR", "ConnectionCallback onConnectionSuspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("WEAR", "ConnectionCallback onConnectionFailed");
                        Log.d("WEAR", connectionResult.toString());
                        //TODO do something on connection failed
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    private void updateNodeId() {
        Log.d("WEAR", "Getting nodes");
        HashSet<Node> results = new HashSet<Node>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node);
        }
        Log.d("WEAR", results.toString());
        nodeId = pickBestNodeId(results);
    }

    private String pickBestNodeId(HashSet<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    private void stopRecording() {
        isRecording = false;
    }

    //start the process of recording audio
    private void startRecording() {
        isRecording = true;

        Log.d("wear", "Starting recording");
        Thread recordingThread = new Thread(new Runnable() {
            public void run() {
                Log.d("wear", "opening new thrad");
                writeAudioDataToPhone();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToPhone() {
        Log.d("wear", "in writeAudioDatatoPhone");
        short sData[] = new short[BufferElements2Rec];
        if (nodeId == null) {
            Log.d("wear", "LEAVING CUS NULL");
            return;
        }
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/tunerRecording.pcm");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

        int minBufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        short[] audioData = new short[minBufferSize];

        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize);

        audioRecord.startRecording();

        while (isRecording) {
            int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
            for (int i = 0; i < numberOfShort; i++) {
                try {
                    dataOutputStream.writeShort(audioData[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("wear", "stopped recording");
        audioRecord.stop();
        try {
            dataOutputStream.close();
            byte[] byteArrayOfSound = read(file);
            PutDataMapRequest dataMap = PutDataMapRequest.create("/recording");
            Asset asset = Asset.createFromBytes(byteArrayOfSound);
            dataMap.getDataMap().putAsset("recording", asset);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);
            Log.d(TAG, "SENT TO PHONE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public byte[] read(File file) throws IOException {

        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                throw new IOException(
                        "EOF reached while trying to read the whole file");
            }
        } finally {
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return buffer;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "CONNCECTED");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
