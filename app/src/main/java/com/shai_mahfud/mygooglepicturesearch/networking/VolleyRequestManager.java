/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.networking;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Manages the network requests sent via the Volley library
 *
 * @author Shai Mahfud
 */
public class VolleyRequestManager {
    // Constants:
    private static final int CACHE_SIZE = 100;


    // Fields:
    /* The sole instance of this singleton class */
    private static VolleyRequestManager instance;

    /* Manages requests to retrieve non-image data from urls */
    private RequestQueue requestQueue;
    /* Used for loading pictures from url */
    private ImageLoader pictureLoader;
    /* The cache for the loader */
    private ImageLoader.ImageCache imageCache;


    // Constructors:
    /*
     * Initializes this component
     *
     * @param ctx The context in which this instance is created
     */
    private VolleyRequestManager(Context ctx) {
        requestQueue = getRequestQueue(ctx);
        imageCache = new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(CACHE_SIZE);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        };
        pictureLoader = new ImageLoader(requestQueue, imageCache);
    }

    /**
     *
     * @param ctx The context in which this method is called
     *
     * @return The sole instance of this singleton class
     */
    public static synchronized VolleyRequestManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new VolleyRequestManager(ctx);
        }

        return instance;
    }

    /**
     *
     * @return The sole instance of this class
     */
    public static synchronized VolleyRequestManager getInstance() {
        return instance;
    }

    /**
     *
     * @param ctx The context in which this method is called
     *
     * @return The request queue which manages the url connections
     */
    public RequestQueue getRequestQueue(Context ctx) {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }

        return requestQueue;
    }

    /**
     * Adds a new API call to the queue
     *
     * @param ctx The context in which this method is called
     * @param req The new request to add
     *
     * @param <T> The type of response the API should retrieve (JSONObject, JSONArray, String, ...)
     */
    public <T> void addToRequestQueue(Context ctx, Request<T> req) {
        getRequestQueue(ctx).add(req);
    }

    /**
     * Fetches a picture, either from cache or from url.
     *
     * @param url The link where the picture is stored
     * @param listener Gets notified when the response is ready
     */
    public void getPicture(String url, ImageLoader.ImageListener listener) {
        pictureLoader.get(url, listener);
    }

    /**
     * If a picture exists in cache, removes the picture and retrieves it again from url.
     *
     * @param url The link where the picture is stored
     * @param listener Gets notified when the response is ready
     */
    public void refresh(String url, ImageLoader.ImageListener listener) {
        if (imageCache.getBitmap(url) != null) {
            imageCache.putBitmap(url, null);
            getPicture(url, listener);
        }
    }
}
