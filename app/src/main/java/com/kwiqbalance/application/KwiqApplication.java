package com.kwiqbalance.application;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.kwiqbalance.R;

/**
 * Created by Ankit Chouhan on 27-12-2017.
 */

public class KwiqApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this,getString(R.string.admob_app_id));
    }
}
