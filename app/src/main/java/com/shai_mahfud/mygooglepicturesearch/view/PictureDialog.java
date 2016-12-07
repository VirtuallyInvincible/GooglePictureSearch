/**
 * All rights reserved to Shai Mahfud
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.shai_mahfud.mygooglepicturesearch.R;

/**
 * Displays a picture on the entire screen
 *
 * @author Shai Mahfud
 */
public class PictureDialog extends Dialog implements View.OnClickListener {
    // Constructors:
    /**
     * Initializes this component
     *
     * @param ctx The context in which this dialog is created
     * @param pictureBitmap The bitmap to be displayed in this dialog
     */
    public PictureDialog(Context ctx, Bitmap pictureBitmap) {
        super(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // Set the layout:
        setContentView(R.layout.dialog_picture);

        // Capture Views:
        ImageView picture = (ImageView) findViewById(R.id.dialog_picture_picture);

        // If the user taps anywhere within the picture, this dialog is closed:
        picture.setOnClickListener(this);

        // Set the picture:
        picture.setImageBitmap(pictureBitmap);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.dialog_picture_picture:
                dismiss();
                break;
            default:
                break;
        }
    }
}
