package com.azhar.waiter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.activities.InvoiceActivity;
import com.azhar.waiter.activities.MainActivity;
import com.azhar.waiter.activities.TablePagerActivity;
import com.azhar.waiter.adapters.TablePagerAdapter;
import com.azhar.waiter.fragments.TableOrdersFragment.TableOrdersFragmentListener;
import com.azhar.waiter.model.Order;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.model.Table;

import java.util.ArrayList;


// This class represents the fragment with the view pager
// to scroll over the details of all the existing tables.
// Each page will correspond to an instance of TableOrdersFragment.
// ----------------------------------------------------------------------------

public class TablePagerFragment extends Fragment {

    // Class attributes
    private static final String INDEX_KEY = "index";

    // Object attributes
    private ArrayList<Table> tableList;  // Model for this fragment
    private int initialTableIndex;       // Index of the current table
    private ViewPager viewPager;


    // Class constructor:

    public static TablePagerFragment newInstance(int tableIndex) {

        // Create the new fragment (using the default constructor)
        TablePagerFragment fragment = new TablePagerFragment();

        // We do not keep the parameters here, just passing them in a bundle to setArguments()
        // (they will be recovered later, in the onCreate() method)
        Bundle arguments = new Bundle();
        arguments.putInt(INDEX_KEY, tableIndex);
        fragment.setArguments(arguments);

        // Return the new fragment
        return fragment;
    }


    // Methods inherited from Fragment:

    // This method is called for the initial creation of the fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Indicates if this fragment would populate a menu (true) by calling onCreateOptionsMenu()
        setHasOptionsMenu(true);

        // Try to get the passed arguments of newInstance()
        if (getArguments() != null)
            initialTableIndex = getArguments().getInt(INDEX_KEY);

        // Get a reference to the table list
        tableList = RestaurantManager.getTables();

        setHasOptionsMenu(true);
    }

    // This method is called to have the fragment instantiate its user interface view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get the root view of the fragment based on its corresponding layout
        // It will be attached to container (which is the corresponding activity view), but not yet
        View rootView = inflater.inflate(R.layout.fragment_table_pager, container, false);

        // Reference to UI elements
        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        FloatingActionButton btnAddOrder = (FloatingActionButton) rootView.findViewById(R.id.btnAddOrder);

        // Create the adapter to load the pages into the view, and assign it to the View Pager
        TablePagerAdapter adapter = new TablePagerAdapter(getFragmentManager(),tableList);

        // Define the parent activity as listener for the ViewPager
        final Activity parent = getActivity();
        viewPager.setAdapter(adapter);

        // Move the pager to the current table
        viewPager.setCurrentItem(initialTableIndex);

        if ( parent instanceof MainActivity)
            viewPager.addOnPageChangeListener( (MainActivity)parent );

        else if ( parent instanceof TablePagerActivity)
            viewPager.addOnPageChangeListener( (TablePagerActivity)parent );

        // Action for the Add Order button (will be managed by the parent activity)
        btnAddOrder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (parent instanceof TableOrdersFragmentListener) {

                    int currentTable = viewPager.getCurrentItem();
                    ((TableOrdersFragmentListener) parent).onAddOrderClicked(currentTable);
                }
            }
        });

        // Return the root view of the fragment
        return rootView;
    }

    // Action bar menu options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_fragment_tablepager, menu);
    }

    // Enable/disable the menu items right before every time the menu is shown
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuPrev = menu.findItem(R.id.menu_previousPage);
        MenuItem menuNext = menu.findItem(R.id.menu_nextPage);
        MenuItem menuInvoice = menu.findItem(R.id.menu_calculateInvoice);
        MenuItem menuEmpty = menu.findItem(R.id.menu_emptyTable);

        if (viewPager.getCurrentItem() > 0) {
            menuPrev.setEnabled(true);
            menuPrev.setIcon( getResources().getDrawable(R.drawable.ic_arrowleft) );
        }
        else {
            menuPrev.setEnabled(false);
            menuPrev.setIcon( getResources().getDrawable(R.drawable.ic_arrowleft_disabled) );
        }

        if (viewPager.getCurrentItem() < tableList.size() - 1) {
            menuNext.setEnabled(true);
            menuNext.setIcon( getResources().getDrawable(R.drawable.ic_arrowright) );
        }
        else {
            menuNext.setEnabled(false);
            menuNext.setIcon( getResources().getDrawable(R.drawable.ic_arrowright_disabled) );
        }


        Table currentTable = RestaurantManager.getTableAtPos( viewPager.getCurrentItem() );

        menuInvoice.setEnabled(currentTable.getOrders().size() > 0);
        menuEmpty.setEnabled(currentTable.getOrders().size() > 0);
    }

    // What to do when a menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean superReturn = super.onOptionsItemSelected(item);

        final int currentTablePos = viewPager.getCurrentItem();

        // Move to the previous table
        if (item.getItemId() == R.id.menu_previousPage) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            return true;
        }

        // Move to the next table
        else if (item.getItemId() == R.id.menu_nextPage) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            return true;
        }

        // Calculate the invoice for the current table
        else if (item.getItemId() == R.id.menu_calculateInvoice) {

            Intent intent = new Intent(getActivity(), InvoiceActivity.class);
            intent.putExtra(InvoiceActivity.TABLE_POS_KEY, currentTablePos);
            startActivity(intent);

            return true;
        }

        // Empty the current table
        else if (item.getItemId() == R.id.menu_emptyTable) {

            final Table currentTable = RestaurantManager.getTableAtPos(currentTablePos);

            if ( currentTable != null ) {

                // Reset the table orders, but keep a copy of the old list
                final ArrayList<Order> oldList = currentTable.resetTable();

                syncFragmentsView(currentTablePos);

                if (getView() != null) {

                    Snackbar.make(getView(), getString(R.string.msg_tableEmptied), Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    if (currentTable != null && oldList != null)
                                        currentTable.restoreTable(oldList);

                                    syncFragmentsView(currentTablePos);
                                }
                            })
                            .show();
                }
            }

            return true;
        }

        return superReturn;
    }


    // Auxiliary methods:

    // Moves the pager to the table in a given position
    // (used to move the pager from outside this fragment)
    public void movePagerToPosition(int newPosition) {
        viewPager.setCurrentItem(newPosition);
    }

    // Syncs the view by assigning it a new adapter with an updated table list,
    // and moves the pager to the given position
    public void syncView(int tablePos) {

        tableList = RestaurantManager.getTables();

        TablePagerAdapter adapter = new TablePagerAdapter(getFragmentManager(),tableList);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(tablePos);
    }

    // Syncs the views of the pagerFragment and the listFragment (if exist) with the current data model
    private void syncFragmentsView(int tablePos) {

        // Try to refresh the table list fragment, if it exists
        TableListFragment listFragment = (TableListFragment) getFragmentManager().findFragmentById(R.id.fragment_table_list);
        if (listFragment != null)
            listFragment.syncView();

        // Try to refresh the pager fragment, if it exists
        TablePagerFragment pagerFragment = (TablePagerFragment) getFragmentManager().findFragmentById(R.id.fragment_table_pager);
        if (pagerFragment != null)
            pagerFragment.syncView(tablePos);
    }


}
