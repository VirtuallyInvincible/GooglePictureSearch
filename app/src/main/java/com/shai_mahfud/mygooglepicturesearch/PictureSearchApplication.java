/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * The file that gets executed when the application is started.
 *
 * @author Shai Mahfud
 */
public class PictureSearchApplication extends Application {//extends MultiDexApplication
    @Override
    public void onCreate() {
        super.onCreate();

        // Install LeakCanary if the app is in debug mode:
        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }
    }
}
