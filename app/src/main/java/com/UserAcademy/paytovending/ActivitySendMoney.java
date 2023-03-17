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
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Iterator;
import java.util.Locale;

public class ActivitySendMoney extends AppCompatActivity {
    public static ImageButton logo;
    public static TextView txtId, txtBalance;
    public static EditText amount, receiverId, code;
    public static Button btnTransfer;
    public static ImageButton btnHelp;
    public static Context context;
    public static ProgressDialog progressDialog;
    public static String loggedId = "", unique = "", amountString = "", receiverIdString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);
        context = this;
        loadLanguage();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);
        logo = (ImageButton) findViewById(R.id.send_money_logo);
        txtId = (TextView)findViewById(R.id.send_money_txtId);
        txtBalance = (TextView)findViewById(R.id.send_money_txtBalance);
        amount = (EditText)findViewById(R.id.send_money_amount);
        code = (EditText)findViewById(R.id.send_money_code);
        receiverId = (EditText)findViewById(R.id.send_money_receiverId);
        btnTransfer = (Button)findViewById(R.id.send_money_btnTransfer);
        btnHelp = (ImageButton)findViewById(R.id.send_money_btnHelp);
        if(checkInternet()){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityHome.class);
                    context.startActivity(i);
                }
            });
            loggedId = getInfo(context).split(";")[1].split(":")[1];
            final String loggedBalance = getInfo(context).split(";")[0].split(":")[1];
            txtBalance.setText(txtBalance.getText() + ": " + loggedBalance + " " + getString(R.string.gel));
            txtId.setText(txtId.getText() + ": " + loggedId);
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(0);
            code.setFilters(filters);
            btnHelp.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.how_it_works)
                                .setMessage(getString(R.string.will_send_to_receiver))
                                .setPositiveButton(R.string.got, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {

                                    }
                                })
                                .setIcon(R.drawable.warnicon)
                                .show();
            }
            });
            btnTransfer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String text = btnTransfer.getText().toString();
                    String req = getString(R.string.req_ver_code);
                    if(text.toLowerCase().contains(req.toLowerCase())){
                        amountString = amount.getText().toString();
                        receiverIdString = receiverId.getText().toString();
                        Double balance = Double.parseDouble(loggedBalance);
                        if(amountString.isEmpty()){
                            amount.setError(getString(R.string.amount_required));
                            return;
                        }
                        if(amountString.startsWith(".")){
                            amount.setError(getString(R.string.incorrect_amount));
                            return;
                        }
                        if(Double.parseDouble(amountString) > balance){
                            amount.setError(getString(R.string.not_money));
                            return;
                        }
                        if(receiverIdString.length() != 6 && loggedId.contains(receiverIdString)){
                            receiverId.setError(getString(R.string.wrong_ID));
                            return;
                        }
                        codeRequest("https://vendpay.ge/user/create_unique?fromId=" + loggedId + "&toId=" + receiverIdString + "&amount=" + amountString);
                        amount.setFocusable(false);
                        receiverId.setFocusable(false);

                        InputFilter[] filters = new InputFilter[1];
                        filters[0] = new InputFilter.LengthFilter(5);
                        code.setFilters(filters);
                        btnTransfer.setText(R.string.complete);
                    }
                    else{
                        String unique_num = code.getText().toString().toUpperCase();
                        if(unique_num.length() < 5){
                            code.setError(getString(R.string.wrong_unique));
                            return;
                        }
                        if(!unique.contains(unique_num)){
                            code.setError(getString(R.string.wrong_unique));
                            return;
                        }
                        payRequest("https://vendpay.ge/user/transfer_money?userId=" + loggedId + "&description=Sent%20to%20-%20&amount=" + amountString + "&addressId=" + receiverIdString);
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

    private void codeRequest(final String url) {
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
                ActivitySendMoney.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: User")) {
                            receiverId.setError(getString(R.string.user_not_found));
                            return;
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
                            unique = sb.toString().split(":")[1];
                        }
                    }
                });
            }
        }).start();
    }

    private void payRequest(final String url) {
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
                ActivitySendMoney.this.runOnUiThread(new Runnable() {
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
                        else {
                            balanceRequest("https://vendpay.ge/user/change_balance?change=minus&userId=" + loggedId + "&amount=" + amountString);
                            balanceRequest("https://vendpay.ge/user/change_balance?change=plus&userId=" + receiverIdString + "&amount=" + amountString);
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
                    }
                });
            }
        }).start();
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
                ActivitySendMoney.this.runOnUiThread(new Runnable() {
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

}
