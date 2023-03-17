package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
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

public class ActivityAddBalanceWithCard extends AppCompatActivity {
    public static ImageButton logo;
    public static EditText amount;
    public static TextView txtLast4Digits;
    public static Button btnAddBalance, btnAddCard;
    public static Context context;
    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_balance_with_card);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);
        loadLanguage();
        logo = (ImageButton) findViewById(R.id.add_balance_with_card_logo);
        amount = (EditText)findViewById(R.id.add_balance_with_card_amount);
        txtLast4Digits = (TextView)findViewById(R.id.add_balance_with_card_last4Digits);
        btnAddBalance = (Button)findViewById(R.id.add_balance_with_card_btnAddBalance);
        btnAddCard = (Button)findViewById(R.id.add_balance_with_card_btnAddCard);
        if(checkInternet()){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityHome.class);
                    context.startActivity(i);
                }
            });
            btnAddCard.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent i = new Intent(context, ActivityAddBalance.class);
                    context.startActivity(i);

                }
            });
            String lastCard = getCard(context);
            if(lastCard.contains("n")){
                String digits = " " + lastCard.split(";")[0].split(":")[1].substring(12);
                txtLast4Digits.setText(getString(R.string.ending_with) + digits);
                btnAddBalance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String amountString = amount.getText().toString();
                        if(amountString.isEmpty()){
                            amount.setError(getString(R.string.amount_required));
                            return;
                        }
                        int amountInt = (int)(Double.parseDouble(amountString) * 100);
                        if(amountInt < 100){
                            amount.setError(getString(R.string.wrong_value));
                            return;
                        }
                        paymentRequest("https://vendpay.ge/user/create_payment?amount=" + amountInt);
                    }
                });
            }
            else {
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(0);
                amount.setFilters(filters);
                txtLast4Digits.setText(getString(R.string.no_saved_cards));
                btnAddBalance.setEnabled(false);
                btnAddBalance.setBackgroundColor(Color.DKGRAY);
            }
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

    private String getCard(Context context) {
        String ret = "";
        try {
            String loggedId = getId(context);
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

    private String getId(Context context) {

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

        return ret.split(";")[1].split(":")[1];
    }

    private void paymentRequest(final String url) {
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
                ActivityAddBalanceWithCard.this.runOnUiThread(new Runnable() {
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
                            String card = getCard(context);
                            ActivityPayment.id = sb.toString().split(" ")[2];
                            ActivityPayment.cardNumber = card.split(";")[0].split(":")[1];
                            ActivityPayment.expMonth = card.split(";")[1].split(":")[1];
                            if((Integer.parseInt(card.split(";")[1].split(":")[1])) > 0 && (Integer.parseInt(card.split(";")[1].split(":")[1]) < 10)){
                                ActivityPayment.expMonth = "0" + (Integer.parseInt(card.split(";")[1].split(":")[1]));
                            }
                            ActivityPayment.expYear = card.split(";")[2].split(":")[1];
                            ActivityPayment.cvc2 = card.split(";")[3].split(":")[1];
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
