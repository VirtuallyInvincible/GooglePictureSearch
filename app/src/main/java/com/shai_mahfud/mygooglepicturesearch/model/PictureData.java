/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores the data retrieved from the pictures API which is relevant for our application.
 *
 * @author Shai Mahfud
 */
public class PictureData {
    // Constants:
    private static final String KEY_TITLE = "title";


    // Fields:
    /* The picture's title */
    private String title;
    /* The URL where the picture is stored */
    private String pictureLink;


    // Methods:
    /**
     * Assigns the data from JSON format to the fields in this model
     *
     * @param dataAsJson The data to set for this model in JSON
     */
    void fromJson(JSONObject dataAsJson) {
        try {
            title = dataAsJson.getString(KEY_TITLE);
            pictureLink = dataAsJson.getString("link");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return The title of the picture whose data is stored in this model
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return The url where the picture resides
     */
    public String getPictureLink() {
        return pictureLink;
    }
}
