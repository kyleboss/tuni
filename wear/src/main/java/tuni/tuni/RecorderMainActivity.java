package tuni.tuni;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vasu on 11/19/15.
 */
public class RecorderMainActivity {
    //44100Hz is currently the only rate that is guaranteed to work on all devices
//but other rates such as 22050, 16000, and 11025 may work on some devices.

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final String TAG = "WEAR";
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    int BufferElements2Rec = 1024;
    int BytesPerElement = 2;
    boolean isRecording = false;
    Button startRecordingButton;
    Button stopRecordingButton;
    GoogleApiClient mGoogleApiClient;
    AudioRecord recorder;
    private MediaRecorder myRecorder;
    private String nodeId = null;
    private String outputFile = null;
    private int bufferSize;
    private AudioRecord audioRecord;
    private byte[] audioData;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
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


        startRecordingButton = (Button) this.findViewById(R.id.StartRecordingButton);
        stopRecordingButton = (Button) this.findViewById(R.id.StopRecordingButton);

        startRecordingButton.setOnClickListener(this);
        stopRecordingButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == startRecordingButton) {
            startRecording();
        } else if (v == stopRecordingButton) {
            stopRecording();
        }
    }

    private void stopRecording() {
        isRecording = false;
        startRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
    }

    //start the process of recording audio
    private void startRecording() {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
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
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tunerRecording.pcm");
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

        int minBufferSize = AudioRecord.getMinBufferSize(11025,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        short[] audioData = new short[minBufferSize];

        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                11025,
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
        } catch (IOException e) {
        }
        sendMessageToDevice();

        //opening channel
//        Log.d("wear", nodeId);
//        mGoogleApiClient.blockingConnect();
//        ChannelApi.OpenChannelResult result = Wearable.ChannelApi.openChannel(mGoogleApiClient, nodeId, "/recorder").await();
//        Channel channel = result.getChannel();

//sending file
//        channel.sendFile(mGoogleApiClient, Uri.fromFile(file)).await();
//        Log.d("wear", file.toString());
//        Log.d("WEAR", "FILE SENT");
//        channel.close(mGoogleApiClient);
//        Asset asset = Asset.createFromUri(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tunerRecording.pcm"));
        Log.d("wear", "OFF TO PHONE");
//        PutDataMapRequest dataMap = PutDataMapRequest.create("/tuner");
//        dataMap.getDataMap().putAsset("tunerAsset", asset);
//        dataMap.getDataMap().putLong("time", new Date().getTime());
//        PutDataRequest request = dataMap.asPutDataRequest();
//        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
//        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
//            @Override
//            public void onResult(DataApi.DataItemResult dataItemResult) {
//                // something
//            }
//        } );
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void sendMessageToDevice() {
        Log.d(TAG, "sendMessageToDevice");

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(final NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                Log.d(TAG, "sendMessageToDevice: onResult");

                final List<Node> nodes = getConnectedNodesResult.getNodes();
                for (final Node node : nodes) {
                    if (node.isNearby()) {
                        Wearable.ChannelApi.openChannel(mGoogleApiClient, node.getId(), "/path").setResultCallback(new ResultCallback<ChannelApi.OpenChannelResult>() {
                            @Override
                            public void onResult(ChannelApi.OpenChannelResult openChannelResult) {
                                Log.d(TAG, "sendMessageToDevice: onResult: onResult");
                                final Channel channel = openChannelResult.getChannel();
                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tunerRecording.pcm");
                                channel.sendFile(mGoogleApiClient, Uri.fromFile(file)).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        Log.d(TAG, "SENT");
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        });
    }
}
