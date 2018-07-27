/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.networking;

import com.shai_mahfud.mygooglepicturesearch.model.PicturesDataResponseJson;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Encapsulates the network operations associated with retrieving the pictures data using Retrofit.
 *
 * @author Shai Mahfud
 */
public interface PictureDataInterface {
    // Constants:
    /** The key of the key query param */
    String QUERY_PARAM_KEY = "key";
    /** The key of the cx query param */
    String QUERY_PARAM_CX = "cx";
    /** The key of the num query param */
    String QUERY_PARAM_NUM = "num";
    /** The key of the start index query param */
    String QUERY_PARAM_START_INDEX = "start";
    /** The key of the search expression query param */
    String QUERY_PARAM_SEARCH_EXPRESSION = "q";
    /**
     * The dynamic part of the URL of the photos search API.
     */
    String DYNAMIC_GOOGLE_SEARCH_PHOTO_API_URL = "/customsearch/v1?searchType=image";


    // Methods:
    /**
     * Retrieves the pictures data as a JSONObject from Google API
     *
     * @param queryOptions A mapping of the query parameters keys to their values
     *
     * @return The response after parsing from JSON to the model I defined (which gets rid of all
     * the redundant data returned by Google API).
     */
    @GET(DYNAMIC_GOOGLE_SEARCH_PHOTO_API_URL)
    Call<PicturesDataResponseJson> getPicturesData(@QueryMap Map<String, String> queryOptions);
}
