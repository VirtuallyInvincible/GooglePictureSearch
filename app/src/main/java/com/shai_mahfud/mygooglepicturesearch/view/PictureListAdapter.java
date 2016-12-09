/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureData;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

/**
 * The adapter for the pictures list
 *
 * @author Shai Mahfud
 */
class PictureListAdapter extends BaseAdapter implements View.OnClickListener,
        DialogInterface.OnDismissListener {
    // Inner classes:
    private class ViewHolder {
        // Fields:
        /* Displays the title of the picture associated with this ViewHolder */
        private TextView title;
        /* Displays the picture associated with this ViewHolder */
        private ImageView picture;


        // Constructors:
        /*
         * Instantiates this class
         *
         * @param convertView The root of the View hierarchy of this ViewHolder
         */
        private ViewHolder(View convertView) {
            title = (TextView) convertView.findViewById(R.id.pictures_list_item_title);
            picture = (ImageView) convertView.findViewById(R.id.pictures_list_item_picture);
        }
    }


    // Fields:
    /* An instance of LayoutInflater for inflating the items of the list from XML */
    private LayoutInflater li;
    /* The key to access the picture stored in cache associated with a record in the list */
    private int picKey;
    /* The key to access the parent View of a picture ImageView */
    private int parentKey;
    /*
     * The URL of the picture currently displayed in full screen mode. Stored to resume the dialog
     * after screen orientation change happens.
     */
    private static String selectedPicUrl;


    // Constructors:
    /**
     * Instantiates this class
     *
     * @param ctx The context in which this adapter is created
     */
    PictureListAdapter(Context ctx) {
        this.li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        picKey = R.id.pictures_list_item_picture;
        parentKey = R.id.pictures_list_content_list;
    }


    // Methods:
    @Override
    public int getCount() {
        return PictureDataManager.getInstance().getCount();
    }

    @Override
    public PictureData getItem(int position) {
        return PictureDataManager.getInstance().getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        // Set the layout:
        final ViewHolder[] vh = new ViewHolder[1];
        if (convertView == null) {
            convertView = li.inflate(R.layout.pictures_list_item, null);
            vh[0] = new ViewHolder(convertView);
            convertView.setTag(vh[0]);
        } else {
            vh[0] = (ViewHolder) convertView.getTag();
        }

        // Set the data:
        final PictureData data = getItem(position);
        vh[0].title.setText(data.getTitle());
        vh[0].picture.setImageBitmap(null);
        String picLink = data.getPictureLink();
        vh[0].picture.setTag(picKey, picLink);
        VolleyRequestManager.getInstance().getPicture(picLink, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean loadedFromCache) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    vh[0].picture.setImageBitmap(bitmap);
                }

                // If the photo was loaded from cache, fetch it again from the API and then refresh
                // the UI:
                if (loadedFromCache) {
                    VolleyRequestManager.getInstance().refresh(data.getPictureLink(), this);
                }
            }
        });

        // Disable click events on the list rows:
        convertView.setFocusable(true);

        // When the user taps the picture, the picture is displayed on the entire screen:
        vh[0].picture.setTag(parentKey, parent);
        vh[0].picture.setOnClickListener(this);

        return convertView;
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
    public void onDismiss(DialogInterface dialogInterface) {
        selectedPicUrl = null;
    }

    /**
     * Displays a picture in full screen mode. Used to resume to full screen mode when screen
     * orientation change occurs.
     *
     * @param ctx The context in which this method is called
     */
    void showPrevDialog(final Context ctx) {
        if (selectedPicUrl != null) {
            VolleyRequestManager.getInstance().getPicture(selectedPicUrl, new ImageLoader.ImageListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean loadedFromCache) {
                    Bitmap pictureBitmap = response.getBitmap();
                    PictureListAdapter.this.showPictureFullScreen(ctx, pictureBitmap);
                }
            });
        }
    }

    /*
     * Displays a picture in full screen mode.
     *
     * @param ctx The context in which this method is called
     * @param pictureBitmap The picture to display in full screen
     */
    private void showPictureFullScreen(Context ctx, Bitmap pictureBitmap) {
        PictureDialog pd = new PictureDialog(ctx, pictureBitmap);
        pd.setOnDismissListener(PictureListAdapter.this);
        pd.show();
    }
}
