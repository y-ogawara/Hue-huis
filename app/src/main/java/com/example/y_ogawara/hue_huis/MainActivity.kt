package com.example.y_ogawara.hue_huis

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHLightState
import com.philips.lighting.hue.sdk.utilities.PHUtilities
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var  phHueSDK : PHHueSDK
    lateinit var realm : Realm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realm = Realm.getDefaultInstance()


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
    fun test1(v:View){
        realm.beginTransaction()
        //dbの準備
//        var model :HuisData = realm.createObject(HuisData::class.java)
        val model = realm.createObject(HuisData::class.java)
        //書き込みたいデータを作成
        model.name = "bbb"
        model.collarB = 1
        //トランザクション終了
        realm.commitTransaction()



    }
    fun test2(v:View){
        //検索用のクエリ作成
        var query : RealmQuery<HuisData> = realm.where(HuisData::class.java)

//        query.equalTo("name", "test");
//        query.or().equalTo("id", 2);


//        query.equalTo("name","bbb")
        //インスタンス生成し、その中にすべてのデータを入れる 配列で
        var results :RealmResults<HuisData> = query.findAll()

        //0番目を出力
        //text.setText(results.get(0).getName());

        //すべての値をログに出力
        for (test in results){
            println(test.keyCode)
            println("name   "+test.name)
            println(test.collarB)


            if (test.name == "Hue"){
                val cache = phHueSDK.selectedBridge.resourceCache
                val myLights = cache.allLights



                val bridge: PHBridge = PHHueSDK.getInstance().selectedBridge
                val lightState = PHLightState()
                val xy = PHUtilities.calculateXYFromRGB(test.collarR, test.collarG, test.collarB, myLights[1].modelNumber)
                lightState.x = xy[0]
                lightState.y = xy[1]
                lightState.brightness = test.brightness
                //lightState.hue = 50000
                bridge.updateLightState(myLights[1], lightState)
            }


        }



    }

    override fun onDestroy() {
        super.onDestroy()
        if(realm != null) {
            realm.close()
        }
    }


}
