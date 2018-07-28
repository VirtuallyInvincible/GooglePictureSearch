/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.model.PicturesDataResponseJson;
import com.shai_mahfud.mygooglepicturesearch.networking.PictureDataInterface;
//import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Mediator that allows communication between the UI element which enables the user to insert the
 * search input and the UI element which displays the search results to the screen.
 *
 * @author Shai Mahfud
 */
class PicturesSearchMediator implements OnSearchListener, OnMoreResultsListener,
        PicturesDataAccessProvider {
    // Inner classes:
    /*
     * Builds instances of this class rather than instantiating it using a constructor. This enables
     * to pass arguments via several methods rather than pass them all in in a single method /
     * constructor, thus keeping method/constructor signatures short.
     */
    static class Builder {
        // Fields:
        /* The new instance of PicturesSearchMediator that this builder is currently constructing */
        private PicturesSearchMediator newInstance;


        // Constructors:
        Builder(Context ctx) {
            newInstance = new PicturesSearchMediator();

            // Create an instance of progress dialog to show when the results are being fetched:
            newInstance.progressDialog = new ProgressDialog(ctx);
            newInstance.progressDialog.setCancelable(false);
            newInstance.progressDialog.setMessage(ctx.getString(R.string.loading_prompt));

            // Get the index of search results (last search result obtained from the server, as
            // stored to shared preferences. Needed to resume the system's state after restarting
            // it):
            SharedPreferences prefs = ctx.getSharedPreferences(
                    KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS, Context.MODE_PRIVATE);
            newInstance.resultsIndex = prefs.getInt(KEY_SEARCH_INDEX, 1);
        }


        // Methods:
        /**
         * Sets the input component used for setting the search subject of the pictures to be
         * searched.
         *
         * @param searchComponent The UI component which enables the user to insert the search
         *                        subject and initiate a new search.
         *
         * @return this Builder object configured with the changes done by the call to this method
         */
        Builder setInputComponent(SearchComponentInterface searchComponent) {
            newInstance.searchComponent = searchComponent;
            newInstance.searchComponent.addOnSearchListener(newInstance);
            return this;
        }

        /**
         * Sets the component which displays the search results when those are retrieved.
         *
         * @param picturesDisplayer The UI component which displays the returned results of the
         *                          search
         * @param dataManager The object responsible for managing the data of the pictures
         *
         * @return this Builder object configured with the changes done by the call to this method
         */
        Builder setDisplayerComponent(PicturesDisplayerInterface picturesDisplayer,
                PictureDataManager dataManager) {
            newInstance.picturesDisplayer = picturesDisplayer;
            newInstance.picturesDisplayer.setMoreResultsListener(newInstance);
            newInstance.setPictureDataManager(dataManager);
            return this;
        }

        /**
         * @return A new instance of PicturesSearchMediator, configured with the properties set by
         * this builder
         */
        PicturesSearchMediator build() {
            return newInstance;
        }
    }


    // Constants:
    private static final String BASE_GOOGLE_SEARCH_PHOTO_API_URL = "https://www.googleapis.com";
    private static final String GOOGLE_API_KEY = "AIzaSyCRUVMCziB7WOZFrD-pTUqJSJnZ93gb73g";
    private static final String GOOGLE_API_CX = "000415672875380541873:0wfhf2dsjhw";
    /**
     * The desired size of each chunk retrieved from the server. Note that Google API doesn't allow
     * more than 10 results per request. Requesting more than 10 yields error code 400.
     */
    private static final int CHUNK_SIZE = 10;
    private static final String KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS =
            "pictures_search_mediator_shared_prefs";
    private static final String KEY_SEARCH_INDEX = "search_index";


    // Fields:
    /* Used for sending requests using Retrofit 2 */
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_GOOGLE_SEARCH_PHOTO_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private PicturesDisplayerInterface picturesDisplayer;
    private SearchComponentInterface searchComponent;
    private ProgressDialog progressDialog;
    /* The zero-relative index of the next result to retrieve (1 for starting a new search) */
    private int resultsIndex = 1;


    // Constructors:
    private PicturesSearchMediator() {}


    // Methods:
    @Override
    public void onSearch(String searchExpression) {
        progressDialog.show();

        // Inform the component which displays the results that a new search has been initiated:
        picturesDisplayer.onNewSearch(searchExpression);

        // Launch a new search request:
        resultsIndex = 1;
        fetchResults();
    }

    @Override
    public void fetchMoreResults() {
        progressDialog.show();
        fetchResults();
    }

    @Override
    public PictureDataManager getPictureDataManager() {
        return picturesDisplayer.getPictureDataManager();
    }

    @Override
    public void setPictureDataManager(PictureDataManager pictureDataManager) {
        picturesDisplayer.setPictureDataManager(pictureDataManager);
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

    void onOrientationChange(Context ctx) {
        picturesDisplayer.onOrientationChange(ctx);
    }

    /*
     * Initiates a request to the server to fetch results that match the current search expression.
     */
    private void fetchResults() {
        final Context ctx = progressDialog.getContext();
        String searchExpression = searchComponent.getSearchExpression();
        PictureDataInterface pdi = retrofit.create(PictureDataInterface.class);
        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put(PictureDataInterface.QUERY_PARAM_KEY, GOOGLE_API_KEY);
        queryOptions.put(PictureDataInterface.QUERY_PARAM_CX, GOOGLE_API_CX);
        queryOptions.put(PictureDataInterface.QUERY_PARAM_NUM, Integer.toString(CHUNK_SIZE));
        queryOptions.put(PictureDataInterface.QUERY_PARAM_START_INDEX, Integer.toString(resultsIndex));
        queryOptions.put(PictureDataInterface.QUERY_PARAM_SEARCH_EXPRESSION, searchExpression);
        final Call<PicturesDataResponseJson> call = pdi.getPicturesData(queryOptions);
        call.enqueue(new Callback<PicturesDataResponseJson>() {
            @SuppressLint("CommitPrefEdits")    // Store immediately to device storage so that if
            // the user exits the screen right after and resumes it, the data will be ready for use
            @Override
            public void onResponse(Call<PicturesDataResponseJson> call,
                                   retrofit2.Response<PicturesDataResponseJson> response) {
                int responseCode = response.code();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    onFailure(call, null);  // Possible authentication error or something else
                    return;
                }

                // Update the cache:
                PicturesDataResponseJson requestResults = response.body();
                int numOfResults = getPictureDataManager().addNewResults(requestResults);

                // Set the start index for the next chunk and store in shared preferences:
                resultsIndex += numOfResults;
                SharedPreferences prefs = ctx.getSharedPreferences(
                        KEY_PICTURES_SEARCH_MEDIATOR_SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_SEARCH_INDEX, resultsIndex);
                editor.commit();

                // Inform the displayer component that fresh results have arrived:
                boolean isLastChunk = (numOfResults < CHUNK_SIZE); // Not very accurate because it
                // could be that we fetched 10 results and the next chunk has 0 results.
                picturesDisplayer.onResults(isLastChunk);

                dismissProgressDialog();
            }

            @Override
            public void onFailure(Call<PicturesDataResponseJson> call, Throwable t) {
                HttpUrl url = call.request().url();
                if (t != null) {
                    t.printStackTrace();
                }

                // Error message:
                new AlertDialog.Builder(ctx).
                        setTitle(R.string.error).
                        setMessage(R.string.api_call_failure_message).
                        create().show();

                // Remove the progress dialog:
                dismissProgressDialog();

                // Inform the displayer:
                picturesDisplayer.onError();
            }
        });
        /*
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("CommitPrefEdits")    // Store immediately to device storage so
                    // that if the user exits the screen right after and resumes it, the data
                    // will be ready for use
                    @Override
                    public void onResponse(JSONObject response) {
                        // Update the cache:
                        int numOfResults = getPictureDataManager().addNewResults(response);

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
                        dismissProgressDialog();
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
                        dismissProgressDialog();

                        // Inform the displayer that an error occurred:
                        picturesDisplayer.onError();
                    }
                }
        );

        // Adding JsonObject request to request queue
        VolleyRequestManager.getInstance(ctx).addToRequestQueue(ctx, jsonObjectReq);
        */
    }

    private void dismissProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
