package com.b2gsoft.jamalpurqrscanner.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.b2gsoft.jamalpurqrscanner.Interface.Connectivity;
import com.b2gsoft.jamalpurqrscanner.Model.ProductInfo;
import com.b2gsoft.jamalpurqrscanner.Repository.LoginRepository;
import com.b2gsoft.jamalpurqrscanner.Repository.ScannerRepository;


public class ScannerViewModel extends ViewModel {

    private Context context;
    private ScannerRepository scannerRepo = ScannerRepository.getInstance();
    private LoginRepository loginRepo = LoginRepository.getInstance();
    private LiveData<String> message = new MutableLiveData<>();
    private LiveData<ProductInfo> couponData = new MutableLiveData<>();
    private LiveData<Boolean> isValidating = new MutableLiveData<>();


    public void init(Context context, Connectivity connectivity) {
        this.context = context;
        loginRepo.resetValues();
        scannerRepo.init(context, connectivity);
        couponData = scannerRepo.getCouponData();
        isValidating = scannerRepo.getValidationCallStatus();
        message = scannerRepo.getMessage();
    }


    public void validateCode(String code) {
        scannerRepo.validateCode(code);
    }


    public void resetValues() {

        scannerRepo.resetValues();
    }


    public LiveData<Boolean> getValidationCallStatus() {
        return isValidating;
    }

    public LiveData<ProductInfo> getCouponData() {
        return couponData;
    }

    public LiveData<String> getMessage() {
        return message;
    }
}
