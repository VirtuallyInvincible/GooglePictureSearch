/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shai_mahfud.mygooglepicturesearch.R;

import org.greenrobot.eventbus.EventBus;

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
    /* The time that should elapse to perform a search after a text change event */
    private static final int DELAY = 1000;


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
    /* Handles searches after the user stopped typing */
    private Handler onFinishTextingHandler = new Handler();
    private Runnable onFinishTextingRunnable = new Runnable() {
        public void run() {
            SearchEditText.super.dispatchOnEditorAction(EditorInfo.IME_ACTION_SEARCH);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openKeyboard();
                    SearchEditText.super.focus();
                }
            }, DELAY);
        }
    };


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

            closeKeyboard();
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

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        onFinishTextingHandler.removeCallbacks(onFinishTextingRunnable);
        super.onTextChanged(charSequence, i, i1, i2);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // As soon as the user stops typing, run a search:
        onFinishTextingHandler.postDelayed(onFinishTextingRunnable, DELAY);

        super.afterTextChanged(editable);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.clearable_edit_text_speak_button:
                EventBus.getDefault().post(new SpeechInputEvent());
                break;
            default:
                super.onClick(v);
                break;
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
        ImageButton speakButton = (ImageButton) findViewById(R.id.clearable_edit_text_speak_button);
        speakButton.setOnClickListener(this);

        // Set the action button of the search box to search button (displays a magnifier when the
        // keyboard is opened):
        super.setImeAction(EditorInfo.IME_ACTION_SEARCH, this);

        // If a search has been previously executed, set the search expression to the search box:
        SharedPreferences prefs = ctx.getSharedPreferences(
                KEY_SEARCH_EDIT_TEXT_SHARED_PREFS, Context.MODE_PRIVATE);
        searchExpression = prefs.getString(KEY_SEARCH_EXP, "");
        super.setText(searchExpression);
    }

    private void closeKeyboard() {
        InputMethodManager imm = getInputMethodManager();
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void openKeyboard() {
        InputMethodManager imm = getInputMethodManager();
        if (imm == null) {
            return;
        }
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private InputMethodManager getInputMethodManager() {
        Context ctx = getContext();
        if (ctx == null) {
            return null;
        }
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        return imm;
    }
}
