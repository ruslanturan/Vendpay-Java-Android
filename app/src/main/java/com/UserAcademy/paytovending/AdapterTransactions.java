package com.UserAcademy.paytovending;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class AdapterTransactions extends ArrayAdapter<ClassTransaction> {

    public Context cContext;
    int cResource;

    public AdapterTransactions(Context context, int i, ArrayList<ClassTransaction> arrayList) {
        super(context, i, arrayList);
        this.cContext = context;
        this.cResource = i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        String amount = ((ClassTransaction) getItem(i)).getAmount();
        String date = ((ClassTransaction) getItem(i)).getDate();
        String address = ((ClassTransaction) getItem(i)).getAddressId();
        String addressId = ((ClassTransaction) getItem(i)).getAddressIdNumber();
        String mail = ((ClassTransaction) getItem(i)).getAddressMail();
        new ClassTransaction(amount,date,address,addressId,mail);
        View inflate = LayoutInflater.from(this.cContext).inflate(this.cResource, viewGroup, false);
        TextView txtAmount = (TextView) inflate.findViewById(R.id.adapter_amount);
        TextView txtDate = (TextView) inflate.findViewById(R.id.adapter_date);
        TextView txtId = (TextView) inflate.findViewById(R.id.adapter_addressId);
        TextView txtMail = (TextView) inflate.findViewById(R.id.adapter_addressMail);
        txtAmount.setText(amount);
        txtDate.setText(date);
        txtMail.setText(mail);
        if(!address.equals("0")){
            txtId.setText(addressId);
        }
        else{
            txtId.setText("");
        }
        if(amount.indexOf("-") > -1){
            txtAmount.setTextColor(Color.parseColor("#FC0000"));
        }
        else {
            txtAmount.setTextColor(Color.parseColor("#FF008A2C"));
        }
        return inflate;
    }
}
