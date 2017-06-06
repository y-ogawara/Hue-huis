package com.example.y_ogawara.hue_huis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class KeyInputActivity extends AppCompatActivity {

    TextView keyText;
    String BeforeActivityName ="";
    Intent getIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_input);
        keyText = (TextView)findViewById(R.id.keyText);
        getIntent = getIntent();
        if (getIntent.getStringExtra("ActivityName").equals("HueSettingActivity")){
            BeforeActivityName = "HueSettingActivity";
        }else if (getIntent.getStringExtra("ActivityName").equals("IftttSettingActivity")) {
            BeforeActivityName = "IftttSettingActivity";
        }
        else{
            BeforeActivityName = "nullActivity";
        }
    }
    public void back(View v){
        if (BeforeActivityName.equals("HueSettingActivity")) {
            Intent intent = new Intent(this,HueSettingActivity.class);
            intent.putExtra("keyCode",keyText.getText().toString());
            //on off
            intent.putExtra("spinner",getIntent.getIntExtra("spinner",0));
            //明るさ
            intent.putExtra("seekBar",getIntent.getIntExtra("seekBar",0));
            //色
            intent.putExtra("rgb",getIntent.getIntArrayExtra("rgb"));
            intent.putExtra("picX",getIntent.getFloatExtra("picX",0));
            intent.putExtra("picY",getIntent.getFloatExtra("picY",0));
            intent.putExtra("hueId",getIntent.getStringExtra("hueId"));
            startActivity(intent);

        }
//        else if (BeforeActivityName.equals("IftttSettingActivity")){
//            Intent intent = new Intent(this,IftttSettingActivity.class);
//            intent.putExtra("keyCode",keyText.getText().toString());
//            intent.putExtra("eventText",getIntent.getStringExtra("eventText"));
//            intent.putExtra("secretKeyText",getIntent.getStringExtra("secretKeyText"));
//            startActivity(intent);
//
//
//        }


    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // キーコードを表示する
        Log.d("HUIS","KeyCode:" + e.getKeyCode());
        keyText.setText(String.valueOf(e.getKeyCode()));
        return super.dispatchKeyEvent(e);
    }
}
