package com.b2gsoft.jamalpurqrscanner.View;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.b2gsoft.jamalpurqrscanner.Interface.Connectivity;
import com.b2gsoft.jamalpurqrscanner.Interface.Session;
import com.b2gsoft.jamalpurqrscanner.Model.ProductInfo;
import com.b2gsoft.jamalpurqrscanner.R;
import com.b2gsoft.jamalpurqrscanner.Utils.ConnectivityStatus;
import com.b2gsoft.jamalpurqrscanner.Utils.SessionValidation;
import com.b2gsoft.jamalpurqrscanner.Utils.SharedPreference;
import com.b2gsoft.jamalpurqrscanner.Utils.StaticValue;
import com.b2gsoft.jamalpurqrscanner.ViewModel.ScannerViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScannerActivity extends AppCompatActivity implements Connectivity, Session {

    private TextView tvScan;
    private RelativeLayout rooView;

    private ScannerViewModel scannerViewModel;
    private ProgressDialog progressDialog;

    private Snackbar snack;
    private SharedPreference sharedPreference;

    private ConnectivityStatus connectivityStatus;
    private Session session;

    private Connectivity connectivity;
    private SessionValidation sessionValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        sharedPreference = new SharedPreference(this);

        connectivity = this;
        session = this;

        connectivityStatus = new ConnectivityStatus();
        sessionValidation = new SessionValidation(this, session);

        scannerViewModel = ViewModelProviders.of(this).get(ScannerViewModel.class);
        scannerViewModel.init(this, connectivity);

        tvScan = (TextView) findViewById(R.id.tv_scan);
        rooView = (RelativeLayout) findViewById(R.id.root_view);


        scannerViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String response) {

                showSnackBar(response);
            }
        });


        scannerViewModel.getCouponData().observe(this, new Observer<ProductInfo>() {
            @Override
            public void onChanged(@Nullable ProductInfo productInfo) {

                if(productInfo != null && !productInfo.getName().isEmpty()) {

                    Intent intent = new Intent(ScannerActivity.this, CouponInfoActivity.class);
                    intent.putExtra(StaticValue.CouponInfo, productInfo);
                    startActivity(intent);
                }
            }
        });


        scannerViewModel.getValidationCallStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isValidating) {

                if(isValidating != null) {

                    if(isValidating) {
                        showProgressDialog();
                    }
                    else {
                        dismissProgressDialog();
                    }
                }
            }
        });


        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(snack != null && snack.isShownOrQueued()) {

                    snack.dismiss();
                }

                IntentIntegrator integrator = new IntentIntegrator((Activity) v.getContext());
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt(getString(R.string.scanning));
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.logout:

                openLoginScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void openLoginScreen() {

        sharedPreference.clearUserData();
        scannerViewModel.resetValues();

        Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {

            if(result.getContents() != null) {

                Log.e("Code ", result.getContents());
                scannerViewModel.validateCode(result.getContents());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void showSnackBar(String message) {

        snack = Snackbar.make(rooView, message, 3000);

        View view = snack.getView();
        view.setBackgroundColor(getResources().getColor(R.color.white));

        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        tv.setTypeface(null, Typeface.BOLD);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else
        {
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snack.show();
    }


    private void showProgressDialog() {

        try {
            progressDialog.setMessage(getString(R.string.validating_code));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
        catch (Exception e) {}
    }


    private void dismissProgressDialog() {

        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    @Override
    protected void onPause() {

        unregisterReceiver(connectivityStatus);
        unregisterReceiver(sessionValidation);
        super.onPause();
    }


    @Override
    protected void onResume() {

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityStatus, intentFilter);
        registerReceiver(sessionValidation, intentFilter);
        super.onResume();
    }


    @Override
    public void onDestroy() {

        dismissProgressDialog();
        super.onDestroy();
    }


    @Override
    public void notConnected() {

        showSnackBar(getString(R.string.not_connected));
    }


    @Override
    public void noActiveConnection() {

        showSnackBar(getString(R.string.no_active_connection));
    }


    @Override
    public void onSessionEnd() {

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.session_end);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Button okay = (Button) dialog.findViewById(R.id.btn_okay);

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                openLoginScreen();
            }
        });

        dialog.show();
    }
}
