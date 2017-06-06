package com.example.y_ogawara.hue_huis

import android.app.SharedElementCallback
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.philips.lighting.hue.sdk.*
import com.philips.lighting.model.PHBridge
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHHueParsingError
import com.philips.lighting.hue.sdk.PHBridgeSearchManager
import android.R.id.edit
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.os.Handler


class BridgeRegisterActivity : AppCompatActivity(){
    lateinit var  phHueSDK : PHHueSDK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bridge_register)
        phHueSDK = PHHueSDK.getInstance()
        phHueSDK.appName = "testApp"     // e.g. phHueSDK.setAppName("QuickStartApp");
        phHueSDK.deviceName = android.os.Build.MODEL

        phHueSDK = PHHueSDK.create()  // or call .getInstance() effectively the same.
        phHueSDK.notificationManager.registerSDKListener(listener)
        val sm = phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
        sm.search(true, true)



    }
    private var listener :PHSDKListener = object : PHSDKListener {
        fun pref(ipAddress:String,username:String){
            val pref = getSharedPreferences("data",Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString("ipAddress", ipAddress)
            editor.putString("username", username)

            editor.apply()
        }


        //Bridgeを検索して見つかったときに呼ばれる
        override  fun onAccessPointsFound(accessPoint: List<PHAccessPoint>) {

            for (test in accessPoint){
                Log.d("accessPoint",test.bridgeId)
                Log.d("accessPoint",test.ipAddress)
                Log.d("accessPoint",test.macAddress)

                //Log.d("accessPoint",test.username)
            }
            //Listの0番目のBridgeに接続
            //後々ListViewに切り替え
            phHueSDK.connect(accessPoint[0])



        }


        override fun onCacheUpdated(arg0: List<Int>, bridge: PHBridge) {
            //Log.w(TAG, "On CacheUpdated")

        }

        //bridgeと接続が完了するとどのActivityからもここが呼ばれる
        //MainActivityにIntentすれば良さそう
       override fun onBridgeConnected(b: PHBridge, username: String) {

            phHueSDK.selectedBridge = b
            phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL.toLong())

            val ip = b.resourceCache.bridgeConfiguration.ipAddress
            //Log.d("username",username)
            //Log.d("ip",ip)
            pref(ip,username)

            //Intent
            startActivity(Intent(this@BridgeRegisterActivity, MainActivity::class.java))
        }





        override fun onAuthenticationRequired(accessPoint: PHAccessPoint?) {
            //Bridgeを接続状態に移す処理を行う
            phHueSDK.startPushlinkAuthentication(accessPoint)
            startActivity(Intent(this@BridgeRegisterActivity, PHPushlinkActivity::class.java))

            // Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
            // you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.


        }

        override fun onConnectionResumed(p0: PHBridge?) {
        }

        override fun onConnectionLost(p0: PHAccessPoint?) {
            // Here you would handle the loss of connection to your bridge.

        }


        override fun onParsingErrors(p0: MutableList<PHHueParsingError>?) {
            // Any JSON parsing errors are returned here.  Typically your program should never return these.
        }



        override fun onError(p0: Int, p1: String?) {
            // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found

        }


    }
}
