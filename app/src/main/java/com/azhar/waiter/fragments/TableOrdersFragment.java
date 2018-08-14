package com.azhar.waiter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.adapters.OrderListAdapter;
import com.azhar.waiter.model.Order;

import java.util.ArrayList;


// This class represents the fragment showing the list of orders from a table.
// ----------------------------------------------------------------------------

public class TableOrdersFragment extends Fragment {

    // Class attributes
    private static final String ORDERS_KEY = "orders";
    private static final String TABLE_KEY = "table";

    // Object attributes
    private ArrayList<Order> orderList;                         // The order list to show
    private int tablePos;                                       // Position of the table the orders belong to, in the table list
    public TableOrdersFragmentListener tableOrdersFragmentListener;    // Table List listener


    // Class "constructor":

    public static TableOrdersFragment newInstance(ArrayList<Order> orders, int table) {

        // Create the new fragment (using the default constructor)
        TableOrdersFragment fragment = new TableOrdersFragment();

        // We do not keep the model here, just passing it in a bundle to setArguments()
        // (it will be recovered later, in the onCreate() method)
        Bundle arguments = new Bundle();
        arguments.putSerializable(ORDERS_KEY, orders);
        arguments.putSerializable(TABLE_KEY, table);
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

        // Try to get the passed arguments (see the newInstance() method)
        if (getArguments() != null) {
            orderList = (ArrayList<Order>) getArguments().getSerializable(ORDERS_KEY);
            tablePos = getArguments().getInt(TABLE_KEY);
        }
    }


    // This method is called when a fragment is first attached to its context
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the OnTableSelectedListener interface:
        // If so, keep the reference to it.
        if (context instanceof TableOrdersFragmentListener)
            tableOrdersFragmentListener = (TableOrdersFragmentListener) context;

        // If not, throw an exception (will terminate the program).
        else
            throw new RuntimeException(context.toString() + " must implement TableOrdersFragmentListener");
    }

    // Same as previous, but using the deprecated onAttach(Activity) instead the newer onAttach(Context)
    // (called when using android.app.Fragment, or when device API is under 23)
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getActivity() instanceof TableOrdersFragmentListener)
            tableOrdersFragmentListener = (TableOrdersFragmentListener) getActivity();
        else
            throw new RuntimeException(getActivity().toString() + " must implement TableOrdersFragmentListener");
    }


    // This method is called to have the fragment instantiate its user interface view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get the root view of the fragment based on its corresponding layout
        // It will be attached to container (which is the corresponding activity view), but not yet
        View rootView = inflater.inflate(R.layout.fragment_table_orders, container, false);

        // Reference to UI elements
        ListView list = (ListView) rootView.findViewById(android.R.id.list);

        // Adapter to load the table list into the view
        OrderListAdapter adapter = new OrderListAdapter(getActivity(), orderList, R.drawable.ic_brokenimage);

        // Assign the adapter to the list
        list.setAdapter(adapter);

        // Assign a listener to the list to execute some action when an order is selected
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int orderPos, long l) {

                if (tableOrdersFragmentListener != null)
                    tableOrdersFragmentListener.onOrderSelected(orderPos, tablePos);
            }
        });

        // Return the root view of the fragment
        return rootView;
    }


    // This method is called when the fragment is no longer attached to its activity
    @Override
    public void onDetach() {
        super.onDetach();

        // Remove the reference to the table listener
        tableOrdersFragmentListener = null;
    }


    // Interface to be implemented by any activity/context that contains this fragment
    public interface TableOrdersFragmentListener {

        void onOrderSelected(int orderPos, int tablePos);

        void onAddOrderClicked(int tablePos);
    }
}
