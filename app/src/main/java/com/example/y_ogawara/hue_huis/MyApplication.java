package com.example.y_ogawara.hue_huis;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by y-ogawara on 17/06/06.
 */

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // Realm設定ここから
        Realm.init(this);
//        RealmConfiguration config = new RealmConfiguration.Builder().build();
//        Realm.setDefaultConfiguration(config);
        // Realm設定ここまで
    }
}