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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class ActivityForgotPassword extends AppCompatActivity {

    public static ImageButton logo;
    public static EditText mail, code, password, pass;
    public static Button btnChangePassword;
    public static Context context;
    public static ImageButton btnHelp;
    public static ProgressDialog progressDialog;
    public static String unique = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        loadLanguage();
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.n);

        logo = (ImageButton) findViewById(R.id.forgot_password_logo);
        mail = (EditText)findViewById(R.id.forgot_password_mail);
        code = (EditText)findViewById(R.id.forgot_password_code);
        password = (EditText)findViewById(R.id.forgot_password_password);
        pass = (EditText)findViewById(R.id.forgot_password_pass);
        btnChangePassword = (Button)findViewById(R.id.forgot_password_btnChangePassword);
        btnHelp = (ImageButton)findViewById(R.id.forgot_password_btnHelp);
        code.setFocusable(false);
        password.setFocusable(false);
        pass.setFocusable(false);
        pass.setFocusableInTouchMode(false);
        password.setFocusableInTouchMode(false);
        code.setFocusableInTouchMode(false);

        if(checkInternet()){
            btnHelp.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.how_it_works)
                            .setMessage(getString(R.string.code_sent))
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
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityFirst.class);
                    context.startActivity(i);
                }
            });
            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String text = btnChangePassword.getText().toString();
                    String req = getString(R.string.req_ver_code);
                    if(text.toLowerCase().contains(req.toLowerCase())){
                        String mailString = mail.getText().toString();
                        if(mailString.isEmpty()){
                            mail.setError(getString(R.string.mail_required));
                            return;
                        }
                        if(mailString.contains(" ")){
                            mail.setError(getString(R.string.no_whitespaces));
                            return;
                        }
                        if(!mailString.contains("@") || !mailString.contains(".")){
                            mail.setError(getString(R.string.wrong_mail));
                            return;
                        }
                        codeRequest("https://vendpay.ge/user/check_user?email=" + mailString);
                        mail.setFocusable(false);
                        code.setFocusable(true);
                        password.setFocusable(true);
                        pass.setFocusable(true);
                        mail.setFocusableInTouchMode(false);
                        pass.setFocusableInTouchMode(true);
                        password.setFocusableInTouchMode(true);
                        code.setFocusableInTouchMode(true);
                        InputFilter[] filters = new InputFilter[1];
                        filters[0] = new InputFilter.LengthFilter(5);
                        code.setFilters(filters);
                        btnChangePassword.setText(R.string.change_password);
                    }
                    else{
                        String textUnique = code.getText().toString();
                        String passwordString = password.getText().toString();
                        String passString = pass.getText().toString();
                        if(textUnique.length() < 5){
                            code.setError(getString(R.string.wrong_unique));
                            return;
                        }
                        if(!unique.contains(textUnique.toLowerCase())){
                            code.setError(getString(R.string.wrong_unique));
                            return;
                        }
                        if(passwordString.isEmpty()){
                            password.setError(getString(R.string.pass_required));
                            return;
                        }
                        if(passwordString.contains(" ")){
                            password.setError(getString(R.string.no_whitespaces));
                            return;
                        }
                        if(!passwordString.equals(passString)){
                            pass.setError(getString(R.string.pass_match));
                            return;
                        }
                        passwordRequest("https://vendpay.ge/user/change_password?unique=" + unique + "&password=" + passwordString);
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
                ActivityForgotPassword.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: User")) {
                            mail.setError(getString(R.string.user_not_found));
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
                        else{
                            unique = sb.toString().split(":")[1].toLowerCase();
                        }
                    }
                });
            }
        }).start();
    }

    private void passwordRequest(final String url) {
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
                ActivityForgotPassword.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: User")) {
                            mail.setError(getString(R.string.user_not_found));
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
                        else{
                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.success))
                                    .setMessage(getString(R.string.pass_changed))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
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
                                    .setIcon(R.drawable.successicon)
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
