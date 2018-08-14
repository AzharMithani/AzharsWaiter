package com.azhar.waiter.backgroundtaskhandler;


// All classes to be executed by a background task must implement this interface.
// BackgroundTaskRunnable objects are meant to be Context-independent and not to interact with user.
// ----------------------------------------------------------------------------

public interface BackgroundTaskRunnable {

    // String that identifies the type of operation executed
    // (this is used by the BackgroundTaskListener object to handle the operation results)
    String getId();

    // This is meant to return true if the execution was successful, or false in any other case.
    boolean execute();

    // This is meant to force the task to be cancelled, if it is possible
    void cancel();

    // This is meant to return an object with the product of the operation
    // (it should be casted to the appropriate class)
    Object getProduct();
}
