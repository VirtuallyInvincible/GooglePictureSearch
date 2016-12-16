/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Mediator that allows communication between the UI element which enables the user to insert the
 * search input and the UI element which displays the search results to the screen.
 *
 * @author Shai Mahfud
 */
class PicturesSearchMediator implements OnSearchListener, OnMoreResultsListener {
    // Constants:
    /*
     * The desired size of each chunk retrieved from the server. Note that Google API doesn't allow
     * more than 10 results per request. Requesting more than 10 yields error code 400.
     */
    private static final int CHUNK_SIZE = 10;
    /*
     * The URL of the search API.
     */
    private static final String GOOGLE_SEARCH_PHOTO_API = "https://www.googleapis.com/customsearch/" +
            "v1?key=AIzaSyBW8wxhgNKU36Q_WPqJciAa9no2mqtcng4&cx=005800383728131713214:5jocmduwqum" +
            "&searchType=image&num=" + CHUNK_SIZE + "&start=%d&q=%s";
    /* Key for obtaining the shared preferences for this component */
    private static final String KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS =
            "pictures_search_mediator_shared_prefs";
    /* Key for current search index */
    private static final String KEY_SEARCH_INDEX = "search_index";


    // Fields:
    /* The UI component which displays the returned results of the search */
    private PicturesDisplayerInterface picturesDisplayer;
    /*
     * The UI component which enables the user to insert the search subject and initiate a new
     * search.
     */
    private SearchComponentInterface searchComponent;
    /*
     * Shown when results are fetched from the server to prevent the user from tapping search or
     * more results multiple times.
     */
    private ProgressDialog progressDialog;
    /* The zero-relative index of the next result to retrieve (1 for starting a new search) */
    private int resultsIndex = 1;


    // Constructors:
    /**
     * Initiates this object
     *
     * @param ctx The context in which this mediator is initiated
     */
    PicturesSearchMediator(Context ctx, SearchComponentInterface searchComponent,
            PicturesDisplayerInterface picturesDisplayer) {
        // Store the components of the pictures search system:
        this.searchComponent = searchComponent;
        this.picturesDisplayer = picturesDisplayer;

        // Set this mediator as the listener for the events of the components of the system (so that
        // they can access it):
        this.searchComponent.addOnSearchListener(this);
        this.picturesDisplayer.setMoreResultsListener(this);

        // Create an instance of progress dialog to show when the results are being fetched:
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(ctx.getString(R.string.loading_prompt));

        // Get the index of search results (last search result obtained from the server, as stored
        // to shared preferences. Needed to resume the system's state after restarting it):
        SharedPreferences prefs = ctx.getSharedPreferences(
                KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS, Context.MODE_PRIVATE);
        resultsIndex = prefs.getInt(KEY_SEARCH_INDEX, 1);
    }


    // Methods:
    @Override
    public void onSearch(String searchExpression) {
        // Show the progress dialog:
        progressDialog.show();

        // Inform the component which displays the results that a new search has been initiated:
        picturesDisplayer.onNewSearch(searchExpression);

        // Launch a new search request:
        resultsIndex = 1;
        fetchResults();
    }

    @Override
    public void fetchMoreResults() {
        // Show the progress dialog and fetch the next chunk of results:
        progressDialog.show();
        fetchResults();
    }

    /**
     * Call when you want to remove the references to the various components to ease the work of the
     * garbage collector and avoid leaks.
     */
    void terminate() {
        picturesDisplayer.terminate();
        picturesDisplayer = null;
        searchComponent.terminate();
        searchComponent = null;
    }

    /*
     * Initiates a request to the server to fetch results that match the current search expression.
     */
    private void fetchResults() {
        final Context ctx = progressDialog.getContext();
        String searchExpression = searchComponent.getSearchExpression();
        String url = String.format(Locale.ENGLISH, GOOGLE_SEARCH_PHOTO_API, resultsIndex,
                searchExpression);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("CommitPrefEdits")    // Store immediately to device storage so
                    // that if the user exits the screen right after and resumes it, the data
                    // will be ready for use
                    @Override
                    public void onResponse(JSONObject response) {
                        // Update the cache:
                        int numOfResults = PictureDataManager.getInstance().addNewResults(response);

                        // Update the index of the last picture retrieved (the start index for the next chunk):
                        resultsIndex += numOfResults;

                        // Store to shared prefs the current index from which the search will
                        // continue when the user taps the more button:
                        SharedPreferences prefs = ctx.getSharedPreferences(
                                KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_SEARCH_INDEX, resultsIndex);
                        editor.commit();

                        // Inform the displayer component that fresh results have arrived:
                        boolean isLastChunk = (numOfResults < CHUNK_SIZE); // Not very accurate
                            // because it could be that we fetched 10 results and the next chunk
                            // has 0 results.
                        picturesDisplayer.onResults(isLastChunk);

                        // Remove the progress dialog:
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error message:
                        new AlertDialog.Builder(ctx).
                                setTitle(R.string.error).
                                setMessage(R.string.api_call_failure_message).
                                create().show();

                        // Remove the progress dialog:
                        progressDialog.dismiss();

                        // Inform the displayer that an error occurred:
                        picturesDisplayer.onError();
                    }
                }
        );

        // Adding JsonObject request to request queue
        VolleyRequestManager.getInstance(ctx).addToRequestQueue(ctx, jsonObjectReq);
    }
}
