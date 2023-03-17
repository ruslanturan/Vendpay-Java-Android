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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Locale;

public class ActivityRegister extends AppCompatActivity {
    public static ImageButton logo;
    public static EditText txtUsername, txtMail, txtPassword, txtConfirmPass;
    public static Button registerBtn;
    public static Context context;
    public static ProgressDialog progressDialog;
    public static String username = "", mail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loadLanguage();
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);
        logo = (ImageButton) findViewById(R.id.register_logo);
        txtUsername = (EditText)findViewById(R.id.register_username);
        txtMail = (EditText)findViewById(R.id.register_mail);
        txtPassword = (EditText)findViewById(R.id.register_password);
        txtConfirmPass = (EditText)findViewById(R.id.register_confirmPassword);
        registerBtn = (Button)findViewById(R.id.register_btn);
        if(checkInternet()){
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityFirst.class);
                    context.startActivity(i);
                }
            });
            registerBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    register();
                }
            });
            txtUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        register();
                        return true;
                    }

                    return false;
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

    public void register(){
        username = txtUsername.getText().toString();
        mail = txtMail.getText().toString();
        String password = txtPassword.getText().toString();
        String confirmPass = txtConfirmPass.getText().toString();
        if(username.isEmpty()){
            txtUsername.setError(getString(R.string.username_required));
            return;
        }
        if(username.contains(" ")){
            txtUsername.setError(getString(R.string.no_whitespaces));
            return;
        }
        if(mail.isEmpty()){
            txtMail.setError(getString(R.string.mail_required));
            return;
        }
        if(mail.contains(" ")){
            txtMail.setError(getString(R.string.no_whitespaces));
            return;
        }
        if(!mail.contains("@") || !mail.contains(".") || mail.contains(",")){
            txtMail.setError(getString(R.string.wrong_mail));
            return;
        }
        if(password.isEmpty()){
            txtPassword.setError(getString(R.string.pass_required));
            return;
        }
        if(password.contains(" ")){
            txtPassword.setError(getString(R.string.no_whitespaces));
            return;
        }
        if(!password.equals(confirmPass)){
            txtPassword.setError(getString(R.string.pass_match));
            return;
        }
        userRequest("https://vendpay.ge/user/adduser?username=" + username + "&email=" + mail + "&password=" + password);
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

    private void userRequest(final String url) {
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
                ActivityRegister.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: E-mail")) {
                            txtMail.setError(getString(R.string.mail_exists));
                            return;
                        }
                        else if (sb.toString().contains("Error")) {
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
                        else {
                            String loggedId = sb.toString().split(":")[1];
                            saveInfo("balance:0.00;ID:" + loggedId,context);
                            //writeName("Username:" + username + ";Mail:" + mail,context);
                            Intent i = new Intent(context, ActivityVerifyPhone.class);
                            ActivityVerifyPhone.loggedId = loggedId;
                            context.startActivity(i);
                        }
                    }
                });
            }
        }).start();
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

//    private void writeName(String data,Context context) {
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("name.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }

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

