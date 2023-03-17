package com.UserAcademy.paytovending;

public class ClassTransaction {

    private String amount, date, addressIdNumber, addressMail, addressId;

    public ClassTransaction(String amount, String date, String addressId, String addressIdNumber, String addressMail) {
        this.amount = amount;
        this.date = date;
        this.addressId = addressId;
        this.addressIdNumber = addressIdNumber;
        this.addressMail = addressMail;
    }

    public String getAmount(){ return this.amount;}

    public String getDate() {return this.date;}

    public String getAddressId() {
        return addressId;
    }

    public String getAddressIdNumber() {
        return addressIdNumber;
    }

    public String getAddressMail() {
        return addressMail;
    }
    public void setDate(String d){
        this.date = d;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setAddressIdNumber(String addressIdNumber) {
        this.addressIdNumber = addressIdNumber;
    }

    public void setAddressMail(String addressMail) {
        this.addressMail = addressMail;
    }


}
