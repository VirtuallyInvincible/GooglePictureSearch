/**
 * All rights reserved to Applicat Technologies Ltd.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;

import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

/**
 * A list which displays pictures loaded from Google API
 *
 * @author Shai Mahfud
 */
public class PicturesList extends ListView implements View.OnClickListener,
        PicturesDisplayerInterface {
    // Constants:
    /* Key for obtaining the shared preferences for this component */
    private static final String KEY_PICTURES_LIST_SHARED_PREFS = "pictures_list_shared_prefs";
    /* Key for obtaining whether the more results footer should be visible */
    private static final String KEY_IS_FOOTER_VISIBLE = "is_footer_visible";


    // Fields:
    /*
     * Enables the user to query for more results once the first chunk of data has arrived and
     * there are more results available.
     */
    private Button footer;
    /* The adapter for the pictures list */
    private PictureListAdapter pictureListAdapter;
    /* Mediates between the input */
    private OnMoreResultsListener moreResultsListener;


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

                // Get the next chunk of results:
                if (moreResultsListener != null) {
                    moreResultsListener.fetchMoreResults();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void setMoreResultsListener(OnMoreResultsListener listener) {
        if (listener == null) {
            return;
        }

        this.moreResultsListener = listener;
    }

    @Override
    public void onNewSearch(String searchExpression) {
        // Clear the pictures currently displayed:
        PictureDataManager.getInstance().clear();
        pictureListAdapter.notifyDataSetChanged();

        // Render the footer invisible:
        footer.setVisibility(View.GONE);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onResults(boolean isLastChunk) {
        // Refresh the list to display the results:
        pictureListAdapter.notifyDataSetChanged();

        // Store to shared prefs whether the more results button will be visible the
        // next time the app is started:
        boolean isFooterVisible = !isLastChunk;
        SharedPreferences prefs = getContext().getSharedPreferences(
                KEY_PICTURES_LIST_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_FOOTER_VISIBLE, isFooterVisible);
        editor.commit();

        // Render the list footer visible / invisible depending on whether there are
        // more results available:
        if (isFooterVisible) {    // More results available
            if (footer.getVisibility() == View.GONE) {
                footer.setVisibility(View.VISIBLE);
            }
        } else {
            if (footer.getVisibility() == View.VISIBLE) {
                footer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void terminate() {
        moreResultsListener = null;
    }

    /*
     * Initializes this component
     *
     * @param ctx The context in which this component is initialized
     */
    @SuppressLint("InflateParams")
    private void init(final Context ctx) {
        // Set the adapter for the list:
        pictureListAdapter = new PictureListAdapter(ctx);
        setAdapter(pictureListAdapter);

        // Set the footer to the list:
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer = (Button) li.inflate(R.layout.pictures_list_footer, null);
        addFooterView(footer);
        footer.setOnClickListener(this);

        // If the user executed a search, quit the app and reopened it, the app should resume to
        // the previous state:
        if (PictureDataManager.getInstance().getCount() > 0) {  // If 0, it means that the cache was
            // cleared, and we have to start anew (ignore the previously stored shared preferences)
            SharedPreferences prefs = ctx.getSharedPreferences(
                    KEY_PICTURES_LIST_SHARED_PREFS, Context.MODE_PRIVATE);
            boolean isMoreResultsVisible = prefs.getBoolean(KEY_IS_FOOTER_VISIBLE, false);
            if (isMoreResultsVisible) {
                footer.setVisibility(View.VISIBLE);
            }
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
}
