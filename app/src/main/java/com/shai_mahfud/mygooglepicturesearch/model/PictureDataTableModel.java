/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Stores the data retrieved from the pictures API and that is stored to the SQLite database using
 * ActiveAndroid. This model is used by this library only.
 *
 * @author Shai Mahfud
 */
@Table(name = PictureDataManager.TABLE_NAME)
public class PictureDataTableModel extends Model {
    // Fields:
    /** The picture's title */
    @SuppressWarnings("FieldCanBeLocal")
    @Column(name = "Title") // For ActiveAndroid
    String title = "";
    /** The URL where the picture is stored */
    @SuppressWarnings("FieldCanBeLocal")
    @Column(name = "Link")  // For ActiveAndroid
    String link = "";


    // Constructors:
    /**
     * Initializes this component. Required for ActiveAndroid.
     */
    public PictureDataTableModel() {
        super();
    }
}
