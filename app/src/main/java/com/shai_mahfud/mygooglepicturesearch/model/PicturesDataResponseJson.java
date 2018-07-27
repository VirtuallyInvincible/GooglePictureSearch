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
    private PictureData[] items = null;


    // Methods:
    PictureData[] getItems() {
        return items;
    }
}
