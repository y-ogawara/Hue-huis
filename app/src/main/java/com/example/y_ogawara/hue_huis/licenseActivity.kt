package com.example.y_ogawara.hue_huis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView


class licenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)
        val webview  = findViewById(R.id.webView) as WebView
        webview.loadUrl("file:///android_asset/licenses.html")
    }
}
