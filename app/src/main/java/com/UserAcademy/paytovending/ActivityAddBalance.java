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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class ActivityAddBalance extends AppCompatActivity {
    public static ImageButton logo;
    public static EditText amount, cardNumber, cardCVV, cardMonth, cardYear;
    public static Button btnAdd;
    public static Context context;
    public static ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance);
        loadLanguage();
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);
        logo = (ImageButton) findViewById(R.id.add_balance_logo);
        amount = (EditText)findViewById(R.id.add_balance_amount);
        cardNumber = (EditText)findViewById(R.id.add_balance_cardNumber);
        cardCVV = (EditText)findViewById(R.id.add_balance_cardCVV);
        cardMonth = (EditText)findViewById(R.id.add_balance_cardMonth);
        cardYear = (EditText)findViewById(R.id.add_balance_cardYear);
        btnAdd = (Button)findViewById(R.id.add_balance_btnAdd);
        if(checkInternet()){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityHome.class);
                    context.startActivity(i);
                }
            });
            btnAdd.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String amountString = amount.getText().toString();
                    String number = cardNumber.getText().toString();
                    String month = cardMonth.getText().toString();
                    String year = cardYear.getText().toString();
                    String cvv = cardCVV.getText().toString();
                    if(amountString.isEmpty()){
                        amount.setError(getString(R.string.amount_required));
                        return;
                    }
                    int amountInt = (int)(Double.parseDouble(amountString) * 100);
                    if(amountInt < 100){
                        amount.setError(getString(R.string.wrong_value));
                        return;
                    }
                    if(number.length() != 16){
                        cardNumber.setError(getString(R.string.wrong_number));
                        return;
                    }
                    if(month.isEmpty() || (Integer.parseInt(month)) > 12){
                        cardMonth.setError(getString(R.string.wrong_value));
                        return;
                    }
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    if(year.isEmpty() || (Integer.parseInt("20" + year)) < currentYear){
                        cardYear.setError(getString(R.string.wrong_value));
                        return;
                    }
                    if(cvv.isEmpty() || cvv.length() < 3 || cvv.length() > 4){
                        cardCVV.setError(getString(R.string.wrong_value));
                        return;
                    }
                    webRequest("https://vendpay.ge/user/create_payment?amount=" + amountInt);
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
                "com.UserAcademy.paytovending.PREFERENCES", Context.MODE_PRIVATE);
        String language = shp.getString("My_Lang","");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

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
                ActivityAddBalance.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (!sb.toString().toLowerCase().contains("transaction_id")) {
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.error_occ))
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
                                    .setIcon(R.drawable.failicon)
                                    .show();
                            return;
                        }
                        else{
                            Intent i = new Intent(context, ActivityPayment.class);
                            ActivityPayment.id = sb.toString().split(" ")[2];
                            ActivityPayment.cardNumber = cardNumber.getText().toString();
                            Integer month = Integer.parseInt(cardMonth.getText().toString());
                            ActivityPayment.expMonth = month.toString();
                            if(month < 10){
                                ActivityPayment.expMonth = "0" + month;
                            }
                            ActivityPayment.expYear = cardYear.getText().toString();
                            ActivityPayment.cvc2 = cardCVV.getText().toString();
                            ActivityPayment.amount = amount.getText().toString();
                            context.startActivity(i);
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
