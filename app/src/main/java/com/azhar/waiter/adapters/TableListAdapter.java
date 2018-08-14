package com.azhar.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.model.Table;

import java.util.ArrayList;


// This class is the adapter needed by a ListView to represent the list of existing tables.
// ----------------------------------------------------------------------------

public class TableListAdapter extends BaseAdapter {

    // Class attributes
    private final static int rowlayout = R.layout.row_table;

    // Object attributes
    private final Context context;
    private final ArrayList<Table> tableList;
    private LayoutInflater inflater;


    // Class constructor
    public TableListAdapter(Context context, ArrayList<Table> tableList) {

        this.context = context;
        this.tableList = tableList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // Methods inherited from BaseAdapter:

    // Total elements on the list
    @Override
    public int getCount()
    {
        return tableList.size();
    }

    // Gets the table at a given position
    @Override
    public Table getItem(int pos)
    {
        return tableList.get(pos);
    }

    // Gets the table id at a given position (not used)
    @Override
    public long getItemId(int pos)
    {
        return (long) pos;
    }

    // Gets the row view for a given position
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        if( convertView == null )
            convertView = inflater.inflate(rowlayout, parent, false);

        // Reference to UI elements
        TextView tableName  = (TextView) convertView.findViewById(R.id.txtTableName);
        TextView tableDetail  = (TextView) convertView.findViewById(R.id.txtTableDetail);

        // Sync the view with the table data
        tableName.setText( tableList.get(pos).getName() );

        int orderCount = tableList.get(pos).orderCount();
        if (orderCount == 0)
            tableDetail.setText( context.getString(R.string.txt_noOrders) );
        else
            tableDetail.setText(orderCount + " " + context.getString(R.string.txt_orders) );


        return convertView;
    }

}
