package com.azhar.waiter.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.adapters.DishListAdapter;
import com.azhar.waiter.model.Dish;
import com.azhar.waiter.utils.Utils;

import java.util.ArrayList;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static com.azhar.waiter.utils.Utils.MessageType.SNACK;


// This class represents the fragment showing the list of existing dishes.
// ----------------------------------------------------------------------------

public class DishListFragment extends Fragment {

    // Class attributes
    private static final String MODEL_KEY = "model";
    private static final String CURRENCY_KEY = "currency";

    // Object attributes
    private ArrayList<Dish> dishList;                         // Model for this fragment
    private String currency;
    private OnDishSelectedListener onDishSelectedListener;    // Dish List listener
    private RecyclerView list;


    // Class "constructor"

    public static DishListFragment newInstance(ArrayList<Dish> model, String currency) {

        DishListFragment fragment = new DishListFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(MODEL_KEY, model);
        arguments.putSerializable(CURRENCY_KEY, currency);
        fragment.setArguments(arguments);

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
        if (getArguments() != null) {

            dishList = (ArrayList<Dish>) getArguments().getSerializable(MODEL_KEY);
            currency = getArguments().getString(CURRENCY_KEY);
        }
    }


    // This method is called when a fragment is first attached to its context
    // (for devices having at least API 23 OR using android.support.v4.app.Fragment)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure that the fragment context implements the OnTableSelectedListener interface:
        // If so, keep the reference to it.
        if (context instanceof OnDishSelectedListener)
            onDishSelectedListener = (OnDishSelectedListener) context;

            // If not, throw an exception (will terminate the program).
        else
            throw new RuntimeException(context.toString() + " must implement OnDishSelectedListener");
    }

    // Same as previous, but using the deprecated onAttach(Activity) instead the newer onAttach(Context)
    // (called when using android.app.Fragment, or when device API is under 23)
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getActivity() instanceof OnDishSelectedListener)
            onDishSelectedListener = (OnDishSelectedListener) getActivity();
        else
            throw new RuntimeException(getActivity().toString() + " must implement OnDishSelectedListener");
    }


    // This method is called to have the fragment instantiate its user interface view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get the root view of the fragment based on its corresponding layout
        // It will be attached to container (which is the corresponding activity view), but not yet
        View rootView = inflater.inflate(R.layout.fragment_dish_list, container, false);

        // Reference to UI elements
        list = (RecyclerView) rootView.findViewById(R.id.dishList);

        // In landscape, set the "list" layout as a simple list
        if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
            list.setLayoutManager(new LinearLayoutManager(getActivity()));

        // In portrait, set the "list" layout as a table with 2 columns
        else {
            list.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            Utils.showMessage(getActivity(), getString(R.string.msg_rotateMenu), SNACK, null);
        }

        // How to animate elements
        list.setItemAnimator( new DefaultItemAnimator() );

        // Adapter to load the dish list into the view
        DishListAdapter adapter = new DishListAdapter(getActivity(),dishList,currency,onDishSelectedListener,R.drawable.ic_brokenimage);
        list.setAdapter(adapter);

        // Return the root view of the fragment
        return rootView;
    }


    // This method is called when the fragment is no longer attached to its activity
    @Override
    public void onDetach() {
        super.onDetach();

        // Remove the reference to the list listener
        onDishSelectedListener = null;
    }


    // Refreshes the list view by assigning it a new adapter with an updated model
    private void syncView() {

        DishListAdapter adapter = new DishListAdapter(getActivity(),dishList,currency,onDishSelectedListener,R.drawable.ic_brokenimage);
        list.setAdapter(adapter);
    }


    // Interface to be implemented by any activity/context that contains this fragment
    public interface OnDishSelectedListener {

        void onDishSelected(int position, Dish dish, View view);
    }
}
