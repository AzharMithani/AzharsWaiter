package com.azhar.waiter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.cdelg4do.waiterdroid.R;
import com.azhar.waiter.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import static com.azhar.waiter.utils.Utils.MessageType.SNACK;


// This class represents the activity for the app settings form.
// ----------------------------------------------------------------------------

public class SettingsActivity extends AppCompatActivity {

    // Object attributes
    private String PREFS_SERVER_URL_KEY;
    private String PREFS_RANDOM_DATA_KEY;
    private String DEFAULT_SERVER_URL;

    Context thisActivity;
    SharedPreferences prefs;

    EditText txtUrlBox;
    CheckBox chkRandomData;
    Button btnDiscard;
    Button btnSave;


    // Methods inherited from AppCompatActivity:

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        thisActivity = this;

        // String constants
        PREFS_SERVER_URL_KEY = getString(R.string.prefs_urlKey);
        PREFS_RANDOM_DATA_KEY = getString(R.string.prefs_randomDataKey);
        DEFAULT_SERVER_URL = getString(R.string.default_url);

        // Set the toolbar as the action bar for this activity and show the 'back' button up on it
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Action bar title
        setTitle( getString(R.string.activity_title_settings) );


        // Load the preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String urlString = prefs.getString(PREFS_SERVER_URL_KEY, DEFAULT_SERVER_URL);
        boolean generateData = prefs.getBoolean(PREFS_RANDOM_DATA_KEY, false);


        // Reference to UI elements
        txtUrlBox = (EditText) findViewById(R.id.txtUrlBox);
        chkRandomData = (CheckBox) findViewById(R.id.chkRandomData);
        btnDiscard = (Button) findViewById(R.id.btnDiscard);
        btnSave = (Button) findViewById(R.id.btnSave);

        txtUrlBox.setText(urlString);
        chkRandomData.setChecked(generateData);


        btnDiscard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Return to the previous activity with a Canceled result code
                int resultCode = RESULT_CANCELED;
                Intent returnIntent = new Intent();
                setResult(resultCode, returnIntent);

                finish();
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ( !validateData() ) {
                    Utils.showMessage(thisActivity, thisActivity.getString(R.string.error_dataEnteredNotValid), SNACK, null);
                    return;
                }

                savePreferences();

                // Return to the previous activity with an Ok result code
                int resultCode = RESULT_OK;
                Intent returnIntent = new Intent();
                setResult(resultCode, returnIntent);

                finish();
            }
        });
    }

    // What to do when an action bar menu option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean superReturn = super.onOptionsItemSelected(item);

        // Go back to previous activity
        if (item.getItemId() == android.R.id.home) {

            int resultCode = RESULT_CANCELED;
            Intent returnIntent = new Intent();
            setResult(resultCode, returnIntent);

            finish();
            return true;
        }

        return superReturn;
    }


    // Auxiliary methods:

    // Validate the data entered by the user
    // (return true if all data is valid, otherwise returns false)
    private boolean validateData() {

        String newUrlString = txtUrlBox.getText().toString();

        if ( newUrlString.equals("") )
            return true;

        try                             {   new URL(newUrlString);  }
        catch (MalformedURLException e) {   return false;           }

        return true;
    }

    // Save the data entered by the user
    private void savePreferences() {

        String newUrl = txtUrlBox.getText().toString();

        if ( newUrl.equals("") ) {

            prefs.edit()
                 .remove(PREFS_SERVER_URL_KEY)
                 .apply();
        }
        else {

            prefs.edit()
                 .putString(PREFS_SERVER_URL_KEY, newUrl )
                 .apply();
        }

        boolean newGenerateRandomData = chkRandomData.isChecked();

        prefs.edit()
             .putBoolean(PREFS_RANDOM_DATA_KEY, newGenerateRandomData )
             .apply();
    }
}
