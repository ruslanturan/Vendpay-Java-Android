package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ActivityPolicyHome extends AppCompatActivity {
    public static Context context;
    public static WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_home);
        context = this;
        getSupportActionBar().setTitle(R.string.n);

        myWebView = (WebView) findViewById(R.id.policyHomeWebView);
        SharedPreferences shp = getSharedPreferences(
                "com.UserAcademy.paytovending.PREFERENCES",Context.MODE_PRIVATE);
        String language = shp.getString("My_Lang","");
        if(language.contains("ab")){
            myWebView.loadUrl("https://vendpay.ge/login/policy/lang=ka");
        }
        else{
            myWebView.loadUrl("https://vendpay.ge/login/policy/lang=en");
        }
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
    }
}
