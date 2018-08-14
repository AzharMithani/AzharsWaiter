package com.azhar.waiter.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.fragments.DishListFragment;
import com.azhar.waiter.model.Dish;
import com.azhar.waiter.model.Order;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.model.Table;
import com.azhar.waiter.utils.Utils;

import java.util.ArrayList;

import static com.azhar.waiter.utils.Utils.MessageType.DIALOG;


// This class represents the activity where the user can order a dish from all the available dishes.
//
// Implements the following interfaces:
//
// - DishListFragment.OnDishSelectedListener: in order to do some action when a dish is selected.
// ----------------------------------------------------------------------------

public class DishListActivity extends AppCompatActivity implements DishListFragment.OnDishSelectedListener {

    // Class attributes
    public static final String TABLE_POS_KEY = "orderTable";  // The table's position in the restaurant's table list


    // Object attributes
    private Table mTable;
    private int mTablePos;

    // Methods inherited from AppCompatActivity:

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_list);

        // Set the toolbar as the action bar for this activity and show the 'back' button up on it
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the data from the intent passed by the previous activity
        mTablePos = getIntent().getIntExtra(TABLE_POS_KEY,-1);

        if ( mTablePos == -1 ) {
            Log.d("DishListActivity","ERROR: Missing data provided by the intent!");
            Utils.showMessage(this, getString(R.string.error_missingIntentParams), DIALOG, getString(R.string.error));
            return;
        }

        // Get a reference to the Table object
        mTable = RestaurantManager.getTableAtPos(mTablePos);

        if ( mTable == null ) {
            Log.d("OrderDetailActivity","ERROR: Wrong table index! (" + mTablePos + ")");
            Utils.showMessage(this, getString(R.string.error_wrongTableIndex) + " (" + mTablePos + ")", DIALOG, getString(R.string.error));
            return;
        }

        // Action bar title
        setTitle( getString(R.string.activity_title_dishList) );


        // Load the dish list fragment

        FragmentManager fm = getFragmentManager();

        // We need to add the fragment only if the activity does not have it yet
        // (if the activity was recreated in the past, it might have the fragment already).
        if ( fm.findFragmentById(R.id.fragment_dish_list) == null ) {

            ArrayList<Dish> dishList = RestaurantManager.getDishes();
            String currency = RestaurantManager.getCurrency();

            DishListFragment fragment = DishListFragment.newInstance(dishList,currency);

            fm.beginTransaction()
                    .add(R.id.fragment_dish_list,fragment)
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


    // Methods inherited from the DishListFragment.OnDishSelectedListener interface:

    // This method indicates what to do when an item in the dish list is selected
    public void onDishSelected(int position, Dish dish, View view) {

        // Add the selected dish to the table orders
        String initialNote = "";
        mTable.addOrder( new Order(dish,initialNote) );

        // Notify the previous activity that a new order was added to the table
        int resultCode = RESULT_OK;
        Intent returnIntent = new Intent();
        returnIntent.putExtra(TABLE_POS_KEY, mTablePos);
        setResult(resultCode, returnIntent);
        finish();
    }

}
