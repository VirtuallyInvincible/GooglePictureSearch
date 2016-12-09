/**
 * All rights reserved to Applicat Technologies Ltd.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

import org.json.JSONObject;

import java.util.Locale;

/**
 * A list which displays pictures loaded from Google API
 *
 * @author Shai Mahfud
 */
public class PicturesList extends LinearLayout implements View.OnClickListener,
        EditText.OnEditorActionListener {
    // Constants:
    /**
     * The desired size of each chunk retrieved from the server. Note that Google API doesn't allow
     * more than 10 results per request. Requesting more than 10 yields error code 400.
     */
    public static final int CHUNK_SIZE = 10;
    /*
     * The URL of the search API.
     */
    private static final String GOOGLE_SEARCH_PHOTO_API = "https://www.googleapis.com/customsearch/" +
            "v1?key=AIzaSyBW8wxhgNKU36Q_WPqJciAa9no2mqtcng4&cx=005800383728131713214:5jocmduwqum" +
            "&searchType=image&num=" + CHUNK_SIZE + "&start=%d&q=%s";
    /* Key for obtaining whether the more results footer should be visible */
    private static final String KEY_IS_FOOTER_VISIBLE = "is_footer_visible";
    /* Key for current search index */
    private static final String KEY_SEARCH_INDEX = "search_index";
    /* Key for obtaining SharedPreferences */
    private static final String KEY_SHARED_PREFS = "shared_prefs";
    /*
     * Key for search input of the user so that it'll be automatically displayed when the screen is
     * loaded
     */
    private static final String KEY_SEARCH_EXP = "search_expression";


    // Fields:
    /* Enables the user to insert search subject */
    private ClearableEditText searchBox;
    /*
     * Enables the user to query for more results once the first chunk of data has arrived and
     * there are more results available.
     */
    private Button footer;
    /* The adapter for the pictures list */
    private PictureListAdapter pictureListAdapter;
    /*
     * Shown when results are fetched from the server to prevent the user from tapping search or
     * more results multiple times.
     */
    private ProgressDialog progressDialog;
    /* The zero-relative index of the next result to retrieve (1 for starting a new search) */
    private int resultsIndex = 1;
    /*
     * The search query. Has to be stored so that if the user changed the text in the search box
     * and didn't pressed the search button in the keyboard, then the previously used search
     * expression will be taken when more results are fetched, and not the text currently in the box.
     */
    private String searchExpression = "";


    // Constructors:
    public PicturesList(Context context) {
        this(context, null, 0);
    }

    public PicturesList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PicturesList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    // Methods:
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.pictures_list_footer:
                // If the app is heavy on memory, clear the images cache:
                ActivityManager activityManager = (ActivityManager) getContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                if (memoryInfo.lowMemory) {
                    VolleyRequestManager.getInstance().clear();
                }

                // Show the progress dialog and fetch the next chunk of results:
                progressDialog.show();
                fetchResults();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            initiateNewSearch();
        }

        return false;
    }

    /*
     * Initializes this component
     *
     * @param ctx The context in which this component is initialized
     */
    @SuppressLint("InflateParams")
    private void init(final Context ctx) {
        // Set the layout:
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.pictures_list, this);

        // Capture Views:
        ListView picturesList = (ListView) findViewById(R.id.pictures_list_content_list);
        searchBox = (ClearableEditText) findViewById(R.id.pictures_list_search_box);

        // Set the action button of the search box to search button (displays a magnifier when the
        // keyboard is opened):
        searchBox.setImeAction(EditorInfo.IME_ACTION_SEARCH, this);

        // Set the adapter for the list:
        pictureListAdapter = new PictureListAdapter(ctx);
        picturesList.setAdapter(pictureListAdapter);

        // Set the footer to the list:
        footer = (Button) li.inflate(R.layout.pictures_list_footer, null);
        picturesList.addFooterView(footer);
        footer.setOnClickListener(this);

        // Create an instance of progress dialog to show when the results are being fetched:
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(ctx.getString(R.string.loading_prompt));

        // If a search has been previously executed, set the search expression to the search box:
        SharedPreferences prefs = ctx.getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        searchExpression = prefs.getString(KEY_SEARCH_EXP, "");
        searchBox.setText(searchExpression);

        // If the user executed a search, quit the app and reopened it, the app should resume to
        // the previous state:
        if (PictureDataManager.getInstance().getCount() > 0) {  // If 0, it means that the cache was
            // cleared, and we have to start anew (ignore the previously stored shared preferences)
            boolean isMoreResultsVisible = prefs.getBoolean(KEY_IS_FOOTER_VISIBLE, false);
            if (isMoreResultsVisible) {
                footer.setVisibility(View.VISIBLE);
            }
            resultsIndex = prefs.getInt(KEY_SEARCH_INDEX, 1);
        }

        // If a screen orientation took place when a picture was displayed in full screen mode,
        // redisplay the picture in full screen:
        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        pictureListAdapter.showPrevDialog(ctx);
                    }
                });
    }

    /*
     * Initiates a request to the server to fetch results that match the current search expression.
     */
    private void fetchResults() {
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

                        // Refresh the list to display the results:
                        pictureListAdapter.notifyDataSetChanged();

                        // Update the index of the last picture retrieved (the start index for the next chunk):
                        resultsIndex += numOfResults;

                        // Store to shared prefs whether the more results button will be visible the
                        // next time the app is started and the current index from which the search
                        // will continue when the user taps the more button:
                        boolean isFooterVisible = (numOfResults == CHUNK_SIZE);
                        SharedPreferences prefs =
                                getContext().getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_IS_FOOTER_VISIBLE, isFooterVisible);
                        editor.putInt(KEY_SEARCH_INDEX, resultsIndex);
                        editor.commit();

                        // Render the list footer visible / invisible depending on whether there are more
                        // results available:
                        if (isFooterVisible) {    // More results available
                            if (footer.getVisibility() == View.GONE) {
                                footer.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (footer.getVisibility() == View.VISIBLE) {
                                footer.setVisibility(View.GONE);
                            }
                        }

                        // Remove the progress dialog:
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error message:
                        new AlertDialog.Builder(getContext()).
                                setTitle(R.string.error).
                                setMessage(R.string.api_call_failure_message).
                                create().show();

                        // Remove the progress dialog:
                        progressDialog.dismiss();
                    }
                }
        );

        // Adding JsonObject request to request queue
        VolleyRequestManager.getInstance(getContext()).
                addToRequestQueue(getContext(), jsonObjectReq);
    }

    /*
     * Clears the previously displayed results and starts a new search
     */
    @SuppressLint("CommitPrefEdits")    // Store immediately to device storage so that if the user
    // exits the screen right after and resumes it, the data will be ready for use
    private void initiateNewSearch() {
        // Show the progress dialog:
        progressDialog.show();

        // Clear the pictures currently displayed:
        PictureDataManager.getInstance().clear();
        pictureListAdapter.notifyDataSetChanged();
        footer.setVisibility(View.GONE);

        // Launch a new search request:
        resultsIndex = 1;
        searchExpression = searchBox.getText();
        fetchResults();

        // Store to shared prefs the search expression so that it'll be automatically displayed:
        SharedPreferences prefs =
                getContext().getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SEARCH_EXP, searchExpression);
        editor.commit();
    }
}
