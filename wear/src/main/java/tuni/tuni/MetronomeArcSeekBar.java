package tuni.tuni;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by vasu on 12/2/15.
 */
public class MetronomeArcSeekBar extends SeekBar {

    protected final float ARCSTROKEWIDTH = (float) 23.55;
    protected final float ARCPADDING = (float) 28.8;
    protected final int LAYOUT_WIDTH = 320;
    protected final int LAYOUT_HEIGHT = 320;

    private Paint mBasePaint;
    private Paint mProgressPaint;
    private RectF mArcRect;
    private int defaultmax = 180;
    private int startAngle = 180;
    private Drawable mThumb;
    private int mTranslateX;
    private int mTranslateY;
    private float mThumbXPos;
    private float mThumbYPos;
    private double mTouchAngle;

    private float mArcRadius = 0;
    private float mProgressSweep = 0;
    private int mStartAngle = 6;
    private int mRotation = 90;
    private int mMax = 100;
    public int mProgress = 0;
    private boolean mClockwise = true;
    private static int INVALID_PROGRESS_VALUE = -1;
    private int mSweepAngle = 168;
    private float mTouchIgnoreRadius;
    private boolean mTouchInside = true;

    private int trackColor = Color.TRANSPARENT;
    private int progressColor=Color.TRANSPARENT;

    public MetronomeArcSeekBar(Context context) {
        super(context);

        mArcRect = new RectF(ARCPADDING,
                ARCPADDING,
                LAYOUT_WIDTH - ARCPADDING,
                LAYOUT_HEIGHT - ARCPADDING);

        mBasePaint = new Paint();
        mBasePaint.setAntiAlias(true);
        mBasePaint.setColor(trackColor);
        mBasePaint.setStrokeWidth(ARCSTROKEWIDTH);
        mBasePaint.setStyle(Paint.Style.STROKE);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStrokeWidth(5);
        mProgressPaint.setStyle(Paint.Style.STROKE);

        mThumb = getResources().getDrawable(R.drawable.seek_arc_control_selector, context.getTheme());

        setMax(defaultmax);// degrees
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mArcRect, startAngle, -1 * getMax(), false, mBasePaint);
        canvas.drawArc(mArcRect, startAngle, -1 * getProgress(), false, mProgressPaint);

        // Draw the thumb nail
        canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
        mThumb.setBounds(0, 0, 29, 29);
        mThumb.draw(canvas);

        invalidate();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final float height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec) - (2 * ARCPADDING);
        final float width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec) - (2 * ARCPADDING);

        final float min = Math.min(width, height);
        float arcDiameter = 0;

        mTranslateX = (int) ((320 - ARCPADDING) * 0.5f);
        mTranslateY = (int) ((320 - ARCPADDING) * 0.5f);

        arcDiameter = min;
        mArcRadius = arcDiameter / 2;
        mArcRect.set(ARCPADDING, ARCPADDING, width + ARCPADDING, height + ARCPADDING);

        //int arcStart = (int)mProgressSweep + mStartAngle  + mRotation + 90;
        int arcStart = 354;
        mThumbXPos = (float) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (float) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

        setTouchInSide(mTouchInside);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);

                break;
        }

        return super.onTouchEvent(event);
    }

    private void updateOnTouch(MotionEvent event) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) {
            return;
        }
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise
        x= (mClockwise) ? x:-x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (mProgress < 0) ? 0 : progress;

        mProgress = progress;
        mProgressSweep = (float) progress / mMax * mSweepAngle;

        updateThumbPosition();

        invalidate();
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbXPos = (float) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
        mThumbYPos = (float) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = mArcRadius
                    - Math.min(thumbHalfWidth, thumbHalfheight);
        }
    }

    public MetronomeArcSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MetronomeArcSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public void setOval(RectF mOval) {
        this.mArcRect = mOval;
    }


    public void setStartAngle(int startAngle) {
        this.startAngle = startAngle;
    }

    public void setTrackColor(int trackColor) {
        this.trackColor = trackColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }
}
