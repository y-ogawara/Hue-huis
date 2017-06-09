package com.example.y_ogawara.hue_huis

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHBridgeResourcesCache
import com.philips.lighting.model.PHLightState

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults

import java.lang.Integer.parseInt
import java.lang.Math.pow


class HueSettingActivity : AppCompatActivity() {

    lateinit var huisKeyCodeText: TextView
    lateinit var imageView: ImageView
    lateinit var spinner: Spinner
    lateinit var lightSpinner: Spinner

    //ColorPicker関連
    internal var selectColor: Int = 0
    internal var rgb: IntArray? = null
    lateinit var mColorPickerDialog: ColorPickerDialog
    lateinit var seekBar: SeekBar
    //spinner関連
    lateinit var spinnerStr: String
    lateinit var lightSpinnerStr:String
    lateinit var phHueSDK :PHHueSDK

    lateinit var realm : Realm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hue_setting)
        huisKeyCodeText = findViewById(R.id.huis_key) as TextView
        imageView = findViewById(R.id.imageView) as ImageView
        seekBar = findViewById(R.id.seekBar) as SeekBar

        realm = Realm.getDefaultInstance()

        spinnerSetting()
        lightSpinnerSet()
        seekBarSetUp()
    }

    fun colorPic(v: View) {
        mColorPickerDialog = ColorPickerDialog(this,
                ColorPickerDialog.OnColorChangedListener { color ->
                    selectColor = color
                    //ARGB形式の16進数文字列に変換
                    val hex = Integer.toHexString(selectColor)
                    //RGBのint型に変換
                    rgb = RGB(hex)
                    //float型のxyに変換
                    imageView.setBackgroundColor(Color.rgb(rgb!![0], rgb!![1], rgb!![2]))
                },
                Color.BLACK)
        mColorPickerDialog.show()
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        huisKeyCodeText.text = intent.getStringExtra("keyCode")

        //ここで前のデータをセット
        //On Off
        spinner.setSelection(intent.getIntExtra("spinner", 0))

        //あかるさ
        seekBar.progress = intent.getIntExtra("seekBar", 0)

        //色
        rgb = intent.getIntArrayExtra("rgb")
        if (rgb != null) {
            imageView.setBackgroundColor(Color.rgb(rgb!![0], rgb!![1], rgb!![2]))
        }
        //hueId
        lightSpinner.setSelection(intent.getIntExtra("lightSpinner", 0))

    }

    fun huisKey(v: View) {
        val intent = Intent(this, KeyInputActivity::class.java)
        //on off
        if (spinnerStr.contentEquals("on")) {
            intent.putExtra("spinner", 0)
        } else {
            intent.putExtra("spinner", 1)
        }
        //明るさ
        intent.putExtra("seekBar", seekBar.progress)
        //色
        intent.putExtra("rgb", rgb)

        //hueId番号
        intent.putExtra("lightSpinner", lightSpinner.selectedItemPosition)

        //activity名
        intent.putExtra("ActivityName", "HueSettingActivity")
        startActivity(intent)
    }

    fun save(v: View) {

        if (huisKeyCodeText.text.toString() == ""){
            Toast.makeText(this,"huisのボタンが選択されていません",LENGTH_SHORT).show()

        }
        else if (lightSpinner.selectedItem == ""){
            Toast.makeText(this,"選択できる電球がありません",LENGTH_SHORT).show()
        }

        else {
            realm.beginTransaction()


            // 書き込み開始
            //dbの準備
            if (rgb == null) {
                rgb = intArrayOf(0,0,0)
            }
            val data = HuisData()
            //書き込みたいデータを作成
            data.name = "Hue"
            data.keyCode = huisKeyCodeText.text.toString()
            data.lightState = spinnerStr
            data.collarR = rgb!![0]
            data.collarG = rgb!![1]
            data.collarB = rgb!![2]
            data.brightness = seekBar.progress
            data.hueId = lightSpinnerStr

            // プライマリーキーが同じならアップデート
            realm.copyToRealmOrUpdate(data)
            //トランザクション終了
            realm.commitTransaction()
            realm.close()



            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }

    //戻るボタンを押したときの動作を指定
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    internal fun RGB(str: String): IntArray {
        val rgb = IntArray(3)
        if (str.length == 8) {
            rgb[0] = parseInt(str.substring(2, 4), 16)
            rgb[1] = parseInt(str.substring(4, 6), 16)
            rgb[2] = parseInt(str.substring(6, 8), 16)
        }
        return rgb
    }

    internal fun spinnerSetting() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // アイテムを追加します
        adapter.add("on")
        adapter.add("off")
        spinner = findViewById(R.id.spinner) as Spinner
        // アダプターを設定します
        spinner.adapter = adapter
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                val spinner = parent as Spinner
                // 選択されたアイテムを取得します
                spinnerStr = spinner.selectedItem as String
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }
    fun lightSpinnerSet(){
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        phHueSDK = PHHueSDK.getInstance()
        connectBridge()


        try {
            var cache = phHueSDK.selectedBridge.resourceCache
            // hueのlightの数を取得
            val myLights = cache.allLights
            // アイテムを追加します
            for (myLight in myLights){
                adapter.add(myLight.name)
            }

        }catch (e: NullPointerException){
            Toast.makeText(this,"bridgeが登録されていません",LENGTH_SHORT).show()
            finish()
        }

        lightSpinner = findViewById(R.id.lightSpinner) as Spinner
        // アダプターを設定します
        lightSpinner.adapter = adapter
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        lightSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                val spinner = parent as Spinner
                // 選択されたアイテムを取得します
                lightSpinnerStr = spinner.selectedItem as String
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }
    fun connectBridge(){
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val ip = pref.getString("ipAddress","null")
        val username = pref.getString("username","null")
        val accessPoint: PHAccessPoint = PHAccessPoint()
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

    internal fun seekBarSetUp() {
        seekBar.max = 255
        seekBar.progress = 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // トラッキング開始時に呼び出されます
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Log.v("onStartTrackingTouch()",
                        seekBar.progress.toString())
            }

            // トラッキング中に呼び出されます
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
                Log.v("onProgressChanged()",
                        progress.toString() + ", " + fromTouch.toString())
            }

            // トラッキング終了時に呼び出されます
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.v("onStopTrackingTouch()",
                        seekBar.progress.toString())
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
