package com.example.y_ogawara.hue_huis

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.philips.lighting.hue.listener.PHLightListener
import com.philips.lighting.hue.sdk.PHAccessPoint
import kotlinx.android.synthetic.main.activity_main.*
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHLight
import com.philips.lighting.model.PHLightState
import com.philips.lighting.hue.sdk.utilities.PHUtilities




class MainActivity : AppCompatActivity() {
    lateinit var  phHueSDK : PHHueSDK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        phHueSDK = PHHueSDK.getInstance()
        phHueSDK.appName = "testApp"   // e.g. phHueSDK.setAppName("QuickStartApp");
        phHueSDK.deviceName = "android.os.Build.MODEL"  // e.g. If you are programming for Android: phHueSDK.setDeviceName(android.os.Build.MODEL);

    }
    override fun onResume() {
        super.onResume()
        connectBridge()
    }

    fun setBridge(v:View){
        val intent = Intent(this, BridgeRegisterActivity::class.java)
        startActivity(intent)

    }
    //以前に接続したBridgeがある場合はそこに接続する
    fun connectBridge(){
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val ip = pref.getString("ipAddress","null")
        val username = pref.getString("username","null")
        val accessPoint:PHAccessPoint  = PHAccessPoint()
        accessPoint.ipAddress = ip
        accessPoint.username = username

        if (ip != "null" ||username != "null"){
            //保存されている接続先に接続しているかどうか
            if (!phHueSDK.isAccessPointConnected(accessPoint)){
                phHueSDK.connect(accessPoint)
            }else{
                println("nazo")
            }
        }else{
            Log.d("connect","どちらかに値が入っていません")
            return
        }

    }
    fun collarChange(v:View) {
        val cache = phHueSDK.selectedBridge.resourceCache
// And now you can get any resource you want, for example:
        val myLights = cache.allLights

        val bridge: PHBridge = PHHueSDK.getInstance().selectedBridge

        val lightState = PHLightState()

        val xy = PHUtilities.calculateXYFromRGB(255, 0, 255, myLights[1].modelNumber)
        lightState.x = xy[0]
        lightState.y = xy[1]
        //lightState.hue = 50000
        bridge.updateLightState(myLights[1], lightState)
    }
    fun hueIntent(v:View){
        val intent = Intent(this, HueSettingActivity::class.java)
        startActivity(intent)
    }
}
