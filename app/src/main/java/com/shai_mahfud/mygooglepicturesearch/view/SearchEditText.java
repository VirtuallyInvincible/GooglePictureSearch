/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A text box aimed at performing a search. It also enables the user to clear the text.
 *
 * @author Shai Mahfud
 */
public class SearchEditText extends ClearableEditText implements EditText.OnEditorActionListener,
        SearchComponentInterface {
    // Constants:
    /* Key for obtaining the shared preferences for this component */
    private static final String KEY_SEARCH_EDIT_TEXT_SHARED_PREFS = "search_edit_text_shared_prefs";
    /*
     * Key for search input of the user so that it'll be automatically displayed when the screen is
     * loaded
     */
    private static final String KEY_SEARCH_EXP = "search_expression";


    // Fields:
    /* Contains the listeners to be notified when a new search is executed via this SearchEditText */
    private List<OnSearchListener> listeners = new ArrayList<>();
    /* A mutex for the listeners data structure */
    private final Object listenersLock = new Object();
    /*
     * The search query. Has to be stored so that if the user changed the text in the search box
     * and didn't pressed the search button in the keyboard, then the previously used search
     * expression will be taken when more results are fetched, and not the text currently in the box.
     */
    private String searchExpression = "";


    // Constructors:
    public SearchEditText(Context context) {
        this(context, null, 0);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    // Methods:
    @Override
    @SuppressLint("CommitPrefEdits")    // Store immediately to device storage so that if the user
    // exits the screen right after and resumes it, the data will be ready for use
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            // Inform the listeners:
            searchExpression = getText().trim();
            synchronized (listenersLock) {
                for (OnSearchListener listener : listeners) {
                    listener.onSearch(searchExpression);
                }
            }

            // Store to shared prefs the search expression so that it'll be automatically displayed:
            SharedPreferences prefs = getContext().getSharedPreferences(
                    KEY_SEARCH_EDIT_TEXT_SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_SEARCH_EXP, searchExpression);
            editor.commit();
        }

        return false;
    }

    @Override
    public String getSearchExpression() {
        return searchExpression;
    }

    @Override
    public void terminate() {
        synchronized (listenersLock) {
            listeners.clear();
        }
    }

    /**
     * Adds a listener to be notified when a search is executed via this SearchEditText
     *
     * @param listener The listener to add
     */
    public void addOnSearchListener(OnSearchListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (listenersLock) {
            listeners.add(listener);
        }
    }

    /*
     * Initializes this component
     *
     * @param ctx The context in which this component is created
     */
    private void init(Context ctx) {
        // Set the action button of the search box to search button (displays a magnifier when the
        // keyboard is opened):
        super.setImeAction(EditorInfo.IME_ACTION_SEARCH, this);

        // If a search has been previously executed, set the search expression to the search box:
        SharedPreferences prefs = ctx.getSharedPreferences(
                KEY_SEARCH_EDIT_TEXT_SHARED_PREFS, Context.MODE_PRIVATE);
        searchExpression = prefs.getString(KEY_SEARCH_EXP, "");
        super.setText(searchExpression);
    }
}
