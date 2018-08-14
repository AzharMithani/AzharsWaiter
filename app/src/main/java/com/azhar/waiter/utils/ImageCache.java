package com.azhar.waiter.utils;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;


// This class references a singleton object representing the cache for all the downloaded images in the app.
// All public methods on this class are static, and it is not possible to instantiate this class.
// ----------------------------------------------------------------------------

public class ImageCache {

    // Class attributes
    private static Map<String,Bitmap> cache;   // The singleton object stored in the class

    // The constructor is private, use getCache() to create/get the cache from outside
    private ImageCache() {
    }

    // Gets a reference to the singleton object
    public static Map<String,Bitmap> getCache() {

        if (cache == null)
            cache = new HashMap<String, Bitmap>();

        return cache;
    }

    // Clears the existing data in the cache
    public static void clear() {
        cache = new HashMap<String, Bitmap>();
    }
}
