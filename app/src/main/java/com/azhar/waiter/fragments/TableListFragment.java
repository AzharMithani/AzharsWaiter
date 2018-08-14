package com.azhar.waiter.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.adapters.TableListAdapter;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.model.Table;

import java.util.ArrayList;


// This class represents the fragment showing the list of existing tables.
// ----------------------------------------------------------------------------

public class TableListFragment extends Fragment {

    // Class attributes
    private static final String MODEL_KEY = "model";

    // Object attributes
    private ArrayList<Table> tableList;                         // Model for this fragment
    private OnTableSelectedListener onTableSelectedListener;    // Table List listener
    ListView list;


    // Class "constructor":

    /* If some data must be passed to a fragment, using a newInstance() method is
     * the preferred way to create a fragment, instead of overloading the default constructor:
     *
     *  - If Android destroys and recreates the Fragment later, it will call the no-argument
     *   constructor of the fragment. That is why overloading the constructor is not recommended.
     *
     * - The way to pass data to a Fragment so that they are available after fragment recreation
     *   is to pass a bundle to the setArguments method (so data must be Serializable/Parcelable)
     *
     *   Later, in the Fragment onCreate(), data can be accessed by calling:
     *   getArguments().get("key"); if the data is an Object
     *   getArguments().getInt("my_key",0); if the data is an int, etc.
     *
     * - Remember: setArguments() can only be called BEFORE the fragment is attached to an Activity.
    */

    public static TableListFragment newInstance(ArrayList<Table> model) {

        // Create the new fragment (using the default constructor)
        TableListFragment fragment = new TableListFragment();

        // We do not keep the model here, just passing it in a bundle to setArguments()
        // (it will be recovered later, in the onCreate() method)
        Bundle arguments = new Bundle();
        arguments.putSerializable(MODEL_KEY, model);
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

        // Try to get the model from the passed arguments (see the newInstance() method)
        if (getArguments() != null)
            tableList = (ArrayList<Table>) getArguments().getSerializable(MODEL_KEY);
    }


    // This method is called when a fragment is first attached to its context
    // (for devices having at least API 23 OR using android.support.v4.app.Fragment)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the OnTableSelectedListener interface:
        // If so, keep the reference to it.
        if (context instanceof OnTableSelectedListener)
            onTableSelectedListener = (OnTableSelectedListener) context;

        // If not, throw an exception (will terminate the program).
        else
            throw new RuntimeException(context.toString() + " must implement OnTableSelectedListener");
    }

    // Same as previous, but using the deprecated onAttach(Activity) instead the newer onAttach(Context)
    // (called when using android.app.Fragment, or when device API is under 23)
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getActivity() instanceof OnTableSelectedListener)
            onTableSelectedListener = (OnTableSelectedListener) getActivity();
        else
            throw new RuntimeException(getActivity().toString() + " must implement OnTableSelectedListener");
    }


    // This method is called to have the fragment instantiate its user interface view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get the root view of the fragment based on its corresponding layout
        // It will be attached to container (which is the corresponding activity view), but not yet
        View rootView = inflater.inflate(R.layout.fragment_table_list, container, false);

        // Reference to UI elements
        list = (ListView) rootView.findViewById(android.R.id.list);

        // Adapter to load the table list into the view
        TableListAdapter adapter = new TableListAdapter(getActivity(),tableList);

        // Assign the adapter to the list
        list.setAdapter(adapter);

        // Assign a listener to the list to execute some action when a row is selected
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (onTableSelectedListener != null)
                    onTableSelectedListener.onTableSelected(position);
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
        onTableSelectedListener = null;
    }


    // Auxiliary methods:

    // Syncs the view by assigning it a new adapter with an updated table list
    public void syncView() {

        tableList = RestaurantManager.getTables();

        TableListAdapter adapter = new TableListAdapter(getActivity(),tableList);
        list.setAdapter(adapter);
    }


    // Interface to be implemented by any activity/context that contains this fragment
    public interface OnTableSelectedListener {

        void onTableSelected(int pos);
    }
}
