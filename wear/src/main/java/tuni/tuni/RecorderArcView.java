package tuni.tuni;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

/**
 * Created by vasu on 12/3/15.
 */
public class RecorderArcView extends View {

    protected final float ARCSTROKEWIDTH = (float) 23.55;
    protected final float ARCPADDING = (float) 28.8;

    protected float mySecondsPassed = 0;
    protected float mySecondsTotal = 0;

    protected final Paint timerPaint = new Paint();
    public static int BLUE = Color.argb(210, 1, 105, 186);

    public RecorderArcView(Context context) {
        super(context);

        timerPaint.setColor(BLUE);
        timerPaint.setAntiAlias(true);
        timerPaint.setStyle(Paint.Style.STROKE);
        timerPaint.setStrokeWidth(ARCSTROKEWIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Check if timer is already done
        if (mySecondsPassed == mySecondsTotal) {
            Log.i("arc", "DONE!");
        } else {
            float angleAmountSeconds = (mySecondsPassed / mySecondsTotal) * -180;

            /* Calculate an Rectangle,
             * with some spacing to the edges */
            RectF arcRect = new RectF(ARCPADDING,
                    ARCPADDING,
                    this.getWidth() - ARCPADDING,
                    this.getHeight() - ARCPADDING);


            // Draw the Seconds-Arc into that rectangle
            canvas.drawArc(arcRect, 180, angleAmountSeconds, false, timerPaint);
        }
    }

    public void updateSecondsPassed(float someSeconds){
        this.mySecondsPassed = someSeconds;
    }

    public void updateSecondsTotal(float totalSeconds){
        this.mySecondsTotal = totalSeconds;
    }
}
