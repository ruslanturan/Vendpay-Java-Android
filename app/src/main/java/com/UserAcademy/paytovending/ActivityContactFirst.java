package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class ActivityContactFirst extends AppCompatActivity {
    public static ImageButton logo, btnCall;
    public static TextView txtMail;
    public static Context context;
    public static ProgressDialog progressDialog;
    public static String number, mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_first);
        loadLanguage();
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);
        logo = (ImageButton) findViewById(R.id.contact_first_logo);
        btnCall = (ImageButton)findViewById(R.id.contact_first_btnCall);
        txtMail = (TextView) findViewById(R.id.contact_first_txtMail);
        if(checkInternet()){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityFirst.class);
                    context.startActivity(i);
                }
            });
            contactRequest("https://vendpay.ge/user/contact_info");
            btnCall.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT > 22) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ActivityContactFirst.this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                        return;
                    }
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
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

    private void contactRequest(final String url) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        new Thread(new Runnable() {
            public void run() {
                final StringBuilder sb = new StringBuilder();
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements select = document.select("body");
                    Iterator it = select.iterator();
                    while (it.hasNext()) {
                        Element element = (Element) it.next();
                        String res = element.toString().replace("<body>\n","").replace("\n</body>","");
                        sb.append(res);
                    }
                } catch (IOException e) {
                    sb.append("Error: ");
                    sb.append(e.getMessage());
                }
                ActivityContactFirst.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error")) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_occ))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(context, ActivityFirst.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Intent i = new Intent(context, ActivityFirst.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setIcon(R.drawable.warnicon)
                                    .show();
                        }
                        else{
                            number = "+995" + sb.toString().split(";")[0].split(":")[1].trim();
                            mail = sb.toString().split(";")[1].split(":")[1];
                            txtMail.setText(sb.toString().split(";")[1].split(":")[1]);
                        }
                    }
                });
            }
        }).start();
    }

    public void loadLanguage(){
        SharedPreferences shp = getSharedPreferences(
                "com.UserAcademy.paytovending.PREFERENCES",Context.MODE_PRIVATE);
        String language = shp.getString("My_Lang","");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

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
