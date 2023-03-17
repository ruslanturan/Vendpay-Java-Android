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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

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

public class ActivityLogin extends AppCompatActivity {
    public static ImageButton logo;
    public static EditText mail, password;
    public static Button btnForgot, btnLogin;
    public static CheckBox checkBox;
    public static Context context;
    public static ProgressDialog progressDialog;
    public static String mailString = "";
    public static Boolean saveCredentials;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadLanguage();
        context = this;
        getSupportActionBar().setTitle(R.string.n);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        logo = (ImageButton) findViewById(R.id.login_logo);
        mail = (EditText)findViewById(R.id.login_mail);
        password = (EditText)findViewById(R.id.login_password);
        btnForgot = (Button)findViewById(R.id.login_btnForgotPassword);
        btnLogin = (Button)findViewById(R.id.login_btnLogin);
        checkBox = (CheckBox)findViewById(R.id.checkBox);
        if(checkBox.isChecked()){
            saveCredentials = true;
        }else{
            saveCredentials = false;
        }
        if(checkInternet()){
            String credentials = getCredentials(context);
            if(credentials.contains(";")){
                mail.setText(credentials.split(";")[0]);
                password.setText(credentials.split(";")[1]);
            }else{
                mail.setText("");
                password.setText("");
            }
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ActivityFirst.class);
                    context.startActivity(i);
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                        if(isChecked){
                            saveCredentials = true;
                        }
                        else{
                            saveCredentials = false;
                        }
                    }
                }
            );
            btnLogin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mailString = mail.getText().toString();
                    String passwordString = password.getText().toString();
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
                    if(passwordString.isEmpty()){
                        password.setError(getString(R.string.pass_required));
                        return;
                    }
                    if(passwordString.contains(" ")){
                        password.setError(getString(R.string.no_whitespaces));
                        return;
                    }
                    userRequest("https://vendpay.ge/user/getuser?mail=" + mailString + "&password=" + passwordString);
                }
            });
            btnForgot.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent i = new Intent(context, ActivityForgotPassword.class);
                    context.startActivity(i);
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
                ActivityLogin.this.runOnUiThread(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (sb.toString().contains("Error: User")) {
                            mail.setError(getString(R.string.user_not_found));
                            return;
                        }
                        else if (sb.toString().contains("Error: Password")) {
                            password.setError(getString(R.string.wrong_pass));
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
                            saveInfo("balance:" + sb.toString().split(";")[0].split(":")[1] + ";ID:" + sb.toString().split(";")[1].split(":")[1],context);
                            if(saveCredentials){
                                setCredentials(mailString + ";" + password.getText().toString(), context);
                            }
                            else {
                                setCredentials("", context);
                            }
                            Intent i = new Intent(context, ActivityHome.class);
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

    private void setCredentials(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("credentials.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String getCredentials(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("credentials.txt");

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
