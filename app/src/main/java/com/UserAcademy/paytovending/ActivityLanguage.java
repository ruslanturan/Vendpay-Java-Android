package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import java.util.Locale;

public class ActivityLanguage extends AppCompatActivity {
    public static ImageButton btnEng, btnGe;
    public static ProgressDialog progressDialog;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        context = this;
        getSupportActionBar().setTitle(R.string.n);

        btnEng = (ImageButton) findViewById(R.id.language_btnEng);
        btnGe = (ImageButton) findViewById(R.id.language_btnGe);

        if(checkInternet()){
            btnEng.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(getString(R.string.loading));
                    progressDialog.show();
                    setLanguage("en");
                    saveLocale("en");
                    recreate();
                    Intent intent = new Intent(context, ActivityFirst.class);
                    context.startActivity(intent);
                }
            });

            btnGe.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(getString(R.string.loading));
                    progressDialog.show();
                    setLanguage("ab");
                    saveLocale("ab");
                    recreate();
                    Intent intent = new Intent(context, ActivityFirst.class);
                    context.startActivity(intent);
                }
            });
        }
        else{
            new AlertDialog.Builder(context)
                    .setTitle("Connection failed")
                    .setMessage("Please check the internet connection and try again.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent("android.intent.action.MAIN");
                            intent.addCategory("android.intent.category.HOME");
                            startActivity(intent);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Intent intent = new Intent("android.intent.action.MAIN");
                            intent.addCategory("android.intent.category.HOME");
                            startActivity(intent);
                        }
                    })
                    .setIcon(R.drawable.failicon)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
    }

    private void setLanguage(String lang){
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);

    }

    public void saveLocale(String lang) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.UserAcademy.paytovending.PREFERENCES", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("My_Lang", lang);
        editor.commit();
    }

    private Boolean checkInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            return  true;
        }
        else
            return false;
    }

}
