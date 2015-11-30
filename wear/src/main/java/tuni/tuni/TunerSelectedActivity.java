package tuni.tuni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.view.View;

/**
 * Created by vasu on 11/19/15.
 */
public class TunerSelectedActivity extends Activity
    implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner_select);

        View view = findViewById(R.id.tuner_select);
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TuneNoteActivity.class);
        startActivity(intent);
    }
}
