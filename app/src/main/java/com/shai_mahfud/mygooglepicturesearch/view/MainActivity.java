/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.shai_mahfud.mygooglepicturesearch.R;
import com.shai_mahfud.mygooglepicturesearch.model.PictureDataManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * The main Activity of the application, which displays the list of photos and enables the user to
 * search for photos.
 *
 * @author Shai Mahfud
 */
public class MainActivity extends Activity {
    private static final int REQ_CODE_SPEECH_INPUT = 1;


    private PicturesSearchMediator mediator;
    private SearchEditText searchBox;


    @Override
    @SuppressLint("InflateParams")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Set layout:
        setContentView(R.layout.activity_main);

        // Capture Views:
        searchBox = (SearchEditText) findViewById(R.id.activity_main_search_box);
        PicturesList picturesList = (PicturesList) findViewById(R.id.activity_main_content_list);

        // Set the mediator for the pictures search components:
        mediator = new PicturesSearchMediator.Builder(this).
                setInputComponent(searchBox).
                setDisplayerComponent(picturesList, new PictureDataManager(this)).
                build();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mediator.onOrientationChange(this);   // Close the picture dialog if it was previously displayed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result != null && !result.isEmpty() && searchBox != null) {
                        searchBox.setText(result.get(0));
                    }
                }
                break;
            }

        }
    }

    @Subscribe
    public void onEvent(SpeechInputEvent event) {
        promptSpeechInput();
    }

    @Override
    public void finish() {
        super.finish();
        mediator.terminate();   // This may actually be redundant, because when the Activity is
                // finished, it's resources are detached and freed by the GC
        mediator = null;
        EventBus.getDefault().unregister(this);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }
}
