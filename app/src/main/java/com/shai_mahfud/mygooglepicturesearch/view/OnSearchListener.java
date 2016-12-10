/**
 * All rights reserved to Applicat Technologies Ltd.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

/**
 * Implemented by classes that should listen for search events.
 *
 * @author Shai Mahfud
 */
interface OnSearchListener {
    /**
     * Called when a new search is executed
     *
     * @param searchExp The subject of the search
     */
    void onSearch(String searchExp);
}
