/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureData;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;
//import com.squareup.picasso.Picasso;

/**
 * The adapter for the pictures list
 *
 * @author Shai Mahfud
 */
class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> implements
        View.OnClickListener, View.OnTouchListener, PicturesDataAccessProvider {
    // Inner classes:
    class ViewHolder extends RecyclerView.ViewHolder {
        // Fields:
        /* Displays the title of the picture associated with this ViewHolder */
        private TextView title;
        /* Displays the picture associated with this ViewHolder */
        private ImageView picture;


        // Constructors:
        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.pictures_list_item_title);
            picture = (ImageView) itemView.findViewById(R.id.pictures_list_item_picture);
        }
    }


    // Fields:
    /*
     * The object responsible for managing the data of the pictures to be displayed
     */
    private PictureDataManager picturesDataManager;
    /* Enables interaction with the Volley library for networking */
    private VolleyRequestManager volleyRequestManager;
    /* A LayoutInflater instance for use throughout this class */
    private LayoutInflater li;
    /* The key to access the picture stored in cache associated with a record in the list */
    private int picKey;
    /* The key to access the parent View of a picture ImageView */
    private int parentKey;
    /*
     * A reference to the currently displayed picture dialog. Needed to terminate the dialog in case
     * of certain events, such as orientation change.
     */
    private PictureDialog pd;
    /*
     * The URL of the picture currently displayed in full screen mode. Stored to resume the dialog
     * after screen orientation change happens.
     */
    private static String selectedPicUrl;
    /* Whether a picture has already been animated upon touch */
    private boolean alreadyAnimated = false;


    // Constructors:
    /**
     * Instantiates this class
     *
     * @param ctx The context in which this adapter is instantiated
     */
    PictureListAdapter(Context ctx) {
        li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        picKey = R.id.pictures_list_item_picture;
        parentKey = R.id.activity_main_content_list;
        volleyRequestManager = new VolleyRequestManager(ctx);
    }


    // Methods:
    @Override
    public PictureDataManager getPictureDataManager() {
        return picturesDataManager;
    }

    @Override
    public void setPictureDataManager(PictureDataManager pictureDataManager) {
        this.picturesDataManager = pictureDataManager;
    }

    @SuppressLint("InflateParams")
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = li.inflate(R.layout.pictures_list_item, parent, false);
        ViewHolder vh = new ViewHolder(root);
        vh.picture.setTag(parentKey, parent);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, int position) {
        // Get the data:
        final PictureData data = picturesDataManager.getItem(position);

        // Set the data:
        vh.title.setText(data.getTitle());
        vh.picture.setImageBitmap(null);
        final String picLink = data.getPictureLink();
        vh.picture.setTag(picKey, picLink);
        volleyRequestManager.getPicture(picLink, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean loadedFromCache) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    vh.picture.setImageBitmap(bitmap);
                }

                // If the photo was loaded from cache, fetch it again from the API and then refresh
                // the UI:
                if (loadedFromCache) {
                    volleyRequestManager.refresh(data.getPictureLink(), this);
                    /*
                    Context ctx = ((ViewGroup) vh.picture.getTag(parentKey)).getContext();
                    Picasso.with(ctx)
                            .load(picLink)
                            .into(vh.picture);
                    */
                }
            }
        });

        // When the user taps the picture, the picture is displayed on the entire screen:
        vh.picture.setOnClickListener(this);

        // As long as the user touches the picture, the picture is enlarged a bit to convey the
        // event:
        vh.picture.setOnTouchListener(this);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return picturesDataManager.getCount();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.pictures_list_item_picture:
                if (v instanceof ImageView) {
                    BitmapDrawable pictureBD = (BitmapDrawable) ((ImageView) v).getDrawable();
                    if (pictureBD != null) {
                        Bitmap pictureBitmap = pictureBD.getBitmap();
                        if (pictureBitmap != null) {
                            ViewGroup parent = (ViewGroup) v.getTag(parentKey);
                            Context ctx = parent.getContext();
                            selectedPicUrl = (String) v.getTag(picKey);
                            showPictureFullScreen(ctx, pictureBitmap);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (!alreadyAnimated && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)) {
            alreadyAnimated = true;
            v.animate().scaleXBy(1.1f).setDuration(1000).start();
            v.animate().scaleYBy(1.1f).setDuration(1000).start();
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE ||
                action == MotionEvent.ACTION_UP) {
            alreadyAnimated = false;
            v.animate().cancel();
            v.setScaleX(1f);
            v.setScaleY(1f);
        }

        return false;
    }

    void terminate() {
        terminatePictureDialog();
    }

    /**
     * Displays a picture in full screen mode. Used to resume to full screen mode when screen
     * orientation change occurs.
     *
     * @param ctx The context in which this method is called
     */
    void showPrevDialog(final Context ctx) {
        if (selectedPicUrl != null) {
            terminatePictureDialog();
            volleyRequestManager.getPicture(selectedPicUrl, new ImageLoader.ImageListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response,
                                       boolean loadedFromCache) {
                    Bitmap pictureBitmap = response.getBitmap();
                    PictureListAdapter.this.showPictureFullScreen(ctx, pictureBitmap);
                }
            });
        }
    }

    /**
     *
     * @return The VolleyRequestManager instance used by this adapter
     */
    VolleyRequestManager getVolleyRequestManager() {
        return volleyRequestManager;
    }

    /*
     * Displays a picture in full screen mode.
     *
     * @param ctx The context in which this method is called
     * @param pictureBitmap The picture to display in full screen
     */
    private void showPictureFullScreen(Context ctx, Bitmap pictureBitmap) {
        this.pd = new PictureDialog(ctx, pictureBitmap);
        this.pd.show();
    }

    private void terminatePictureDialog() {
        if (this.pd != null) {
            this.pd.dismiss();
            this.pd = null;
        }
    }
}
