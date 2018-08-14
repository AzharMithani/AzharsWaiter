package com.azhar.waiter.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.fragments.TableOrdersFragment;
import com.azhar.waiter.fragments.TablePagerFragment;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.utils.Utils;

import static com.azhar.waiter.utils.Utils.MessageType.DIALOG;


// This class represents the activity used to represent & navigate through the order list of all tables.
//
// Implements the following interfaces:
//
// - ViewPager.OnPageChangeListener: in order to do some action when the table pager view changes.
//
// - TableOrdersFragment.TableOrdersFragmentListener: in order to do some action when an order is selected.
// ----------------------------------------------------------------------------

public class TablePagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, TableOrdersFragment.TableOrdersFragmentListener {

    // Class attributes
    public static final String CURRENT_POS_KEY = "currentPos";

    private static final int REQUEST_EDIT_ORDER = 1;
    private static final int REQUEST_ADD_ORDER = 2;


    // Object attributes
    private int currentTableIndex;


    // Methods inherited from AppCompatActivity:

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_pager);

        // Reference to the UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Set the toolbar as the action bar for this activity and show the 'back' button up on it
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the data from the intent passed by the previous activity
        currentTableIndex = getIntent().getIntExtra(CURRENT_POS_KEY,-1);

        if ( currentTableIndex == -1 ) {
            Log.d("TablePagerActivity","ERROR: Missing data provided by the intent!");
            Utils.showMessage(this, getString(R.string.error_missingIntentParams), DIALOG, getString(R.string.error));
            return;
        }

        // Action bar title
        setTitle( RestaurantManager.getTableAtPos(currentTableIndex).getName() );


        // Load the pager fragment

        FragmentManager fm = getFragmentManager();

        // We need to add the fragment only if the activity does not have it yet
        // (if the activity was recreated in the past, it might have the fragment already).
        if ( fm.findFragmentById(R.id.fragment_table_pager) == null ) {

            TablePagerFragment pagerFragment = TablePagerFragment.newInstance(currentTableIndex);

            fm.beginTransaction()
                    .add(R.id.fragment_table_pager,pagerFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean superValue = super.onOptionsItemSelected(item);

        if (item.getItemId() == android.R.id.home) {

            finish();
            return true;
        }

        return superValue;
    }

    // This method is called when another activity called with startActivityForResult() sends response back
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If coming back from the Order Detail Activity
        if (requestCode == REQUEST_EDIT_ORDER) {

            if (resultCode == Activity.RESULT_OK) {

                // Reference to the pager fragment, if it exists
                TablePagerFragment pagerFragment = (TablePagerFragment) getFragmentManager().findFragmentById(R.id.fragment_table_pager);

                if ( pagerFragment == null )
                    return;

                // Get the data returned by the Detail Activity
                int tablePos = data.getIntExtra(OrderDetailActivity.TABLE_POS_KEY, -1);
                int orderPos = data.getIntExtra(OrderDetailActivity.ORDER_POS_KEY, -1);

                if (tablePos == -1 || orderPos == -1)
                    return;

                // Update the Fragment view
                pagerFragment.syncView(tablePos);
            }
        }

        // If coming back from the Dish List Activity
        if (requestCode == REQUEST_ADD_ORDER) {

            if (resultCode == Activity.RESULT_OK) {

                // Reference to the pager fragment, if it exists
                TablePagerFragment pagerFragment = (TablePagerFragment) getFragmentManager().findFragmentById(R.id.fragment_table_pager);

                if ( pagerFragment == null )
                    return;

                // Get the data returned by the Detail Activity
                int tablePos = data.getIntExtra(DishListActivity.TABLE_POS_KEY, -1);

                if (tablePos == -1 )
                    return;

                // Update the Fragment view
                pagerFragment.syncView(tablePos);
            }
        }
    }


    // Methods inherited from the ViewPager.OnPageChangeListener:

    // When a new page is selected, update the action bar title with the new table name
    // and save the new index position
    @Override
    public void onPageSelected(int position) {

        setTitle( RestaurantManager.getTableAtPos(position).getName() );
        currentTableIndex = position;
    }

    // When the current page is scrolled (by the user or programmatically), do nothing
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    // When the scroll state changes (by the user or programmatically), do nothing
    @Override
    public void onPageScrollStateChanged(int state) {
    }


    // Methods inherited from the TableOrdersFragment.TableOrdersFragmentListener interface:

    // What to do when a row in the order list is selected
    // (launch the order detail activity, and wait for some response back)
    @Override
    public void onOrderSelected(int orderPos, int tablePos) {

        Intent intent = new Intent(this, OrderDetailActivity.class);

        intent.putExtra(OrderDetailActivity.ORDER_POS_KEY, orderPos);
        intent.putExtra(OrderDetailActivity.TABLE_POS_KEY, tablePos);

        startActivityForResult(intent, REQUEST_EDIT_ORDER);
    }

    // What to do when a row in the order list is selected
    // (launch the dish list activity to choose a dish, and wait for some response back)
    @Override
    public void onAddOrderClicked(int tablePos) {

        Intent intent = new Intent(this, DishListActivity.class);

        intent.putExtra(DishListActivity.TABLE_POS_KEY, tablePos);

        startActivityForResult(intent, REQUEST_ADD_ORDER);
    }

}
