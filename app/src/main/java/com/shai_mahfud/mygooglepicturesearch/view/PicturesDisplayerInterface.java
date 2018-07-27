/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.content.Context;

/**
 * Defines the basic behavior that should be implemented by any UI component which should display
 * Google picture API search results.
 *
 * @author Shai Mahfud
 */
interface PicturesDisplayerInterface extends PicturesDataAccessProvider {
    /**
     * Sets a new listener to be notified when the next chunk of results should be fetched
     *
     * @param listener The listener to set
     */
    void setMoreResultsListener(OnMoreResultsListener listener);
    /**
     * Called when a new search is executed
     *
     * @param searchExp The subject of the search
     */
    void onNewSearch(String searchExp);
    /**
     * Called when search results are available
     *
     * @param isLastChunk The results represent the last chunk
     */
    void onResults(boolean isLastChunk);
    /**
     * Called when an error occurs while trying to fetch more results.
     */
    void onError();
    /** Called to free resources and terminate operations */
    void terminate();
    /** Called when a screen orientation change occurs */
    void onOrientationChange(Context ctx);
}
