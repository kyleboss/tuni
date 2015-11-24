package tuni.tuni;

/**
 * Created by vasu on 11/19/15.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import android.os.Vibrator;
import android.widget.Toast;

public class MetronomeMainActivity extends WearableActivity {

    private Button start, end, plus, minus;
    private TextView num;
    private int value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button)findViewById(R.id.start);
        end = (Button)findViewById(R.id.end);
        plus = (Button)findViewById(R.id.plus);
        minus = (Button)findViewById(R.id.minus);
        num = (TextView)findViewById(R.id.number);
        value = 0;
        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);;


        num.setText(Integer.toString(value));

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value++;
                num.setText(Integer.toString(value));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value > 0) {
                    value--;
                } else {
                    CharSequence text = "You cannot have a negative bit!";
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                    toast.show();
                }
                num.setText(Integer.toString(value));
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long[] beats = {0, 100, 60000/value};
                vib.vibrate(beats, 0);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.cancel();
            }
        });
    }
}
