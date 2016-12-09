/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for managing the data of the pictures which is retrieved from the API and exposes
 * methods for getting and manipulating the stored data in a thread-safe manner.
 *
 * @author Shai Mahfud
 */
public class PictureDataManager {
    // Constants:
    /* The key of the data structure within the API result that contains the pictures data */
    private static final String KEY_ITEMS = "items";


    // Fields:
    /* The sole instance of this singleton class */
    private static PictureDataManager instance = new PictureDataManager();

    /*
     * The accumulated data of all the pictures fetched and displayed (excluding the pictures
     * themselves, which are very heavy and shouldn't be stored in a data structure.
     */
    private List<PictureData> picturesData = new ArrayList<>();
    /* A lock with which to synchronize operations on the pictures data */
    private final Object lock = new Object();


    // Methods:
    /**
     *
     * @return The sole instance of this class
     */
    public static PictureDataManager getInstance() {
        return instance;
    }

    /**
     * Adds data of newly retrieved pictures to the data structure.
     *
     * @param newResults The new results from the server in JSON
     *
     * @return The number of pictures retrieved
     */
    public int addNewResults(JSONObject newResults) {
        int numOfNewResults = 0;
        // Note for the reader: using synchronized in the method signature is possible but
        // experience taught me it's better to synchronize inside the body of a method rather than
        // in its signature, because if a deadlock occurs, it's much easier to find the cause that
        // way, for example by printing before each synchronized block, right after capturing the
        // lock and right after releasing the lock. This is much more difficult to do with
        // synchronized methods - you'll have to print before each method call.
        synchronized (lock) {
            try {
                JSONArray newPicturesData = newResults.getJSONArray(KEY_ITEMS);
                numOfNewResults = newPicturesData.length();
                for (int i = 0; i < numOfNewResults; i++) {
                    PictureData newPictureData = new PictureData();
                    newPictureData.fromJson(newPicturesData.getJSONObject(i));
                    picturesData.add(newPictureData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return numOfNewResults;
    }

    /**
     *
     * @return The number of accumulated pictures
     */
    public int getCount() {
        int count;
        synchronized (lock) {
            count = picturesData.size();
        }
        return count;
    }

    /**
     * Fetches the data of a specific picture whose index is given as argument.
     *
     * @param position The zero-relative position of the picture within the accumulated pictures
     *                 data structure
     *
     * @return The item stored in the position given as argument
     */
    public PictureData getItem(int position) {
        PictureData item;
        synchronized (lock) {
            item = picturesData.get(position);
        }
        return item;
    }

    /**
     * Removes all the data currently stored
     */
    public void clear() {
        synchronized (lock) {
            picturesData.clear();
        }
    }
}
