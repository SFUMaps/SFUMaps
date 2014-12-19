package me.gurinderhans.wifirecorder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.Locale;

/**
 * Created by ghans on 14-12-17.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final String TAG = getClass().getSimpleName();

    private String tableName;
    private Context mContext;

    public SectionsPagerAdapter(FragmentManager fm, Context ctx, String tblName) {
        super(fm);
        mContext = ctx;
        tableName = tblName;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        if (position == 0) return new VisibleAPsFragment().newInstance(tableName);
        else return new RecordedAPsFragment().newInstance(tableName);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.pager_title_visibleaps).toUpperCase(l);
            case 1:
                return mContext.getString(R.string.pager_title_recordedaps).toUpperCase(l);
        }
        return null;
    }

}
