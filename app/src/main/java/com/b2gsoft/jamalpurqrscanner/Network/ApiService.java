package com.b2gsoft.jamalpurqrscanner.Network;

import com.b2gsoft.jamalpurqrscanner.Model.LoginResponse;
import com.b2gsoft.jamalpurqrscanner.Model.User;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("salesmen/login")
    Call<JsonObject> login(@Body User user);

    @Headers("Accept: application/json")
    @POST("salesmen/coupon-validation-check")
    Call<JsonObject> validateCode(@Header("Authorization") String token, @Query("qrcode") String code);
}