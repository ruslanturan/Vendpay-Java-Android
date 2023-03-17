package com.UserAcademy.paytovending;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Button;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class FetchTransactions extends AsyncTask<Void, Void, Void> {
    public static Button btn;
    public static ArrayList<ClassTransaction> transactions = new ArrayList<>();
    String data;
    String singleParsedName;
    String amount, date, addressIdNumber, addressMail, addressId;
    public FetchTransactions() {
        String str = "";
        this.data = str;
        this.singleParsedName = str;
    }

    public Void doInBackground(Void... voidArr) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(((HttpURLConnection) new URL("https://vendpay.ge/api/transaction/get/" + ActivityTransactions.id).openConnection()).getInputStream()));
            String str = "";
            transactions.clear();
            while (str != null) {
                str = bufferedReader.readLine();
                StringBuilder sb = new StringBuilder();
                sb.append(this.data);
                sb.append(str);
                this.data = sb.toString();
            }
            JSONArray jSONArray = new JSONArray(this.data);
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                this.amount = jSONObject.getString("amount");
                this.date = jSONObject.getString("date").substring(0,16).replace("T", " ");
                this.addressId = jSONObject.getString("addressId");
                this.addressIdNumber = jSONObject.getString("addressId_number");
                this.addressMail = jSONObject.getString("address_Mail");
                transactions.add(new ClassTransaction(this.amount, this.date, this.addressId, this.addressIdNumber,this.addressMail));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (JSONException e3) {
            e3.printStackTrace();
        }
        return null;
    }

    public void onPreExecute() {
        super.onPreExecute();
        ActivityTransactions.progressDialog = new ProgressDialog(ActivityTransactions.context);
        ActivityTransactions.progressDialog.setMessage("Loading");
        ActivityTransactions.progressDialog.show();
    }

    public void onPostExecute(Void voidR) {
        super.onPostExecute(voidR);
        Collections.reverse(transactions);
        ActivityTransactions.list.setAdapter(new AdapterTransactions(ActivityTransactions.context, R.layout.adapter_transactions, transactions));
        ActivityTransactions.progressDialog.dismiss();
    }
}