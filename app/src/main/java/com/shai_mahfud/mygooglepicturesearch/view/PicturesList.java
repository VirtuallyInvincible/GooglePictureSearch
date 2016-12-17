/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.support.v7.widget.RecyclerView;

import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

/**
 * A list which displays pictures loaded from Google API
 *
 * @author Shai Mahfud
 */
public class PicturesList extends RecyclerView implements PicturesDisplayerInterface {
    // Inner classes:
    /*
     * Responsible for retrieving the next pictures chunk when the user scrolls to the bottom of the
     * list.
     */
    private class PagingListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastChunk) {
                if (((visibleItemCount + firstVisibleItemPosition) >= totalItemCount)
                        && (firstVisibleItemPosition >= 0)) {
                    // Get the next chunk of results:
                    if (moreResultsListener != null) {
                        isLoading = true;
                        moreResultsListener.fetchMoreResults();
                    }

                    // If the app is heavy on memory, clear the images cache:
                    ActivityManager activityManager = (ActivityManager) getContext().
                            getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    activityManager.getMemoryInfo(memoryInfo);
                    if (memoryInfo.lowMemory) {
                        VolleyRequestManager.getInstance(getContext()).clear();
                    }
                }
            }
        }
    }


    // Fields:
    /* The adapter for the pictures list */
    private PictureListAdapter pictureListAdapter;
    /* Mediates between the input */
    private OnMoreResultsListener moreResultsListener;
    /* The LayoutManager of this RecyclerView */
    private android.support.v7.widget.LinearLayoutManager layoutManager;
    /* Whether the last chunk has been reached (no more results) */
    private boolean isLastChunk = false;
    /* Whether a chunk of pictures is currently being loaded */
    private boolean isLoading = false;


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
    }

    @Override
    public void onResults(boolean isLastChunk) {
        isLoading = false;
        this.isLastChunk = isLastChunk;

        // Refresh the list to display the results:
        pictureListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError() {
        isLoading = false;
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
    private void init(final Context ctx) {
        // Set the adapter for the list:
        layoutManager = new LinearLayoutManager(ctx);
        setLayoutManager(layoutManager);
        pictureListAdapter = new PictureListAdapter(ctx);
        setAdapter(pictureListAdapter);
        setItemAnimator(new DefaultItemAnimator());

        // Add a scroll listener which will retrieve the next chunk when the user scrolls to the
        // bottom, if more data is available:
        addOnScrollListener(new PagingListener());

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
