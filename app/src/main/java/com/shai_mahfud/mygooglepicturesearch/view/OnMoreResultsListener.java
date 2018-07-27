/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

/**
 * Implemented by classes that should be notified when a request to obtain more results is executed.
 *
 * @author Shai Mahfud
 */
interface OnMoreResultsListener {
    /** Called when the next chunk of results is requested */
    void fetchMoreResults();
}
