/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

/**
 * Stores the data retrieved from the pictures API which is relevant for our application.
 *
 * @author Shai Mahfud
 */
public class PictureData {
    // Fields:
    /* The picture's title */
    @SuppressWarnings("FieldCanBeLocal")
    private String title = "";
    /* The URL where the picture is stored */
    @SuppressWarnings("FieldCanBeLocal")
    private String link = "";


    // Constructors:
    /**
     * Initializes this component. Required for ActiveAndroid.
     */
    public PictureData() {
        super();
    }


    // Methods:
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
        return link;
    }

    /**
     *
     * @param title The title to set
     */
    void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @param link The link to set
     */
    void setLink(String link) {
        this.link = link;
    }
}
