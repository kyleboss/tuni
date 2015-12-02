package tuni.tuni;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

/**
 * Created by vasu on 12/1/15.
 */
public class TunerArcView extends View {

    protected final float ARCSTROKEWIDTH = (float) 23.55;
    protected final float ARCPADDING = (float) 28.8;

    private float start;
    private float angle;
    private Paint paint = new Paint();

    public TunerArcView(Context context) {
        super(context);

        this.paint.setColor(Color.TRANSPARENT);
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(ARCSTROKEWIDTH);

        start = 90;
        angle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /* Calculate an Rectangle,
         * with some spacing to the edges */
        RectF arcRect = new RectF(ARCPADDING,
                ARCPADDING,
                this.getWidth() - ARCPADDING,
                this.getHeight() - ARCPADDING);

        canvas.drawArc(arcRect, start, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public float getStart() {
        return start;
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setAngle(float newAngle) {
        angle = newAngle;
    }

    public void setStart(float newStart) {
        start = newStart;
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }
}
