package com.blockendcall.android.api;

import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.AuthResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.model.NumberCheckResult;
import com.blockendcall.android.model.Stats;
import com.blockendcall.android.model.UserReport;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface BlockedNumberApi {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/v1/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body Map<String, String> body);

    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body Map<String, String> body);

    // ── Number check (public) ────────────────────────────────────────────────
    @GET("api/v1/numbers/check/{phoneNumber}")
    Call<ApiResponse<NumberCheckResult>> checkNumber(@Path("phoneNumber") String phoneNumber);

    // ── Blocked numbers (auth) ───────────────────────────────────────────────
    @GET("api/v1/numbers")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> listNumbers(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @GET("api/v1/numbers/search")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> searchNumbers(
            @Query("q") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/numbers/category/{category}")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> listByCategory(
            @Path("category") String category,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/numbers/{id}")
    Call<ApiResponse<BlockedNumber>> getNumberById(@Path("id") long id);

    @POST("api/v1/numbers/report")
    Call<ApiResponse<BlockedNumber>> reportNumber(@Body Map<String, String> body);

    @POST("api/v1/numbers/{id}/false-positive")
    Call<ApiResponse<Void>> reportFalsePositive(
            @Path("id") long id,
            @Body Map<String, String> body
    );

    @DELETE("api/v1/numbers/{id}")
    Call<ApiResponse<Void>> deleteNumber(@Path("id") long id);

    // ── Stats (public) ───────────────────────────────────────────────────────
    @GET("api/v1/stats")
    Call<ApiResponse<Stats>> getStats();

    // ── User ─────────────────────────────────────────────────────────────────
    @GET("api/v1/users/me/reports")
    Call<ApiResponse<List<UserReport>>> getMyReports();
}
