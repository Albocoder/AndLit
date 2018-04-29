package com.andlit.ui.groupView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter
{
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs)
    {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch( position )
        {
            case 0:
                YourGroupsFragment tab1 = new YourGroupsFragment();
                return tab1;
            case 1:
                JoinGroupFragment tab2 = new JoinGroupFragment();
                return tab2;
            case 2:
                CreateGroupFragment tab3 = new CreateGroupFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return mNumOfTabs;
    }
}
