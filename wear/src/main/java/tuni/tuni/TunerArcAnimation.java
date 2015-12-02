package tuni.tuni;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Paint;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by vasu on 12/1/15.
 */
public class TunerArcAnimation extends Animation {

    private TunerArcView tunerArcView;

    private float oldAngle;
    private float newAngle;

    private float oldStart;
    private float newStart;

    public TunerArcAnimation(TunerArcView tunerArcView, float newStart, float newStop) {
        this.oldAngle = tunerArcView.getAngle();
        this.newAngle = newStop - newStart;
        this.oldStart = tunerArcView.getStart();
        this.newStart = newStart;

        this.tunerArcView = tunerArcView;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);
        float start = oldStart + ((newStart - oldStart) * interpolatedTime);

        tunerArcView.setAngle(angle);
        tunerArcView.setStart(start);

        tunerArcView.requestLayout();
    }
}
