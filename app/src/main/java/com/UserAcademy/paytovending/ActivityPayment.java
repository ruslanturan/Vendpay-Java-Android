package com.UserAcademy.paytovending;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

public class ActivityPayment extends AppCompatActivity {
    public static Context context;
    public static WebView myWebView;
    public static String id, cardNumber, expMonth, expYear, cvc2, amount;
    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        context = this;
        getSupportActionBar().setTitle(R.string.n);

        myWebView = (WebView) findViewById(R.id.webview);

        if(checkInternet()){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();

            myWebView.loadUrl("https://ecommerce.ufc.ge/ecomm2/ClientHandler?trans_id=" + id);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            myWebView.getSettings().setDomStorageEnabled(true);
            myWebView.setVisibility(View.INVISIBLE);
            final boolean[] i = {true};
            myWebView.setWebViewClient(new WebViewClient()
            {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if(url.toLowerCase().contains("/tbc/success")){
                        myWebView.setVisibility(View.INVISIBLE);
                        progressDialog.dismiss();
                        resultRequest("https://vendpay.ge/user/payment_result?trans_id=" + id);
                    }
                    else if(url.toLowerCase().contains("/tbc/fail")){
                        myWebView.setVisibility(View.INVISIBLE);
                        progressDialog.dismiss();
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
                    }
                }
                @Override
                public void onPageFinished(final WebView view, String url) {
                    if(url.toLowerCase().contains("trans_id")){

                        view.loadUrl("javascript:var cardNum = document.getElementById('cardNumber').value = '" + cardNumber + "';" +
                                "var expMonth = document.getElementById('expmonth').value = '" + expMonth + "';" +
                                "var expYear = document.getElementById('expyear').value = '" + expYear + "';" +
                                "var cvc2 = document.getElementById('cvc2').value = '" + cvc2 + "';" +
                                " var btn = document.getElementById('payment-submit').click()");
                    }
                    else if (!url.toLowerCase().contains("ecommerce.ufc.ge") || !url.toLowerCase().contains("/tbc/")){
                        progressDialog.dismiss();
                        myWebView.setVisibility(View.VISIBLE);
                    }
//               else if(url.contains("georgiancard")){
//                    progressDialog.dismiss();
//
//                    view.loadUrl("javascript: var alert = document.getElementById('close-alert').click();" +
//                            " var send = document.getElementsByClassName('pay_button')[0].removeAttribute('disabled', 'false');" +
//                            " if(document.getElementById('errortexten').textContent.length > 0){" +
//                            " window.location.href = 'https://vendpay.ge/TBC/success'; }");
//                    if(i[0]){
//                        view.loadUrl("javascript: var sendsms = document.getElementsByClassName('button_sms')[0].click();");
//                        i[0] = false;
//                    }
//
//                    btnConfirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String code = txtCode.getText().toString();
//                            if(code.isEmpty() || code.length() != 4){
//                                txtCode.setError(getString(R.string.wrong_value));
//                                return;
//                            }
//                            view.loadUrl("javascript: var input = document.getElementsByClassName('sms_input')[0].value = '" + code + "';" +
//                                    " var pay = document.getElementsByClassName('pay_button')[0].click()");
//                        }
//                    });
//                }
//                else if(url.toLowerCase().contains("threedsbrowserchallenge")){
//                    progressDialog.dismiss();
//                    view.loadUrl("javascript: setTimeout(function() { " +
//                            "if(document.getElementById('error-text').textContent.length > 0) { " +
//                            " window.location.href = 'https://vendpay.ge/TBC/success'; } }, 2000);");
//                    btnConfirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String code = txtCode.getText().toString();
//                            if(code.isEmpty() || code.length() != 5){
//                                txtCode.setError(getString(R.string.wrong_value));
//                                return;
//                            }
//                            view.loadUrl("javascript: var input = document.getElementById('Code').value = '" + code + "';" +
//                                    " var pay = document.getElementById('submit-otp-button').click();" +
//                                    " setTimeout(function() { " +
//                                    "if(document.getElementById('error-text').textContent.length > 0) { " +
//                                    " window.location.href = 'https://vendpay.ge/TBC/success'; } }, 2000);");
//                        }
//                    });
//                }
//                else if(url.toLowerCase().contains("libertybank")){
//                    progressDialog.dismiss();
//
//                    view.loadUrl("javascript: setTimeout(function() { " +
//                            "if(document.getElementsByClassName('error')[0].textContent.length > 0) { " +
//                            " window.location.href = 'https://vendpay.ge/TBC/success'; } }, 2000);");
//                    btnConfirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String code = txtCode.getText().toString();
//                            if(code.isEmpty() || code.length() != 4){
//                                txtCode.setError(getString(R.string.wrong_value));
//                                return;
//                            }
//                            view.loadUrl("javascript: var input = document.getElementById('DynamicPassword').value = '" + code + "';" +
//                                    " var pay = document.getElementById('btnSubmit').click();" +
//                                    " setTimeout(function() { " +
//                                    " if(document.getElementsByClassName('error')[0].textContent.length > 0) { " +
//                                    " window.location.href = 'https://vendpay.ge/TBC/success'; } }, 2000);");
//                        }
//                    });
//                }
                }
                @Override
                public void onLoadResource(WebView view, String url) {
                    // TODO Auto-generated method stub
                    super.onLoadResource(view, url);

                }
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // System.out.println("when you click on any interlink on webview that time you got url :-" + url);
                    return super.shouldOverrideUrlLoading(view, url);
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

    private void resultRequest(final String url) {
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
                ActivityPayment.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (!sb.toString().toLowerCase().contains("ok")) {
                            progressDialog.dismiss();
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
                                    .setIcon(R.drawable.failicon)
                                    .show();

                        }
                        else{
                            String cardData = "n:" + cardNumber + ";m:" + expMonth + ";y:" + expYear + ";c:" + cvc2;
                            String loggedId = getId(context);
                            transactionRequest("https://vendpay.ge/user/transfer_money?userId=" + loggedId + "&description=Increase&amount=" + amount + "&addressId=0");
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.success))
                                    .setMessage(getString(R.string.successfully))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AlertDialog.Builder(context)
                                                    .setTitle(getString(R.string.warning))
                                                    .setMessage(getString(R.string.save_))
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            saveCard(cardData, context);
                                                            Intent i = new Intent(context, ActivityHome.class);
                                                            context.startActivity(i);
                                                        }
                                                    })
                                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                        @Override
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
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            new AlertDialog.Builder(context)
                                                    .setTitle(getString(R.string.warning))
                                                    .setMessage(getString(R.string.save_))
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            saveCard(cardData, context);
                                                            Intent i = new Intent(context, ActivityHome.class);
                                                            context.startActivity(i);
                                                        }
                                                    })
                                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                        @Override
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

    private void transactionRequest(final String url) {
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
                ActivityPayment.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if(sb.toString().contains("Error")){
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
                        }
                        else{
                            String loggedId = getId(context);
                            balanceRequest("https://vendpay.ge/user/change_balance?change=plus&userId=" + loggedId + "&amount=" + amount);
                        }
                    }
                });
            }
        }).start();
    }

    private void balanceRequest(final String url) {
        new Thread(new Runnable() {
            public void run() {
                progressDialog.dismiss();
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
                ActivityPayment.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if(sb.toString().contains("Error")){
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
                        }
                    }
                });
            }
        }).start();
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

    private void saveCard(String data,Context context) {
        try {
            String loggedId = getId(context);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(loggedId + " card.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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

    }

}
