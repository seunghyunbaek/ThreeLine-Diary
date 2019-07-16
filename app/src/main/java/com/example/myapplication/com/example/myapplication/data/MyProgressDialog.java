package com.example.myapplication.com.example.myapplication.data;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {
    ProgressDialog progressDialog;

    public MyProgressDialog(Context mContext, String title) {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    public void dismiss() {
        progressDialog.dismiss();
    }
}
