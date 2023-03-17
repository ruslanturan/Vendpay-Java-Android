package com.UserAcademy.paytovending;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Locale;

public class ActivityHome extends AppCompatActivity {
    public static TextView txtId, txtBalance;
    public static CardView btnConnect;
    public static ImageButton logo,btnQr;
    public static Context context;
    public static ProgressDialog progressDialog;
    public static String loggedBalance = "", loggedId = "",balance="";
    private int PERMISSION_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        loadLanguage();
        context = this;
        getSupportActionBar().setTitle(R.string.n);
        txtId = (TextView)findViewById(R.id.home_txtId);
        txtBalance = (TextView)findViewById(R.id.home_txtBalance);
        btnConnect = (CardView) findViewById(R.id.home_btnConnect);
        btnQr = (ImageButton) findViewById(R.id.home_btnQr);
        logo = (ImageButton) findViewById(R.id.home_logo);

        if(checkInternet()){
            loggedId = getInfo(context).split(";")[1].split(":")[1];
            txtId.setText(txtId.getText() + ": " + loggedId);
            balanceRequest("https://vendpay.ge/user/getuserbalance?id=" + loggedId);
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    balanceRequest("https://vendpay.ge/user/getuserbalance?id=" + loggedId);
                }
            });
            btnConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Double amountDouble = Double.parseDouble(balance) * 100;
                    final int amountForDevice = (int) Math.round(amountDouble);
                    if(amountForDevice > 49){
                        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), "android.permission.CAMERA") == 0) {
                            context.startActivity(new Intent(context.getApplicationContext(), ActivityReadQR.class));
                        }
                        else {
                            new android.app.AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.permission_required))
                                    .setMessage(getString(R.string.need_camera))
                                    .setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.CAMERA},PERMISSION_CODE);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setIcon(R.drawable.warnicon).show();
                        }
                    }
                    else{
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(getString(R.string.error))
                                .setMessage(getString(R.string.minimum_amount))
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).setIcon(R.drawable.warnicon).show();
                    }
                }

            });
            btnQr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Double amountDouble = Double.parseDouble(balance) * 100;
                    final int amountForDevice = (int) Math.round(amountDouble);
                    if(amountForDevice > 49){
                        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), "android.permission.CAMERA") == 0) {
                            context.startActivity(new Intent(context.getApplicationContext(), ActivityReadQR.class));
                        }
                        else {
                            new android.app.AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.permission_required))
                                    .setMessage(getString(R.string.need_camera))
                                    .setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ActivityCompat.requestPermissions((Activity) context, new String[] {Manifest.permission.CAMERA},PERMISSION_CODE);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setIcon(R.drawable.warnicon).show();
                        }
                    }
                    else{
                        new android.app.AlertDialog.Builder(context)
                                .setTitle(getString(R.string.error))
                                .setMessage(getString(R.string.minimum_amount))
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).setIcon(R.drawable.warnicon).show();
                    }
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
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);

            SpannableString s = new SpannableString(mi.toString());
            s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
            mi.setTitle(s);

            final Typeface typeface = ResourcesCompat.getFont(context, R.font.bpg_extrasquare_mtavruli_2009);
            applyFontToMenuItem(mi, typeface);
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.account:
                Intent account_intent = new Intent(context, ActivityAccountInfo.class);
                context.startActivity(account_intent);
                return true;
            case R.id.addBalance:
                String cards = getCard(context);
                if(cards != null && cards.length() > 3){
                    Intent balance_intent = new Intent(context, ActivityAddBalanceWithCard.class);
                    context.startActivity(balance_intent);
                }
                else{
                    Intent balance_intent = new Intent(context, ActivityAddBalance.class);
                    context.startActivity(balance_intent);
                }
                return true;
            case R.id.transfer:
                Intent transfer_intent = new Intent(context, ActivityTransferMoney.class);
                context.startActivity(transfer_intent);
                return true;
            case R.id.transactions:
                Intent transaction_intent = new Intent(context, ActivityTransactions.class);
                context.startActivity(transaction_intent);
                return true;
            case R.id.contact:
                Intent contact_intent = new Intent(context, ActivityContactHome.class);
                context.startActivity(contact_intent);
                return true;
            case R.id.terms:
                Intent terms_intent = new Intent(context, ActivityTermsHome.class);
                context.startActivity(terms_intent);
                return true;
            case R.id.policy:
                Intent policy_intent = new Intent(context, ActivityPolicyHome.class);
                context.startActivity(policy_intent);
                return true;
            case R.id.logout:
                saveInfo("", context);
                //writeName("", context);
                Intent logout_intent = new Intent(context, ActivityLaunch.class);
                context.startActivity(logout_intent);
                return true;
            case R.id.changeLang:
                String lang = item.getTitle().toString();
                if(lang.contains("Lang")){
                    setLanguage("ab");
                    saveLocale("ab");
                    recreate();
                }
                else {
                    setLanguage("en");
                    saveLocale("en");
                    recreate();
                }
                return true;
            default:
                return true;
        }
    }

    private void applyFontToMenuItem(MenuItem mi, Typeface font) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                context.startActivity(new Intent(context.getApplicationContext(), ActivityReadQR.class));
            }
            else {
                new android.app.AlertDialog.Builder(context)
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.not_allowed))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.drawable.failicon)
                        .show();
            }
        }
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

    private void saveInfo(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String getCard(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(loggedId + " card.txt");

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

    private void balanceRequest(final String url) {
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
                ActivityHome.this.runOnUiThread(new Runnable() {
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
                            loggedBalance = sb.toString().split(";")[0].split(":")[1];
                            balance = sb.toString().split(";")[0].split(":")[1];
                            String loggedPhone = sb.toString().split(";")[1].split(":")[1];
                            txtBalance.setText(txtBalance.getText().toString().split(":")[0] + ": " + loggedBalance.split(getString(R.string.gel))[0] + " " + getString(R.string.gel));
                            String loggedData = "balance: " + loggedBalance.split(getString(R.string.gel))[0] + ";" + getInfo(context).split(";")[1];
                            saveInfo(loggedData, context);
                            if(loggedPhone.equals("0")){
                                Intent intent = new Intent(context, ActivityVerifyPhone.class);
                                context.startActivity(intent);
                            }
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
    @Override
    public void onBackPressed() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
    }

}
