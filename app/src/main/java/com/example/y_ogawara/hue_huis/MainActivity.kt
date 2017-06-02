package com.example.y_ogawara.hue_huis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import com.philips.lighting.hue.sdk.PHHueSDK



class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val phHueSDK = PHHueSDK.getInstance()
        phHueSDK.appName = "testApp"   // e.g. phHueSDK.setAppName("QuickStartApp");
        phHueSDK.deviceName = "android.os.Build.MODEL"  // e.g. If you are programming for Android: phHueSDK.setDeviceName(android.os.Build.MODEL);
    }

}
