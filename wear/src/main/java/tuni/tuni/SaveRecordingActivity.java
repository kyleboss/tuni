package tuni.tuni;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * Created by vasu on 11/19/15.
 */
public class SaveRecordingActivity extends Activity
    implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private ImageButton saveButton;
    private static final String TAG = "WEAR";
    GoogleApiClient mGoogleApiClient;
    private String nodeId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_save);

        saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToDevice();
                finish();
            }
        });


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
    }


    @Override
    protected void onStop() {
        super.onStop();

        // TODO: destroy file on watch?
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

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


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
//   Log.d("wear", "OFF TO PHONE");
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
