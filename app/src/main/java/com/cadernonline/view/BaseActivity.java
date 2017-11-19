package com.cadernonline.view;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.cadernonline.R;

public class BaseActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    protected void showError(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showProgress(){
        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage(getString(R.string.loading_media));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void hideProgress(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}