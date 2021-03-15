package com.example.tracking_ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("SetJavaScriptEnabled")
public class ShowWebChartActivity extends AppCompatActivity {

    WebView webView;
    int num1, num2, num3, num4, num5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webchart);

        Intent intent = getIntent();
        num1 = intent.getIntExtra("NUM1", 20);
        num2 = intent.getIntExtra("NUM2", 20);
        num3 = intent.getIntExtra("NUM3", 20);
        num4 = intent.getIntExtra("NUM4", 20);
        num5 = intent.getIntExtra("NUM5", 20);

        webView = (WebView)findViewById(R.id.web2);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/chart.html");
    }

    public class WebAppInterface {

        @JavascriptInterface
        public int getNum1() {
            return num1;
        }

        @JavascriptInterface
        public int getNum2() {
            return num2;
        }

        @JavascriptInterface
        public int getNum3() {
            return num3;
        }

        @JavascriptInterface
        public int getNum4() {
            return num4;
        }

        @JavascriptInterface
        public int getNum5() {
            return num5;
        }
    }

}