package tuni.tuni;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, ChannelApi.ChannelListener {

    private GoogleApiClient mGoogleApiClient;
    private int count = 0;
    public String TAG = "MAIN MOBILE";
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d(TAG, "CONNECTING:");

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Toast.makeText(getApplicationContext(), "Data changed.",
                Toast.LENGTH_LONG).show();
        Log.d("main", "1");
        for (DataEvent event : dataEvents) {
            Log.d("main", "2");
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/tuner")) {
                // Get the Asset object
                Log.d("main", "3");
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                final Asset asset = dataMapItem.getDataMap().getAsset("tunerAsset");
                Log.d(TAG, "");
                Thread savingThread = new Thread(new Runnable() {
                    public void run() {
                        ConnectionResult result =
                                mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
                        if (!result.isSuccess()) {
                            return;
                        }
                        Log.d("main", "4");
                        // Convert asset into a file descriptor and block until it's ready
                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                mGoogleApiClient, asset).await().getInputStream();
                        mGoogleApiClient.disconnect();
                        if (assetInputStream == null) {
                            return;
                        }

                        // Get folder for output
                        File storage = getApplicationContext().getFilesDir();
                        Log.d(TAG, "Location:" + storage.toString());
                        File dir = new File(storage.getAbsolutePath() + "/tuner/");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        } // Create folder if needed
                        Log.d(TAG, dir.getAbsolutePath());

                        // Read data from the Asset and write it to a file on external storage
                        final File file = new File(dir, "/tunerRecording.pcm");
                        if (file.exists()) {
                            file.delete();
                        }
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, file.getPath());

                        try {
                            FileOutputStream fOut = new FileOutputStream(file);
                            int nRead;
                            byte[] data = new byte[16384];
                            while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                                fOut.write(data, 0, nRead);
                            }

                            fOut.flush();
                            fOut.close();
                        } catch (Exception e) {
                        }

                        // Rescan folder to make it appear
                        try {
                            String[] paths = new String[1];
                            paths[0] = file.getAbsolutePath();
                            MediaScannerConnection.scanFile(context, paths, null, null);
                        } catch (Exception e) {
                        }

                        try {
                            MediaPlayer myPlayer;
                            myPlayer = new MediaPlayer();
                            myPlayer.setDataSource(file.getPath());
                            myPlayer.prepare();
                            myPlayer.start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                savingThread.start();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "CONNECTED");
        Toast.makeText(getApplicationContext(), "Connected.",
                Toast.LENGTH_LONG).show();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onChannelOpened(Channel channel) {
        Log.d("mobile", "onChannelOpened");
        if (channel.getPath().equals("/recorder")) {
            Log.d("mobile", "In recorder");

            File file = new File("/sdcard/tunerRecord.pcm");

            try {
                file.createNewFile();
            } catch (IOException e) {
                //handle error
            }

            channel.receiveFile(mGoogleApiClient, Uri.fromFile(file), false);

            MediaPlayer myPlayer;
            myPlayer = new MediaPlayer();
            try {
                myPlayer.setDataSource(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                myPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            myPlayer.start();
//            Channel.GetInputStreamResult getInputStreamResult = channel.getInputStream(mGoogleApiClient).await();
//            InputStream inputStream = getInputStreamResult.getInputStream();
//
//            writePCMToFile(inputStream);

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("mobile", "Audio File Received");
                    Toast.makeText(MainActivity.this, "Audio file received!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onChannelClosed(Channel channel, int i, int i1) {
        Log.d("main", "onChannelClosed");
    }

    @Override
    public void onInputClosed(Channel channel, int i, int i1) {
        Log.d("main", "onInputClosed");
    }

    @Override
    public void onOutputClosed(Channel channel, int i, int i1) {
        Log.d("main", "onOutputClosed");
    }

    public void writePCMToFile(InputStream inputStream) {
        OutputStream outputStream = null;
        Log.d("mobile", "Writing PCM");
        try {
            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(new File("/sdcard/tunerRecord.pcm"));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.out.println("Done writing PCM to file!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
