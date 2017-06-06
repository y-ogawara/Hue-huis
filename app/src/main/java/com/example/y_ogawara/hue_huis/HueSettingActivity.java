package com.example.y_ogawara.hue_huis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;


public class HueSettingActivity extends AppCompatActivity {

    TextView huisKeyCodeText;
    ImageView imageView;
    Spinner spinner;
    EditText hueIdText;
    boolean flag[] = new boolean[4];

    //デバッグ用
//    TextView picXText;
//    TextView picYText;
    //ColorPicker関連

    int selectColor;
    float[] hsv = new float[3];
    float picX;
    float picY;
    int[] rgb;
    ColorPickerDialog mColorPickerDialog;
    SeekBar seekBar;
    //spinner関連
    String spinnerStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hue_setting);
        huisKeyCodeText =(TextView) findViewById(R.id.huis_key);
        imageView = (ImageView)findViewById(R.id.imageView);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        hueIdText = (EditText) findViewById(R.id.hueIdText);

        spinnerSetting();
        seekBarSetUp();
    }
    public void colorPic(View v){
        mColorPickerDialog = new ColorPickerDialog(this,
                new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        selectColor = color;
                        //ARGB形式の16進数文字列に変換
                        String hex = Integer.toHexString(selectColor);
                        //RGBのint型に変換
                        rgb = RGB(hex);
                        //float型のxyに変換
                        //xy(((float)rgb[0])/255,((float)rgb[1])/255,((float)rgb[2])/255);
                        imageView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
//                        picXText.setText(String.valueOf(picX));
//                        picYText.setText(String.valueOf(picY));
                    }
                },
                Color.BLACK);
        mColorPickerDialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        huisKeyCodeText.setText(intent.getStringExtra("keyCode"));

        //ここで前のデータをセット
        //On Off
        spinner.setSelection(intent.getIntExtra("spinner",0));

        //あかるさ
        seekBar.setProgress(intent.getIntExtra("seekBar",0));

        //色
        rgb = intent.getIntArrayExtra("rgb");
        picX = intent.getFloatExtra("picX",0);
        picY = intent.getFloatExtra("picY",0);
        if (rgb != null){
            imageView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
        }
        //hueId
        hueIdText.setText(intent.getStringExtra("hueId"));


    }
    public void huisKey(View v){
        Intent intent = new Intent(this,KeyInputActivity.class);
        //on off
        if (spinnerStr.contentEquals("on")){
            intent.putExtra("spinner",0);
        }else{
            intent.putExtra("spinner",1);
        }
        //明るさ
        intent.putExtra("seekBar",seekBar.getProgress());
        //色
        intent.putExtra("rgb",rgb);
        intent.putExtra("picX",picX);
        intent.putExtra("picY",picY);

        //hueId番号
        intent.putExtra("hueId",String.valueOf(hueIdText.getText()));

        //activity名
        intent.putExtra("ActivityName","HueSettingActivity");
        startActivity(intent);
    }

    public void save(View v){

        //何も入ってなかったら
        if (hueIdText.length() == 0){
            Toast.makeText(this,"hueのIdが指定されていません",Toast.LENGTH_SHORT).show();
            Toast.makeText(this,"kadecotを開き、操作したいhueのidを確認してください",Toast.LENGTH_LONG).show();
        }else{
        String[] arrayItems = {"Hue", huisKeyCodeText.getText().toString(),spinnerStr,String.valueOf(picX),String.valueOf(picY),
                String.valueOf(seekBar.getProgress()),String.valueOf(hueIdText.getText())};
        saveArray(arrayItems,String.valueOf(huisKeyCodeText.getText()));
            Log.d("保存されたkey",String.valueOf(huisKeyCodeText.getText()));


        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        }




    }
    //戻るボタンを押したときの動作を指定
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }


    void flagClear(){
        for (int i = 0; i<flag.length;i++){
            flag[i] = false;
        }
    }
    // プリファレンス保存
// aaa,bbb,ccc... の文字列で保存
    private void saveArray(String[] array,String PrefKey){
        StringBuffer buffer = new StringBuffer();
        String stringItem = null;
        for(String item : array){
            buffer.append(item+",");
        };
        if(buffer != null){
            String buf = buffer.toString();
            stringItem = buf.substring(0, buf.length() - 1);

            SharedPreferences prefs1 = getSharedPreferences("Array", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putString(PrefKey, stringItem).apply();
        }
    }
    void xy(float red,float green,float blue){
        red = (red > 0.04045f) ? (float) pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f);
        green = (green > 0.04045f) ? (float) pow((green + 0.055f) / (1.0f + 0.055f), 2.4f) : (green / 12.92f);
        blue = (blue > 0.04045f) ? (float) pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f);
        float X = red * 0.664511f + green * 0.154324f + blue * 0.162028f;
        float Y = red * 0.283881f + green * 0.668433f + blue * 0.047685f;
        float Z = red * 0.000088f + green * 0.072310f + blue * 0.986039f;
        picX = X / (X + Y + Z);
        picY = Y / (X + Y + Z);
    }
    int[] RGB(String str){
        int rgb[] = new int[3];
        if (str.length()==8) {
            //r = hex2int(str.substring(0, 2));
            rgb[0] = parseInt(str.substring(2, 4),16);
            rgb[1] = parseInt(str.substring(4, 6),16);
            rgb[2] = parseInt(str.substring(6, 8),16);
        }
        return rgb;
    }
    void spinnerSetting(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter.add("on");
        adapter.add("off");
        spinner = (Spinner) findViewById(R.id.spinner);
        // アダプターを設定します
        spinner.setAdapter(adapter);
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Spinner spinner = (Spinner) parent;
                // 選択されたアイテムを取得します
                spinnerStr = (String) spinner.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    void seekBarSetUp(){
        seekBar.setMax(255);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // トラッキング開始時に呼び出されます
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.v("onStartTrackingTouch()",
                        String.valueOf(seekBar.getProgress()));
            }
            // トラッキング中に呼び出されます
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                Log.v("onProgressChanged()",
                        String.valueOf(progress) + ", " + String.valueOf(fromTouch));
            }
            // トラッキング終了時に呼び出されます
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.v("onStopTrackingTouch()",
                        String.valueOf(seekBar.getProgress()));
            }
        });
    }

}
