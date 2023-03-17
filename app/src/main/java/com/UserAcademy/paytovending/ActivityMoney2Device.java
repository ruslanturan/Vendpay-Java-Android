package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Locale;

public class ActivityMoney2Device extends AppCompatActivity {
    public static ImageButton logo;
    public static Context context;
    public static EditText amount;
    public static Button btnSend;
    public static ProgressDialog progressDialog;
    public static String vendName = "", loggedId = "", loggedBalance = "", amountString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money2_device);
        loadLanguage();
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);

        logo = (ImageButton) findViewById(R.id.money2_device_logo);
        amount = (EditText)findViewById(R.id.money2_device_amount);
        btnSend = (Button)findViewById(R.id.money2_device_btnSend);
        loggedId = getInfo(context).split(";")[1].split(":")[1];
        loggedBalance = getInfo(context).split(";")[0].split(":")[1];
        final Double balance = Double.parseDouble(loggedBalance);

        if(checkInternet()){
            btnSend.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    amountString = amount.getText().toString();
                    Double amountDouble = Double.parseDouble(amountString) * 100;
                    final int amountForDevice = (int) Math.round(amountDouble);
                    if(amountString.length() > 0){
                        if(amountDouble > 50){
                            if(Double.parseDouble(amountString) > balance){
                                new AlertDialog.Builder(context)
                                        .setTitle(getString(R.string.error))
                                        .setMessage(getString(R.string.not_money))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {

                                            }
                                        })
                                        .setIcon(R.drawable.failicon)
                                        .show();
                                return;
                            }
                            else{
                                machineRequest("https://vendpay.ge/machine/setbalance?name=" + vendName + "&amount=" + amountForDevice + "&userId=" + loggedId);
                                new AlertDialog.Builder(context)
                                        .setTitle(getString(R.string.success))
                                        .setMessage(getString(R.string.completed))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent(context, ActivityHome.class);
                                                context.startActivity(i);
                                            }
                                        })
                                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                Intent i = new Intent(context, ActivityHome.class);
                                                context.startActivity(i);
                                            }
                                        })
                                        .setIcon(R.drawable.successicon)
                                        .show();
                            }
                        }else{
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.minimum_amount))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {

                                        }
                                    })
                                    .setIcon(R.drawable.failicon)
                                    .show();
                        }
                    }
                    else{
                        new AlertDialog.Builder(context)
                                .setTitle(getString(R.string.error))
                                .setMessage(getString(R.string.incorrect_amount))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {

                                    }
                                })
                                .setIcon(R.drawable.failicon)
                                .show();
                    }
                    amount.setText("");
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

    private String getInfo(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput("config.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void machineRequest(final String url) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
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
                ActivityMoney2Device.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: Machine is busy")) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.busy))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setIcon(R.drawable.warnicon)
                                    .show();
                        }
                        else if (sb.toString().contains("Error")) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_occ))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setIcon(R.drawable.warnicon)
                                    .show();
                        }
                        else{
                            webRequest("https://vendpay.ge/user/change_balance?change=minus&userId=" + loggedId + "&amount=" + amountString);
                            webRequest("https://vendpay.ge/user/transfer_money?userId=" + loggedId + "&description=Paid%20to%20-%20" + vendName + "&amount=" + amountString + "&addressId=0");
                            webRequest("https://vendpay.ge/user/transaction?userId=" + loggedId + "&machinename=" + vendName + "&amount=" + amountString);
                        }
                    }
                });
            }
        }).start();
    }

    private void webRequest(final String url) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
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
                ActivityMoney2Device.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error")) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_occ))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            Intent i = new Intent(context, ActivityHome.class);
                                            context.startActivity(i);
                                        }
                                    })
                                    .setIcon(R.drawable.warnicon)
                                    .show();
                        }
                    }
                });
            }
        }).start();
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
    @Override
    public void onBackPressed() {
        context.startActivity(new Intent(context.getApplicationContext(), ActivityHome.class));
    }

}
