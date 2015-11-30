package tuni.tuni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by vasu on 11/19/15.
 */
public class RecordingActivity extends Activity
    implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_main);

        View view = findViewById(R.id.recorder_stop);
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SaveRecordingActivity.class);
        startActivity(intent);
    }
}
