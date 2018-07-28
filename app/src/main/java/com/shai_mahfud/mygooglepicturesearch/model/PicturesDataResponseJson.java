/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

/**
 * A model matching the raw response JSON retrieved by Retrofit
 *
 * @author Shai Mahfud
 */
public class PicturesDataResponseJson {
    // Fields:
    /* The pictures data in the response */
    private PictureData[] items = null;


    // Methods:
    /**
     *
     * @return The current chunk's results in array format
     */
    PictureData[] getItems() {
        return items;
    }
}
