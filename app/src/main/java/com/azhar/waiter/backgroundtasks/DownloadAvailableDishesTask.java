package com.azhar.waiter.backgroundtasks;

import android.content.Context;
import android.util.Log;

import com.azhar.waiter.backgroundtaskhandler.BackgroundTaskRunnable;
import com.azhar.waiter.model.Allergen;
import com.azhar.waiter.model.Dish;
import com.azhar.waiter.model.RestaurantManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


// This class represents the task to download the available dishes from a remote server.
// Implements the BackgroundTaskRunnable interface to be executed by a BackgroundTaskHandler.
// ----------------------------------------------------------------------------

public class DownloadAvailableDishesTask implements BackgroundTaskRunnable {

    // Class attributes:
    public final static String taskId = "DOWNLOAD_AVAILABLE_DISHES_TASK";


    // Object attributes:
    private Context context;
    private URL serviceUrl;
    private String tablePrefix;
    private boolean randomOrders;

    private boolean isCancelled;


    // Constructor:
    public DownloadAvailableDishesTask(Context context, String stringUrl, String tablePrefix, boolean randomOrders) {

        this.context = context;

        try                             {   serviceUrl = new URL(stringUrl);   }
        catch (MalformedURLException e) {   serviceUrl = null;                 }

        this.tablePrefix = tablePrefix;
        this.randomOrders = randomOrders;
        isCancelled = false;
    }


    // Methods inherited from BackgroundTaskRunnable

    // This task does not return any product
    // (actually the product is the RestaurantManager initialized,
    // but that is a singleton object so there is no need to return anything here)
    public Object getProduct() {
        return null;
    }

    // Returns the operation taskId (to identify what operation was executed)
    public String getId() {
        return taskId;
    }

    // Cancells the operation, if possible
    public void cancel() {
        isCancelled = true;
    }

    // This method represents the operation itself to be executed
    // (returns true if the operation succeeded, or false in any other case)
    public boolean execute() {

        // Check that we have a valid url to connect
        if (serviceUrl == null)
            return false;

        // Connect and download the data
        InputStream input;
        StringBuilder sb;

        try {
            HttpURLConnection connection = (HttpURLConnection) serviceUrl.openConnection();
            connection.connect();
            int responseLength = connection.getContentLength();
            byte data[] = new byte[1024];
            long currentBytes = 0;
            int downloadedBytes;
            input = connection.getInputStream();
            sb = new StringBuilder();

            // The download will continue until the stream is done or the operation is cancelled
            while ( (downloadedBytes = input.read(data)) != -1 && !isCancelled ) {

                sb.append(new String(data, 0, downloadedBytes));
            }

            if (isCancelled) {

                Log.d("DownloadDishesTask", "WARNING: the operation was cancelled");
                return false;
            }
        }
        catch (Exception ex) {

            Log.d("DownloadDishesTask", "ERROR: exception while downloading data");
            ex.printStackTrace();
            return false;
        }


        // Parse the downloaded data
        try {
            JSONObject jsonRoot = new JSONObject(sb.toString());

            // General data (file date, currency, tax rate and table count)
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse( jsonRoot.getString("date") );
            String currency = jsonRoot.getString("currency");
            BigDecimal taxRate = new BigDecimal( jsonRoot.getString("tax") );
            int tableCount = jsonRoot.getInt("tables");

            // Make sure that there is at least one table. If not, stop the process
            if (tableCount <= 0) {
                Log.d("DownloadDishesTask", "ERROR: the 'tables' value must be greater than 0");
                return false;
            }

            // Other possible checks: 'date' is correct, 'currency' is not empty, 'tax' is less than 1.0, etc.
            // ...


            // Now we can initialize the RestaurantManager and populate it
            RestaurantManager.newInstance(date,currency,taxRate,tableCount,tablePrefix);


            // List of available allergens
            JSONArray allergens = jsonRoot.getJSONArray("allergens");

            for (int i = 0; i < allergens.length(); i++) {

                JSONObject a = allergens.getJSONObject(i);

                int aId = a.getInt("id");
                String aName = a.getString("name");
                String aUrlString =  a.getString("icon");

                RestaurantManager.addAllergen( new Allergen(aId,aName,aUrlString) );
            }


            // List of available dishes
            JSONArray dishes = jsonRoot.getJSONArray("dishes");

            for (int i = 0; i < dishes.length(); i++) {

                JSONObject d = dishes.getJSONObject(i);

                String dName = d.getString("name");
                String dDescription = d.getString("description");
                String dUrlString =  d.getString("image");
                BigDecimal dPrice = new BigDecimal( d.getString("price") );

                // Create the current dish (without any allergen)
                Dish dish = new Dish(dName,dDescription,dUrlString,dPrice);

                // Now, the allergens for the current dish
                // (the field is optional, since not all dishes contain allergens)
                JSONArray dishAllergens = null;

                try {
                    dishAllergens = d.getJSONArray("allergens");
                }
                catch (JSONException e) {
                    Log.d("DownloadDishesTask", "INFO: dish " + dName + " does not contain allergens");
                }

                if (dishAllergens != null) {

                    ArrayList<Integer> da = new ArrayList<Integer>();

                    for (int j = 0; j < dishAllergens.length(); j++) {

                        int daId = dishAllergens.getInt(j);
                        da.add(daId);
                    }

                    RestaurantManager.setDishAllergens(dish, da);
                }


                // Finally, we add the current dish to the menu
                RestaurantManager.addDish(dish);
            }
        }
        catch (Exception ex) {

            Log.d("DownloadDishesTask", "ERROR: exception while parsing data");
            ex.printStackTrace();

            return false;
        }


        // Now that all JSON data should be stored in the RestaurantManager,
        // we can generate some random orders for testing
        if (randomOrders)
            RestaurantManager.generateRandomOrders(context);


        // If we made it till here, the operation was completed successfully
        return true;
    }

}
