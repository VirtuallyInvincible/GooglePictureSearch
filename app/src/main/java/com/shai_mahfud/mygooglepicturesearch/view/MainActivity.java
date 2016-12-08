/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

import org.json.JSONObject;

/**
 * The main Activity of the application, which displays the list of photos and enables the user to
 * search for photos.
 *
 * @author Shai Mahfud
 */
public class MainActivity extends Activity implements View.OnClickListener,
        EditText.OnEditorActionListener, TextWatcher {
    // Constants:
    /**
     * The desired size of each chunk retrieved from the server. Note that Google API doesn't allow
     * more than 10 results per request. Requesting more than 10 yields error code 400.
     */
    public static final int CHUNK_SIZE = 10;
    /*
     * According to the example in your exercise, this is the position of the first item in the list
     * of results.
     */
    private static final int START_INDEX = 1;
    /*
     * The URL of the search API.
     */
    private static final String GOOGLE_SEARCH_PHOTO_API = "https://www.googleapis.com/customsearch/" +
            "v1?key=AIzaSyBW8wxhgNKU36Q_WPqJciAa9no2mqtcng4&cx=005800383728131713214:5jocmduwqum" +
            "&searchType=image&num=" + CHUNK_SIZE + "&start=%d&q=%s";
    /* Key for obtaining SharedPreferences */
    private static final String KEY_SHARED_PREFS = "shared_prefs";
    /* Key for obtaining whether the more results footer should be visible */
    private static final String KEY_IS_FOOTER_VISIBLE = "is_footer_visible";
    /*
     * Key for search input of the user so that it'll be automatically displayed when the screen is
     * loaded
     */
    private static final String KEY_SEARCH_EXP = "search_expression";
    /* Key for current search index */
    private static final String KEY_SEARCH_INDEX = "search_index";


    // Fields:
    /* The field where the user types the expression to search */
    private EditText searchBox;
    /* Enables to clear the text in the search box */
    private ImageView searchBoxClearButton;
    /* The adapter for the pictures list */
    private PictureListAdapter pictureListAdapter;
    /*
     * Enables the user to query for more results once the first chunk of data has arrived and
     * there are more results available.
     */
    private Button footer;
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


    // Methods:
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout:
        setContentView(R.layout.activity_main);

        // Capture Views:
        searchBox = (EditText) findViewById(R.id.activity_main_search_box);
        searchBoxClearButton = (ImageView) findViewById(R.id.activity_main_clear_search_button);
        ListView contentList = (ListView) findViewById(R.id.activity_main_content_list);

        // Once the user starts typing, render the clear button visible:
        searchBox.addTextChangedListener(this);

        // If the user taps the clear button, remove the search text:
        searchBoxClearButton.setOnClickListener(this);

        // Listen for attempts to start new searches:
        searchBox.setOnEditorActionListener(this);

        // Set the adapter for the list:
        pictureListAdapter = new PictureListAdapter(getLayoutInflater());
        contentList.setAdapter(pictureListAdapter);

        // Set the footer to the list:
        footer = (Button) getLayoutInflater().
                inflate(R.layout.activity_main_list_footer, null);
        contentList.addFooterView(footer);
        footer.setOnClickListener(this);

        // If the user executed a search, quit the app and reopened it, the app should resume to
        // the previous state:
        if (PictureDataManager.getInstance().getCount() > 0) {  // If 0, it means that the cache was
                // cleared, and we have to start anew (ignore the previously stored shared
                // preferences)
            SharedPreferences prefs = getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
            boolean isMoreResultsVisible = prefs.getBoolean(KEY_IS_FOOTER_VISIBLE, false);
            if (isMoreResultsVisible) {
                footer.setVisibility(View.VISIBLE);
            }
            searchExpression = prefs.getString(KEY_SEARCH_EXP, "");
            searchBoxClearButton.setVisibility(searchExpression.isEmpty() ? View.GONE : View.VISIBLE);
            searchBox.setText(searchExpression);
            resultsIndex = prefs.getInt(KEY_SEARCH_INDEX, 1);
        }

        // Create an instance of progress dialog to show when the results are being fetched:
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading_prompt));
    }

    @Override
    public void finish() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.finish();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            initiateNewSearch();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.activity_main_list_footer:
                // Show the progress dialog and fetch the next chunk of results:
                progressDialog.show();
                fetchResults();
                break;
            case R.id.activity_main_clear_search_button:
                searchBox.getText().clear();
                searchBoxClearButton.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (searchBoxClearButton.getVisibility() != View.VISIBLE) {
            searchBoxClearButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    /*
     * Initiates a request to the server to fetch results that match the current search expression.
     */
    private void fetchResults() {
        String url = String.format(GOOGLE_SEARCH_PHOTO_API, resultsIndex, searchExpression);
        System.out.println("url = " + url);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Update the cache:
                        int numOfResults = PictureDataManager.getInstance().addNewResults(response);

                        // Refresh the list to display the results:
                        pictureListAdapter.notifyDataSetChanged();

                        // Update the index of the last picture retrieved (the start index for the next chunk):
                        resultsIndex += numOfResults;

                        // Store to shared prefs whether the more results button will be visible the
                        // next time the app is started. Also store the search expression so that it'll
                        // be automatically displayed and the current index of search:
                        boolean isFooterVisible = (numOfResults == CHUNK_SIZE);
                        SharedPreferences prefs =
                                getSharedPreferences(KEY_SHARED_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(KEY_IS_FOOTER_VISIBLE, isFooterVisible);
                        editor.putString(KEY_SEARCH_EXP, searchExpression);
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
                        new AlertDialog.Builder(MainActivity.this).
                                setTitle(R.string.error).
                                setMessage(R.string.api_call_failure_message).
                                create().show();

                        // Remove the progress dialog:
                        progressDialog.dismiss();
                    }
                }
        );

        // Adding JsonObject request to request queue
        VolleyRequestManager.getInstance(getApplicationContext()).
                addToRequestQueue(this, jsonObjectReq);
    }

    /*
     * Clears the previously displayed results and starts a new search
     */
    private void initiateNewSearch() {
        // Show the progress dialog:
        progressDialog.show();

        // Clear the pictures currently displayed:
        PictureDataManager.getInstance().clear();
        pictureListAdapter.notifyDataSetChanged();
        footer.setVisibility(View.GONE);

        // Launch a new search request:
        resultsIndex = 1;
        searchExpression = searchBox.getText().toString().trim();
        fetchResults();
    }
}
