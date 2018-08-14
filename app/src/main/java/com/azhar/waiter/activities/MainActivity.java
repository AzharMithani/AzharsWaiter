package com.azhar.waiter.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskHandler;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskListener;
import com.azhar.waiter.backgroundtasks.DownloadAvailableDishesTask;
import com.azhar.waiter.fragments.TableListFragment;
import com.azhar.waiter.fragments.TableOrdersFragment;
import com.azhar.waiter.fragments.TablePagerFragment;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.utils.ImageCache;
import com.azhar.waiter.utils.Utils;

import static com.azhar.waiter.utils.Utils.MessageType.DIALOG;
import static com.azhar.waiter.utils.Utils.MessageType.SNACK;


// This class represents the main activity of the app, used to represent either the table list
// or both the table list and the table detail view (depending on the device screen and orientation).
//
// Implements the following interfaces:
//
// - BackgroundTaskListener: in order to throw tasks in background using a BackgroundTaskHandler.
//
// - ViewPager.OnPageChangeListener: in order to do some action when the table pager view changes.
//
// - TableListFragment.OnTableSelectedListener: in order to do some action when a table is selected.
//
// - TableOrdersFragment.TableOrdersFragmentListener: in order to do some action when an order is selected.
// ----------------------------------------------------------------------------

public class MainActivity extends AppCompatActivity implements BackgroundTaskListener, ViewPager.OnPageChangeListener, TableListFragment.OnTableSelectedListener, TableOrdersFragment.TableOrdersFragmentListener {

    // Class attributes
    private static final int REQUEST_EDIT_ORDER = 1;
    private static final int REQUEST_ADD_ORDER = 2;
    private static final int REQUEST_SHOW_PAGER = 3;
    private static final int REQUEST_SHOW_SETTINGS = 4;

    // Object attributes
    private String PREFS_SERVER_URL_KEY;
    private String PREFS_RANDOM_DATA_KEY;
    private String DEFAULT_SERVER_URL;

    //TableListFragment tableListFragment;
    //TablePagerFragment tablePagerFragment;
    int currentTableIndex;


    // Methods inherited from AppCompatActivity:

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // String constants
        PREFS_SERVER_URL_KEY = getString(R.string.prefs_urlKey);
        PREFS_RANDOM_DATA_KEY = getString(R.string.prefs_randomDataKey);
        DEFAULT_SERVER_URL = getString(R.string.default_url);

        // Reference to the UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Set icon for the toolbar and set it as the toolbar for this activity
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        // If we did not download the data from the server yet, try it now
        if ( !RestaurantManager.isSingletonReady() )
            startDataDownloadInBackground();

        // If we already downloaded the data from the server, go load the fragments
        else
            loadActivityFragments(currentTableIndex);
    }

    // Action bar menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    // What to do when an action bar menu option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean superReturn = super.onOptionsItemSelected(item);

        // Reload data from server
        if (item.getItemId() == R.id.menu_reloadFromServer) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle( getString(R.string.msg_downloadDialog_title) )
                    .setMessage( getString(R.string.msg_downloadDialog_question) )
                    .setCancelable(false)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            removeActivityFragments();
                            ImageCache.clear();
                            startDataDownloadInBackground();
                        }
                    })
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        // Go to the settings page
        else if (item.getItemId() == R.id.menu_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SHOW_SETTINGS);
            return true;
        }

        return superReturn;
    }

    // This method is called when another activity called with startActivityForResult() sends response back
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If coming back from the Pager Activity
        // (no need to get data returned, just update the table list in case something changed)
        if (requestCode == REQUEST_SHOW_PAGER) {

            // Try to refresh the table list fragment, if it exists (it always should)
            TableListFragment listFragment = (TableListFragment) getFragmentManager().findFragmentById(R.id.fragment_table_list);

            if ( listFragment != null ) {
                listFragment.syncView();
            }
        }

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

                // Try to refresh the pager fragment, if it exists
                TablePagerFragment pagerFragment = (TablePagerFragment) getFragmentManager().findFragmentById(R.id.fragment_table_pager);

                if ( pagerFragment != null ) {

                    // Get the data returned by the Dish List Activity
                    int tablePos = data.getIntExtra(DishListActivity.TABLE_POS_KEY, -1);

                    if (tablePos == -1)
                        return;

                    // Update the Fragment view
                    pagerFragment.syncView(tablePos);
                }

                // Try to refresh the table list fragment, if it exists (it always should)
                TableListFragment listFragment = (TableListFragment) getFragmentManager().findFragmentById(R.id.fragment_table_list);

                if ( listFragment != null ) {
                    listFragment.syncView();
                }

            }
        }

        // If coming back from the Settings Activity
        if (requestCode == REQUEST_SHOW_SETTINGS) {

            if (resultCode == Activity.RESULT_OK)
                Utils.showMessage(this, getString(R.string.msg_savedSettings), SNACK, null);
        }
    }


    // Methods inherited from the TableListFragment.OnTableSelectedListener interface:

    // This method indicates what to do when a row in the table list is selected
    @Override
    public void onTableSelected(int pos) {

        TablePagerFragment pagerFragment = null;

        // Check if there is room for the table pager fragment right now
        if (findViewById(R.id.fragment_table_pager) != null)
            pagerFragment = (TablePagerFragment) getFragmentManager().findFragmentById(R.id.fragment_table_pager);

        // If the activity already had a TablePager fragment, just update it with the selected table
        if ( pagerFragment != null ) {
            pagerFragment.movePagerToPosition(pos);
        }

        // If the activity did not have a TablePager fragment, then call to a TablePagerActivity
        else {

            Intent intent = new Intent(this, TablePagerActivity.class);
            intent.putExtra(TablePagerActivity.CURRENT_POS_KEY, pos);
            startActivityForResult(intent, REQUEST_SHOW_PAGER);
        }
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

    // What to do when user clicks the button to add a new order to the current table
    // (launch the dish list activity to choose a dish, and wait for some response back)
    @Override
    public void onAddOrderClicked(int tablePos) {

        Intent intent = new Intent(this, DishListActivity.class);

        intent.putExtra(DishListActivity.TABLE_POS_KEY, tablePos);

        startActivityForResult(intent, REQUEST_ADD_ORDER);
    }


    // Methods inherited from the ViewPager.OnPageChangeListener:

    // When a new page is selected, update the action bar title with the new table name
    // and save the new index position
    @Override
    public void onPageSelected(int position) {

        // Change the activity title to the current table of the view pager,
        // only if the TablePagerFragment is being shown
        // (this "if" is necessary because onPageSelected is also called when the activity layout changes,
        // e.g. from 2 fragments to 1 fragment, and we do not want to set a table name as title in that case)
        if (findViewById(R.id.fragment_table_pager) != null) {

            String tableName = RestaurantManager.getTableAtPos(position).getName();
            setTitle(tableName);

            currentTableIndex = position;
        }

    }

    // When the current page is scrolled (by the user or programmatically), do nothing
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    // When the scroll state changes (by the user or programmatically), do nothing
    @Override
    public void onPageScrollStateChanged(int state) {
    }


    // Methods inherited from the BackgroundTaskListener interface:

    // This method is called when a background task finishes
    public void onBackgroundTaskFinished(BackgroundTaskHandler taskHandler) {

        // Determine if the task was the download of the available dishes
        if ( taskHandler.getTaskId() == DownloadAvailableDishesTask.taskId ) {

            // If there were problems, show error message and finish
            if ( taskHandler.hasFailed() ) {
                Log.d("MainActivity","ERROR: The data download has failed!");
                Utils.showMessage(this, getString(R.string.downloadMenu_errorMsg), DIALOG, getString(R.string.error));
                return;
            }

            // If everything went OK, log the data contained in the RestaurantManager
            Log.d("MainActivity",RestaurantManager.contentToString());
            Utils.showMessage(this, getString(R.string.downloadMenu_successMsg_head) + " " + RestaurantManager.dishCount() + " " + getString(R.string.downloadMenu_successMsg_tail), SNACK, null);


            // Now we can proceed to load the fragment(s) contained in the activity
            // (in case the pager fragment is shown, it will show table at position 0)
            loadActivityFragments(0);
        }
    }


    // Auxiliary methods:

    // Launches a background task to download the menu data from the server
    public void startDataDownloadInBackground() {

        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setTitle( getString(R.string.downloadMenu_progressTitle) );
        pDialog.setMessage( getString(R.string.downloadMenu_progressMsg) );
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);

        String tablePrefix = getString(R.string.tablePrefix);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String urlString = prefs.getString(PREFS_SERVER_URL_KEY, DEFAULT_SERVER_URL);
        boolean randomData =  prefs.getBoolean(PREFS_RANDOM_DATA_KEY, false);

        DownloadAvailableDishesTask downloadDishes = new DownloadAvailableDishesTask(getApplicationContext(),urlString,tablePrefix,randomData);
        new BackgroundTaskHandler(downloadDishes,this,pDialog).execute();
    }

    // This method is called to manually load the fragments of the activity
    // (the tablePos argument is used only in case the Table Pager fragment is loaded)
    private void loadActivityFragments(int tablePos) {

        // In case this method was called before loading the remote data, do nothing
        if ( !RestaurantManager.isSingletonReady() )
            return;

        FragmentManager fm = getFragmentManager();

        // Make sure there is space to load the table list (this should be always true)
        if ( findViewById(R.id.fragment_table_list) != null) {

            // We need to add the fragment only if the activity does not have it yet
            // (if the activity was recreated in the past, it might have the fragment already).
            TableListFragment tableListFragment = (TableListFragment) fm.findFragmentById(R.id.fragment_table_list);

            if ( tableListFragment == null ) {

                tableListFragment = TableListFragment.newInstance( RestaurantManager.getTables() );

                fm.beginTransaction()
                        .add(R.id.fragment_table_list,tableListFragment)
                        .commit();
            }

            // This title will be replaced by the table name if we are showing the TablePagerFragment
            setTitle( getString(R.string.app_name) );
        }

        // Make sure there is space for the TablePager
        // (only when we are on a big screen and orientation is landscape)
        if (findViewById(R.id.fragment_table_pager) != null) {

            TablePagerFragment tablePagerFragment = (TablePagerFragment) fm.findFragmentById(R.id.fragment_table_pager);

            if ( tablePagerFragment == null) {

                tablePagerFragment = TablePagerFragment.newInstance(tablePos);

                fm.beginTransaction()
                        .add(R.id.fragment_table_pager, tablePagerFragment)
                        .commit();
            }

            setTitle( RestaurantManager.getTableAtPos(tablePos).getName() );
        }
    }

    // This method removes all fragments from the activity
    private void removeActivityFragments() {

        FragmentManager fm = getFragmentManager();

        TableListFragment tableListFragment = (TableListFragment) fm.findFragmentById(R.id.fragment_table_list);
        if ( tableListFragment != null ) {

            fm.beginTransaction()
                    .remove(tableListFragment)
                    .commit();
        }

        TablePagerFragment tablePagerFragment = (TablePagerFragment) fm.findFragmentById(R.id.fragment_table_pager);
        if ( tablePagerFragment != null ) {

            fm.beginTransaction()
                    .remove(tablePagerFragment)
                    .commit();
        }
    }

}
