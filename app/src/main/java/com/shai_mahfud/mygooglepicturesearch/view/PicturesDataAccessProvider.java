/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;

/**
 * Implemented by classes that should provide access to an instance of PictureDataManager by other
 * classes of the system (we don't want PictureDataManager to be globally accessible like a
 * singleton, but we do want it to be accessible by any class that needs it)
 *
 * @author Shai Mahfud
 */
interface PicturesDataAccessProvider {
    /**
     *
     * @return An instance of PictureDataManager
     */
    PictureDataManager getPictureDataManager();
    /**
     * Sets the PictureDataManager which would be retrieved by this PicturesDataAccessProvider
     *
     * @param pictureDataManager The manager to set
     */
    void setPictureDataManager(PictureDataManager pictureDataManager);
}
