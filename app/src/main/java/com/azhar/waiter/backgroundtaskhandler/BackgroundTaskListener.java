package com.azhar.waiter.backgroundtaskhandler;


// All classes to process the result of a background task must implement this.
// ----------------------------------------------------------------------------

public interface BackgroundTaskListener {

    void onBackgroundTaskFinished(BackgroundTaskHandler taskHandler);
}
