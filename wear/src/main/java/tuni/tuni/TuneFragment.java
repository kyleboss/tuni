package tuni.tuni;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by vasu on 11/19/15.
 */
public class TuneFragment extends Fragment
    implements View.OnTouchListener {

    private Bitmap aBitmap;
    private Bitmap bBitmap;
    private Bitmap dBitmap;
    private Bitmap eHighBitmap;
    private Bitmap eLowBitmap;
    private Bitmap gBitmap;

    private ImageButton aButton;
    private ImageButton bButton;
    private ImageButton dButton;
    private ImageButton eHighButton;
    private ImageButton eLowButton;
    private ImageButton gButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_tuner_main, container, false);
        view.setOnTouchListener(this);

        aBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        bBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b);
        dBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.d);
        eHighBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.e_high);
        eLowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.e_low);
        gBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.g);

        aButton = (ImageButton) view.findViewById(R.id.aButton);
        bButton = (ImageButton) view.findViewById(R.id.bButton);
        dButton = (ImageButton) view.findViewById(R.id.dButton);
        eHighButton = (ImageButton) view.findViewById(R.id.eHighButton);
        eLowButton = (ImageButton) view.findViewById(R.id.eLowButton);
        gButton = (ImageButton) view.findViewById(R.id.gButton);

        aButton.setOnTouchListener(this);
        bButton.setOnTouchListener(this);
        dButton.setOnTouchListener(this);
        eHighButton.setOnTouchListener(this);
        eLowButton.setOnTouchListener(this);
        gButton.setOnTouchListener(this);

        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // MotionEvent object holds X-Y values
        int iX = (int) event.getX();
        int iY = (int) event.getY();

        Intent intent = new Intent(getActivity(), TuneNoteActivity.class);
        try {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (iX < aBitmap.getWidth() && iY < aBitmap.getHeight() && aBitmap.getPixel(iX, iY) != 0) {
                    aButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.A);
                    startActivity(intent);
                    return true;
                } else if (iX < bBitmap.getWidth() && iY < bBitmap.getHeight() && bBitmap.getPixel(iX, iY) != 0) {
                    bButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.B);
                    startActivity(intent);
                    return true;
                } else if (iX < dBitmap.getWidth() && iY < dBitmap.getHeight() && dBitmap.getPixel(iX, iY) != 0) {
                    dButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.D);
                    startActivity(intent);
                    return true;
                } else if (iX < eHighBitmap.getWidth() && iY < eHighBitmap.getHeight() && eHighBitmap.getPixel(iX, iY) != 0) {
                    eHighButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.E_HIGH);
                    startActivity(intent);
                    return true;
                } else if (iX < eLowBitmap.getWidth() && iY < eLowBitmap.getHeight() && eLowBitmap.getPixel(iX, iY) != 0) {
                    eLowButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.E_LOW);
                    startActivity(intent);
                    return true;
                } else if (iX < gBitmap.getWidth() && iY < gBitmap.getHeight() && gBitmap.getPixel(iX, iY) != 0) {
                    gButton.setVisibility(View.VISIBLE);
                    intent.putExtra(TuneNoteActivity.NOTE, TuneNoteActivity.G);
                    startActivity(intent);
                    return true;
                }
            }
        } catch (Exception e) {

        }

        if(event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            aButton.setVisibility(View.INVISIBLE);
            bButton.setVisibility(View.INVISIBLE);
            dButton.setVisibility(View.INVISIBLE);
            eHighButton.setVisibility(View.INVISIBLE);
            eLowButton.setVisibility(View.INVISIBLE);
            gButton.setVisibility(View.INVISIBLE);
        }

        return getActivity().onTouchEvent(event);
    }
}
