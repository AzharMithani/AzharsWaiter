package com.azhar.waiter.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.azhar.waiter.fragments.TableOrdersFragment;
import com.azhar.waiter.model.Table;

import java.util.ArrayList;


// This class is the adapter needed by a ViewPager to iterate between the existing tables.
// Each page of the associated ViewPager will correspond to an instance of TableOrdersFragment.
//
// Class FragmentStatePagerAdapter is used here because FragmentPagerAdapter keeps all the views
// that it loads into memory forever, while FragmentStatePagerAdapter disposes views that fall
// outside the current and traversable views.
//
// If FragmentPagerAdapter was used, the fragments shown in the view pager would be always the same
// even after reloading data from the server once and again.
// ----------------------------------------------------------------------------

public class TablePagerAdapter extends FragmentStatePagerAdapter {

    // Object attributes
    private final ArrayList<Table> tableList;


    // Class constructor
    public TablePagerAdapter(FragmentManager fm, ArrayList<Table> tableList) {
        super(fm);

        this.tableList = tableList;
    }


    // Methods inherited from FragmentStatePagerAdapter:

    // Get the TableOrdersFragment for a given position
    @Override
    public Fragment getItem(int position) {

        Table table = tableList.get(position);
        TableOrdersFragment fragment = TableOrdersFragment.newInstance(table.getOrders(),position);

        return fragment;
    }

    // Get the total count of tables
    @Override
    public int getCount() {
        return tableList.size();
    }

    // Called when the host view is attempting to determine if an item's position has changed.
    // Returning POSITION_NONE means it changed, so the fragment always has to be retrieved again
    // (adds a bit overhead, but ensures the fragment shown is always up to date even if the view pager repopulates)
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    /*
    // Get the page title for a given position
    // (necessary if a PagerTabStrip or a PagerTitleStrip is used inside the ViewPager layout)
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);

        Table table = tableList.get(position);
        return table.getName();
    }
    */

}
