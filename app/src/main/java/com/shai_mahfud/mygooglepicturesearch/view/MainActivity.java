/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.networking.VolleyRequestManager;

import io.fabric.sdk.android.Fabric;

/**
 * The main Activity of the application, which displays the list of photos and enables the user to
 * search for photos.
 *
 * @author Shai Mahfud
 */
public class MainActivity extends Activity {
    // Fields:
    private PicturesSearchMediator mediator;


    // Methods:
    @Override
    @SuppressLint("InflateParams")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Set layout:
        setContentView(R.layout.activity_main);

        // Capture Views:
        SearchEditText searchBox = (SearchEditText) findViewById(R.id.activity_main_search_box);
        PicturesList picturesList = (PicturesList) findViewById(R.id.activity_main_content_list);

        // Set the mediator for the pictures search components:
        mediator = new PicturesSearchMediator(this, searchBox, picturesList);
    }

    @Override
    public void finish() {
        super.finish();
        mediator.terminate();   // This may actually be redundant, because when the Activity is
                // finished, it's resources are detached and freed by the GC
        mediator = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // System memory is low and the app should free resources
        VolleyRequestManager.getInstance(this).clear();
    }
}
