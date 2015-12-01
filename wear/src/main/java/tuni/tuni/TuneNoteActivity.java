package tuni.tuni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by vasu on 11/19/15.
 */
public class TuneNoteActivity extends Activity {

    public static String NOTE = "note";
    public static String A = "a";
    public static String B = "b";
    public static String D = "d";
    public static String E_HIGH = "e_high";
    public static String E_LOW = "e_low";
    public static String G = "g";

    private String tuning_note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_note);
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
    }
}
