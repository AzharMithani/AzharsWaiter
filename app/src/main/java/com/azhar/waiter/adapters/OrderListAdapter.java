package com.azhar.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskHandler;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskListener;
import com.azhar.waiter.backgroundtasks.DownLoadImageTask;
import com.azhar.waiter.model.Order;
import com.azhar.waiter.utils.Utils;

import java.util.ArrayList;


// This class is the adapter needed by a ListView to represent the list of orders for a table.
// ----------------------------------------------------------------------------

public class OrderListAdapter extends BaseAdapter implements BackgroundTaskListener {

    // Class attributes
    private final static int rowlayout = R.layout.row_order;
    private final static int rowlayout_empty = R.layout.row_order_empty;


    // Object attributes
    private final Context context;
    private final ArrayList<Order> orderList;
    private final int brokenImageResource;
    private LayoutInflater inflater;


    // Class constructor
    public OrderListAdapter(Context context, ArrayList<Order> orderList, int brokenImageResource) {

        this.context = context;
        this.orderList = orderList;
        this.brokenImageResource = brokenImageResource;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // Methods inherited from BaseAdapter:

    // Total elements on the list
    @Override
    public int getCount()
    {
        if ( modelIsEmpty() )
            return 1;

        return orderList.size();
    }

    // Gets the order at a given position
    @Override
    public Order getItem(int pos)
    {
        if ( !modelIsEmpty() )
            return orderList.get(pos);

        return null;
    }

    // Determines if a row of the list can be selected
    @Override
    public boolean isEnabled(int position) {

        // Any row can be selected only if there are orders to show in the list.
        // Otherwise, no one can be selected.
        return ( !modelIsEmpty() );
    }

    // Gets the order id at a given position (not used)
    @Override
    public long getItemId(int pos)
    {
        return (long) pos;
    }

    // Gets the row view for a given position
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        // If there are no orders to list, just show a row with an informative text
        if ( modelIsEmpty() ) {

            if (convertView == null)
                convertView = inflater.inflate(rowlayout_empty, parent, false);

            // Reference to UI elements
            TextView txtNoOrders  = (TextView) convertView.findViewById(R.id.txtNoOrders);

            // Tell the user the table has no orders yet
            txtNoOrders.setText( context.getString(R.string.txt_tableHasNoOrders) );
        }

        // If there are orders to show in the model, construct the row view as usual
        else {

            ViewHolder holder;

            if (convertView == null) {

                convertView = inflater.inflate(rowlayout, parent, false);

                holder = new ViewHolder();
                holder.dishImage = (ImageView) convertView.findViewById(R.id.imgDishImage);
                holder.dishName  = (TextView) convertView.findViewById(R.id.txtDishName);
                holder.orderNotes  = (TextView) convertView.findViewById(R.id.txtOrderNotes);

                convertView.setTag(holder);
            }

            else
                holder = (ViewHolder) convertView.getTag();

            // Sync the view with the order data
            holder.dishName.setText( orderList.get(pos).getDishName() );
            holder.orderNotes.setText( orderList.get(pos).getNotes() );

            // Attempt to download the order image in background
            Utils.downloadImageInBackground(orderList.get(pos).getDish().imageUrl, holder.dishImage, this);
        }


        return convertView;
    }


    // Methods inherited from the BackgroundTaskListener interface:

    public void onBackgroundTaskFinished(BackgroundTaskHandler taskHandler) {

        // Determine if the task was the load of the dish image
        if ( taskHandler.getTaskId() == DownLoadImageTask.taskId ) {

            Utils.showDownloadedImage(taskHandler,brokenImageResource);
        }
    }


    // Auxiliary methods/classes:

    // Determines if the data model for the adapter has no items
    private boolean modelIsEmpty() {

        if ( orderList != null && orderList.size() > 0)
            return false;

        return true;
    }


    // This class is just a container with references to the row UI elements
    public class ViewHolder {

        public ImageView dishImage;
        public TextView dishName;
        public TextView orderNotes;
    }

}
