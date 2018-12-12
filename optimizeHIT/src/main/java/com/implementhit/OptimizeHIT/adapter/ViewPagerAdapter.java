package com.implementhit.OptimizeHIT.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.MenuActivity;
import com.implementhit.OptimizeHIT.fragments.DashboardFragment;
import com.implementhit.OptimizeHIT.fragments.LibraryFragment;
import com.implementhit.OptimizeHIT.fragments.NotificationsFragment;
import com.implementhit.OptimizeHIT.fragments.OptiQueryFragment;

import java.util.HashMap;

/**
 * Created by victor on 7/22/16.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final int ITEM_COUNT = 4;

    public static final int POSITION_DASHBOARD = 0;
    public static final int POSITION_LIBRARY = 1;
    public static final int POSITION_OPTI_QUERY = 2;
    public static final int POSITION_NOTIFICATIONS = 3;

    private HashMap<Integer, Fragment> fragmentHashMap = new HashMap<>();

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        if (position == POSITION_DASHBOARD) {
            fragment = new DashboardFragment();
        } else if (position == POSITION_LIBRARY) {
            fragment = new LibraryFragment();
        } else if (position == POSITION_OPTI_QUERY) {
            fragment = new OptiQueryFragment();
        } else if (position == POSITION_NOTIFICATIONS) {
            fragment = new NotificationsFragment();
        }

        fragmentHashMap.put(position, fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
//        fragmentHashMap.remove(position);
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    public Fragment getFragment(int position) {
        return fragmentHashMap.get(position);
    }

}
