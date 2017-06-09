package com.example.y_ogawara.hue_huis

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHLightState
import com.philips.lighting.hue.sdk.utilities.PHUtilities
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults


class MainActivity : AppCompatActivity() {
    lateinit var phHueSDK: PHHueSDK
    lateinit var realm: Realm
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

    fun setBridge(v: View) {
        val intent = Intent(this, BridgeRegisterActivity::class.java)
        startActivity(intent)

    }

    //以前に接続したBridgeがある場合はそこに接続する
    fun connectBridge() {
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val ip = pref.getString("ipAddress", "null")
        val username = pref.getString("username", "null")
        val accessPoint: PHAccessPoint = PHAccessPoint()
        accessPoint.ipAddress = ip
        accessPoint.username = username

        if (ip != "null" || username != "null") {
            //保存されている接続先に接続しているかどうか
            if (!phHueSDK.isAccessPointConnected(accessPoint)) {
                phHueSDK.connect(accessPoint)
            } else {
                println("nazo")
            }
        } else {
            Log.d("connect", "どちらかに値が入っていません")
            return
        }

    }


    fun hueIntent(v: View) {
        val intent = Intent(this, HueSettingActivity::class.java)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        // 押したときと離したとき 2回認識されないように
        if (e.action == KeyEvent.ACTION_UP) {
            // キーコードを表示する
            Log.d("HUIS", "KeyCode:" + e.keyCode)
            lightSet(e.keyCode.toString())
        }
        return false
        // 本来のイベントを返す (下をやらないと戻るボタンとか動かない)
        // return super.dispatchKeyEvent(e)
    }


    //TODO realmから全データを取得してUIで検索をかけているので、realmで検索をかけるように書き直す
    fun lightSet(keyCode: String) {
        //検索用のクエリ作成
        val query: RealmQuery<HuisData> = realm.where(HuisData::class.java)
        //インスタンス生成し、その中にすべてのデータを入れる 配列で
        val results: RealmResults<HuisData> = query.findAll()

        //すべての値をログに出力
        for (test in results) {
            // Hueを操作したいとき
            if (test.name == "Hue") {
                // realmに登録されたkeyとHuisのkeyが同じ時
                if (test.keyCode == keyCode) {
                    val cache = phHueSDK.selectedBridge.resourceCache
                    // bridgeに接続されているすべての電球を取得
                    val myLights = cache.allLights
                    var count = 0
                    // 電球の数だけ回す
                    while (count < myLights.size) {
                        // 電球の名前と、realmに保存されている電球の名前が一致したら抜ける
                        if (myLights[count].name == test.hueId) {
                            break
                        }
                        count++
                    }
                    val bridge: PHBridge = PHHueSDK.getInstance().selectedBridge
                    val lightState = PHLightState()
                    // realmに入っている値が
                    if (test.lightState == "off") {
                        lightState.isOn = false
                    } else {
                        //電球をONに
                        lightState.isOn = true
                        // RGBをxyに変換
                        val xy = PHUtilities.calculateXYFromRGB(test.collarR, test.collarG, test.collarB, myLights[count].modelNumber)
                        lightState.x = xy[0]
                        lightState.y = xy[1]
                        // 明るさを設定
                        lightState.brightness = test.brightness
                    }
                    // bridgeに書き込み
                    bridge.updateLightState(myLights[count], lightState)
                    println("")
                }
            }

        }


    }

}
