package tuni.tuni;

import android.content.Context;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vasu on 11/19/15.
 */
public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private ArrayList<SimpleRow> mPages;

    public SampleGridPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        initPages();
    }

    private void initPages() {
        mPages = new ArrayList<SimpleRow>();

        SimpleRow row1 = new SimpleRow();
        row1.addPages(new SimplePage("HomeFragment"));

        SimpleRow row2 = new SimpleRow();
        row2.addPages(new SimplePage("MenuTunerFragment"));
        row2.addPages(new SimplePage("TuneFragment"));

        SimpleRow row3 = new SimpleRow();
        row3.addPages(new SimplePage("MenuMetronomeFragment"));
        row3.addPages(new SimplePage("MetronomeFragment"));

        SimpleRow row4 = new SimpleRow();
        row4.addPages(new SimplePage("MenuRecorderFragment"));
        row4.addPages(new SimplePage("RecorderFragment"));

        mPages.add(row1);
        mPages.add(row2);
        mPages.add(row3);
        mPages.add(row4);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        SimplePage page = mPages.get(row).getPages(col);

        try {
            Class<?> fragmentClass = Class.forName("tuni.tuni." + page.mTitle);
            return (Fragment) fragmentClass.newInstance();
        } catch (ClassNotFoundException e) {
            Log.e("getFragment", "fragment class not found");
            e.printStackTrace();
        } catch (InstantiationException e) {
            Log.e("getFragment", "cannot instantiate fragment class");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("getFragment", "illegal access");
            e.printStackTrace();
        }

        return new BlankFragment();
    }


    @Override
    public int getRowCount() {
        return mPages.size();
    }

    @Override
    public int getColumnCount(int row) {
        return mPages.get(row).size();
    }
}