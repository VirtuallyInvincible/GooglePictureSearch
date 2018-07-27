/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

/**
 * Implemented by components that enable the user to execute a search and should permit access to
 * the search data to other components of the system (the mediator, in our case)
 *
 * @author Shai Mahfud
 */
interface SearchComponentInterface {
    /**
     *
     * @return The search expression associated with the current state of the implementing object
     */
    String getSearchExpression();
    /**
     * Adds a listener to be notified when a search is executed via this SearchEditText
     *
     * @param listener The listener to add
     */
    void addOnSearchListener(OnSearchListener listener);
    /** Called to free resources and terminate operations */
    void terminate();
}
