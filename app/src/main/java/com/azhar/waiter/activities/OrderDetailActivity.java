package com.azhar.waiter.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.adapters.AllergenListAdapter;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskHandler;
import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskListener;
import com.azhar.waiter.backgroundtasks.DownLoadImageTask;
import com.azhar.waiter.model.Order;
import com.azhar.waiter.model.RestaurantManager;
import com.azhar.waiter.utils.Utils;

import static com.azhar.waiter.utils.Utils.MessageType.DIALOG;


// This class represents the activity that shows the detail of an order belonging to a table.
// ----------------------------------------------------------------------------

public class OrderDetailActivity extends AppCompatActivity implements BackgroundTaskListener {

    // Class attributes
    public static final String ORDER_POS_KEY = "orderPos";    // The order's position in the table's orders list
    public static final String TABLE_POS_KEY = "orderTable";  // The table's position in the restaurant's table list


    // Object attributes
    private Order mOrder;
    private int mOrderPos, mTablePos;


    // Methods inherited from AppCompatActivity:

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Set the toolbar as the action bar for this activity and show the 'back' button up on it
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Get the data from the intent passed by the previous activity
        mOrderPos = getIntent().getIntExtra(ORDER_POS_KEY,-1);
        mTablePos = getIntent().getIntExtra(TABLE_POS_KEY,-1);

        if ( mOrderPos == -1 || mTablePos == -1 ) {
            Log.d("OrderDetailActivity","ERROR: Missing data provided by the intent!");
            Utils.showMessage(this, getString(R.string.error_missingIntentParams), DIALOG, getString(R.string.error));
            return;
        }

        // Get a reference to the Order object
        mOrder = RestaurantManager.getOrderAtPos_InTable(mOrderPos, mTablePos);

        if ( mOrder == null ) {
            Log.d("OrderDetailActivity","ERROR: Wrong table/order indexes! (" + mTablePos + ", " + mOrderPos + ")");
            Utils.showMessage(this, getString(R.string.error_wrongTableOrderIndex) + " (" + mTablePos + ", " + mOrderPos + ")", DIALOG, getString(R.string.error));
            return;
        }

        // Action bar title
        setTitle( getString(R.string.activity_title_orderDetail) );

        // Reference to UI elements
        TextView txtOrderPos = (TextView) findViewById(R.id.txtOrderPos);
        TextView txtDishName = (TextView) findViewById(R.id.txtDishName);
        ImageView imgDishImage = (ImageView) findViewById(R.id.imgDishImage);
        TextView txtDishDescription = (TextView) findViewById(R.id.txtDishDescription);
        ListView listAllergens = (ListView) findViewById(R.id.listAllergens);
        TextView txtCaptionNotes = (TextView) findViewById(R.id.txtCaptionNotes);
        final EditText txtNotes = (EditText) findViewById(R.id.txtNotes);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        Button btnOk = (Button) findViewById(R.id.btnOk);

        // Sync the view using the model's data
        txtOrderPos.setText( getString(R.string.txt_orderHeader) + " " + mOrderPos);
        txtDishName.setText( mOrder.getDishName() );
        txtDishDescription.setText( mOrder.getDishDescription() );
        txtCaptionNotes.setText( getString(R.string.txt_orderNotes) );
        txtNotes.setText( mOrder.getNotes() );

        // Attempt to download the dish image in background
        Utils.downloadImageInBackground(mOrder.getDish().imageUrl, imgDishImage, this);

        // If the dish contains some allergen, show the list using an adapter.
        // Otherwise, just hide the list.
        if (mOrder.getAllergens().size()>0) {

            AllergenListAdapter adapter = new AllergenListAdapter(this, mOrder.getAllergens(), R.drawable.ic_brokenimage);
            listAllergens.setAdapter(adapter);
            Utils.setListViewHeightBasedOnChildren(listAllergens);

            listAllergens.setEnabled(false);
        }
        else
            listAllergens.setVisibility(View.GONE);

        // Buttons behavior
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent returnIntent = new Intent();
                int resultCode = RESULT_CANCELED;
                setResult(resultCode, returnIntent);
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Update the order with the (possible) changes made on this activity
                mOrder.setNotes( txtNotes.getText().toString() );

                int resultCode = RESULT_OK;

                Intent returnIntent = new Intent();

                returnIntent.putExtra(ORDER_POS_KEY, mOrderPos);
                returnIntent.putExtra(TABLE_POS_KEY, mTablePos);

                setResult(resultCode, returnIntent);
                finish();
            }
        });
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


    // Methods inherited from the BackgroundTaskListener interface:

    public void onBackgroundTaskFinished(BackgroundTaskHandler taskHandler) {

        // Determine if the task was the load of the dish image
        if ( taskHandler.getTaskId() == DownLoadImageTask.taskId ) {

            Utils.showDownloadedImage(taskHandler,R.drawable.ic_brokenimage);
        }
    }
}
