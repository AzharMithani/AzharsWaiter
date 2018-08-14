package com.azhar.waiter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskHandler;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskListener;
import com.azhar.waiter.backgroundtasks.DownLoadImageTask;
import com.azhar.waiter.fragments.DishListFragment;
import com.azhar.waiter.model.Allergen;
import com.azhar.waiter.model.Dish;
import com.azhar.waiter.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;


// This class is the adapter needed by a RecyclerView to represent the list of available dishes.
// ----------------------------------------------------------------------------

public class DishListAdapter extends RecyclerView.Adapter<DishListAdapter.DishViewHolder> implements BackgroundTaskListener {

    // Class attributes
    private final static int cellLayout = R.layout.cell_dish;

    // Object attributes
    private Context context;
    private ArrayList<Dish> dishList;
    private final int brokenImageResource;
    private String currency;
    private DishListFragment.OnDishSelectedListener onDishSelectedListener;


    public DishListAdapter(Context context, ArrayList<Dish> dishList, String currency, DishListFragment.OnDishSelectedListener listener, int brokenImageResource) {
        super();

        this.dishList = dishList;
        this.currency = currency;
        this.context = context;
        this.onDishSelectedListener = listener;
        this.brokenImageResource = brokenImageResource;
    }


    @Override
    public DishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(cellLayout, parent, false);
        return new DishViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(DishViewHolder holder, final int position) {

        holder.bindDish(dishList.get(position), context);

        holder.getView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (onDishSelectedListener != null) {
                    onDishSelectedListener.onDishSelected(position, dishList.get(position), view);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }


    // Methods inherited from the BackgroundTaskListener interface:

    public void onBackgroundTaskFinished(BackgroundTaskHandler taskHandler) {

        // Determine if the task was the load of the dish image
        if ( taskHandler.getTaskId() == DownLoadImageTask.taskId ) {

            Utils.showDownloadedImage(taskHandler,brokenImageResource);
        }
    }


    // Auxiliary methods:

    // Populates a TableLayout having a given number of columns with allergen images
    private void populateTable(TableLayout table, ArrayList<Allergen> allergens, int columns) {

        if ( table == null || columns<1 || allergens.size()==0 || table.getChildCount()>0)
            return;

        // Total rows the table will need
        int totalRows = (int) Math.ceil( (double)allergens.size() / (double)columns );

        // Build a Map with the allergens corresponding to each row
        HashMap<Integer,ArrayList<Allergen>> hashMap = new HashMap<>();

        for (int row=0; row<totalRows; row++) {

            ArrayList<Allergen> rowList = new ArrayList<>();

            int first, last;

            first = row * columns;

            if (row == totalRows-1)
                last = allergens.size() - 1;
            else
                last = ( (row+1) * columns ) - 1;

            for (int a=first; a<=last; a++)
                rowList.add( allergens.get(a) );

            hashMap.put(row, rowList);
        }

        // Add to the table a new row with all the corresponding allergens
        for (int row=0; row<totalRows; row++) {

            ArrayList<Allergen> rowList = hashMap.get(row);

            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            for (int a=0; a<rowList.size(); a++) {

                ImageView icon = new ImageView(context);
                //icon.setImageResource(R.drawable.ic_defaultplaceholder);
                icon.setScaleType(ImageView.ScaleType.FIT_XY);
                icon.setPadding(5, 5, 5, 5);

                int imgWidht = Utils.convertDpToPixel(35, context);
                int imgHeight = Utils.convertDpToPixel(35, context);
                icon.setLayoutParams(new TableRow.LayoutParams(imgWidht,imgHeight));

                tableRow.addView(icon);

                Utils.downloadImageInBackground( rowList.get(a).imageUrl, icon, this);
            }

            table.addView( tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT) );
        }
    }



    public class DishViewHolder extends RecyclerView.ViewHolder {

        // Reference to UI elements
        private ImageView imgDish;
        private TextView txtDishName;
        private TextView txtDishDescription;    // The dish description is only available on landscape
        private TableLayout allergenTable;
        private TextView txtPrice;

        private View view;
        private BackgroundTaskListener listener;


        public DishViewHolder(View itemView, BackgroundTaskListener listener) {
            super(itemView);

            view = itemView;
            this.listener = listener;

            imgDish = (ImageView) view.findViewById(R.id.imgDish);
            txtDishName = (TextView) view.findViewById(R.id.txtDishName);
            txtDishDescription = (TextView) view.findViewById(R.id.txtDishDescription);
            allergenTable = (TableLayout) view.findViewById(R.id.allergenTable);
            txtPrice = (TextView) view.findViewById(R.id.txtDishPrice);

            view.setTag(this);
        }

        public void bindDish(Dish dish, Context context) {

            txtDishName.setText(dish.name);

            if (txtDishDescription != null)
                txtDishDescription.setText(dish.description);

            txtPrice.setText( Utils.getMoneyString(dish.price,currency) );

            // If the dish has some allergen, show the allergen table
            // (with 4 columns in landscape, or 3 in portrait)
            if (dish.allergens.size()>0) {

                if (context.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
                    populateTable(allergenTable, dish.allergens, 4);
                else
                    populateTable(allergenTable, dish.allergens, 3);
            }
            // If no allergens to show, remove all rows in the table
            // (to prevent errors when recycling the view)
            else
                allergenTable.removeAllViewsInLayout();

            // Attempt to download the dish image in background
            Utils.downloadImageInBackground(dish.imageUrl, imgDish, listener);
        }

        public View getView() {
            return view;
        }
    }

}
