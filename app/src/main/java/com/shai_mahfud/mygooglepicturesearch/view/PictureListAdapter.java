/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.content.Context;
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
public class PictureListAdapter extends BaseAdapter implements View.OnClickListener {
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
            title = (TextView) convertView.findViewById(R.id.activity_main_list_item_title);
            picture = (ImageView) convertView.findViewById(R.id.activity_main_list_item_picture);
        }
    }


    // Fields:
    /* An instance of LayoutInflater for inflating the items of the list from XML */
    private LayoutInflater li;


    // Constructors:
    /**
     * Instantiates this class
     *
     * @param li An instance of LayoutInflater for inflating the items of the list from XML
     */
    PictureListAdapter(LayoutInflater li) {
        this.li = li;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // Set the layout:
        final ViewHolder[] vh = new ViewHolder[1];
        if (convertView == null) {
            convertView = li.inflate(R.layout.activity_main_list_item, null);
            vh[0] = new ViewHolder(convertView);
            convertView.setTag(vh[0]);
        } else {
            vh[0] = (ViewHolder) convertView.getTag();
        }

        // Set the data:
        final PictureData data = getItem(position);
        vh[0].title.setText(data.getTitle());
        vh[0].picture.setImageBitmap(null);
        VolleyRequestManager.getInstance().getPicture(data.getPictureLink(),
                new ImageLoader.ImageListener() {
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
        vh[0].picture.setTag(parent);
        vh[0].picture.setOnClickListener(this);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.activity_main_list_item_picture:
                if (v instanceof ImageView) {
                    BitmapDrawable pictureBD = (BitmapDrawable) ((ImageView) v).getDrawable();
                    if (pictureBD != null) {
                        Bitmap pictureBitmap = pictureBD.getBitmap();
                        if (pictureBitmap != null) {
                            ViewGroup parent = (ViewGroup) v.getTag();
                            Context ctx = parent.getContext();
                            new PictureDialog(ctx, pictureBitmap).show();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
