package com.azhar.waiter.backgroundtaskhandler;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


// This class represent a handler to execute operations in background.
// (the objects to execute must implement the BackgroundTaskRunnable interface)
// ----------------------------------------------------------------------------

public class BackgroundTaskHandler extends AsyncTask<Void,Integer,Boolean> {

    // Object attributes:
    private BackgroundTaskRunnable mTask;       // Object with the task to be executed on background
    private BackgroundTaskListener mListener;   // Listener to handle the results (if any)
    private ProgressDialog mProgress;           // Progress dialog to be shown
    private boolean mOperationFailed;           // Flag that indicates if the operation failed


    // Class constructors:

    // With progress dialog: if it is null, it will not be shown.
    // In other case, it must be already initialized and attached to a context.
    public BackgroundTaskHandler(BackgroundTaskRunnable task, BackgroundTaskListener listener, ProgressDialog progress) {

        mTask = task;
        mListener = listener;
        mProgress = progress;

        mOperationFailed = false;
    }

    // Without progress dialog (is the same than previous constructor, with progress = null)
    public BackgroundTaskHandler(BackgroundTaskRunnable task, BackgroundTaskListener listener) {

        mTask = task;
        mListener = listener;
        mProgress = null;

        mOperationFailed = false;
    }


    // Other methods:

    public boolean hasFailed() {
        return mOperationFailed;
    }

    public BackgroundTaskRunnable getTask() {
        return mTask;
    }

    public String getTaskId() {
        return mTask.getId();
    }

    public Object getTaskProduct() {
        return mTask.getProduct();
    }


    // Methods inherited from AsyncTask:

    // What to do on the UI thread BEFORE starting the background operation
    @Override
    protected void onPreExecute() {

        super.onPreExecute();

        if ( mProgress != null )
            mProgress.show();

        Log.d("BackgroundTaskHandler","INFO: Starting task " + mTask.getId() );
    }

    // What to do DURING the background operation (on a background thread)
    @Override
    protected Boolean doInBackground(Void... params) {

        boolean success = false;

        if ( mTask != null )
            success = mTask.execute();

        if ( !success )
            mOperationFailed = true;

        return success;
    }

    // What to do on the UI thread AFTER finishing the background operation (not cancelled)
    @Override
    protected void onPostExecute(Boolean result) {

        Log.d("BackgroundTaskHandler","INFO: Finished task " + mTask.getId() );

        super.onPostExecute(result);

        if ( mProgress != null )
            mProgress.dismiss();

        if ( mListener != null )
            mListener.onBackgroundTaskFinished(this);
    }

    // What to do on the UI thread if the background operation was cancelled
    @Override
    protected void onCancelled() {

        Log.d("BackgroundTaskHandler","INFO: Cancelled task " + mTask.getId() );

        if ( mTask != null )
            mTask.cancel();

        mOperationFailed = true;

        if ( mProgress != null )
            mProgress.dismiss();

        if ( mListener != null )
            mListener.onBackgroundTaskFinished(this);
    }


}
