package com.blockendcall.android.api;

import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.AuthResponse;
import com.blockendcall.android.model.BlockedNumber;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface BlockedNumberApi {

    @POST("api/v1/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body Map<String, String> body);

    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body Map<String, String> body);

    @GET("api/v1/numbers/check/{phoneNumber}")
    Call<ApiResponse<BlockedNumber>> checkNumber(@Path("phoneNumber") String phoneNumber);

    @GET("api/v1/numbers")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> listNumbers(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @GET("api/v1/numbers/{id}")
    Call<ApiResponse<BlockedNumber>> getNumberById(@Path("id") long id);

    @POST("api/v1/numbers/report")
    Call<ApiResponse<BlockedNumber>> reportNumber(@Body Map<String, String> body);

    @DELETE("api/v1/numbers/{id}")
    Call<ApiResponse<Void>> deleteNumber(@Path("id") long id);
}
