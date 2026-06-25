package com.blockendcall.android.api;

import com.blockendcall.android.model.AdminUser;
import com.blockendcall.android.model.Announcement;
import com.blockendcall.android.model.ApiKeyModel;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.AuthResponse;
import com.blockendcall.android.model.Badge;
import com.blockendcall.android.model.BlockedCallLogEntry;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.model.LeaderboardEntry;
import com.blockendcall.android.model.NumberCheckResult;
import com.blockendcall.android.model.NumberReportedName;
import com.blockendcall.android.model.NumberTimeline;
import com.blockendcall.android.model.PersonalListEntry;
import com.blockendcall.android.model.PublicWhitelistEntry;
import com.blockendcall.android.model.Stats;
import com.blockendcall.android.model.UserPreference;
import com.blockendcall.android.model.UserProfile;
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

    // ── Blocked numbers ──────────────────────────────────────────────────────
    @GET("api/v1/numbers")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> listNumbers(
            @Query("page") int page, @Query("size") int size, @Query("sort") String sort);

    @GET("api/v1/numbers/search")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> searchNumbers(
            @Query("q") String query, @Query("page") int page, @Query("size") int size);

    @GET("api/v1/numbers/category/{category}")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> listByCategory(
            @Path("category") String category, @Query("page") int page, @Query("size") int size);

    @GET("api/v1/numbers/{id}")
    Call<ApiResponse<BlockedNumber>> getNumberById(@Path("id") long id);

    @POST("api/v1/numbers/report")
    Call<ApiResponse<BlockedNumber>> reportNumber(@Body Map<String, String> body);

    @POST("api/v1/numbers/{id}/false-positive")
    Call<ApiResponse<Void>> reportFalsePositive(@Path("id") long id, @Body Map<String, String> body);

    @DELETE("api/v1/numbers/{id}")
    Call<ApiResponse<Void>> deleteNumber(@Path("id") long id);

    @POST("api/v1/numbers/check-batch")
    Call<ApiResponse<List<NumberCheckResult>>> checkBatch(@Body Map<String, List<String>> body);

    // ── Me too ───────────────────────────────────────────────────────────────
    @POST("api/v1/numbers/{id}/confirm")
    Call<ApiResponse<Void>> meToo(@Path("id") long numberId);

    // ── Number enrichment ────────────────────────────────────────────────────
    @GET("api/v1/numbers/{id}/reported-names")
    Call<ApiResponse<List<NumberReportedName>>> getReportedNames(@Path("id") long id);

    @POST("api/v1/numbers/{id}/reported-names")
    Call<ApiResponse<Void>> submitReportedName(@Path("id") long id, @Body Map<String, String> body);

    @GET("api/v1/numbers/{id}/timeline")
    Call<ApiResponse<List<NumberTimeline>>> getTimeline(@Path("id") long id);

    // ── Stats (public) ───────────────────────────────────────────────────────
    @GET("api/v1/stats")
    Call<ApiResponse<Stats>> getStats();

    @GET("api/v1/stats/leaderboard")
    Call<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(@Query("limit") int limit);

    // ── User profile ─────────────────────────────────────────────────────────
    @GET("api/v1/users/me")
    Call<ApiResponse<UserProfile>> getMyProfile();

    @PUT("api/v1/users/me")
    Call<ApiResponse<UserProfile>> updateProfile(@Body Map<String, String> body);

    @POST("api/v1/users/me/password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> body);

    @GET("api/v1/users/me/reports")
    Call<ApiResponse<List<UserReport>>> getMyReports();

    @DELETE("api/v1/users/me/reports/{reportId}")
    Call<ApiResponse<Void>> deleteReport(@Path("reportId") long reportId);

    @DELETE("api/v1/users/me")
    Call<ApiResponse<Void>> deleteAccount(@Body Map<String, Object> body);

    @POST("api/v1/users/me/terms")
    Call<ApiResponse<Void>> acceptTerms();

    // ── Personal whitelist ───────────────────────────────────────────────────
    @GET("api/v1/users/me/personal-whitelist")
    Call<ApiResponse<List<PersonalListEntry>>> getPersonalWhitelist();

    @POST("api/v1/users/me/personal-whitelist")
    Call<ApiResponse<PersonalListEntry>> addToWhitelist(@Body Map<String, String> body);

    @DELETE("api/v1/users/me/personal-whitelist/{phone}")
    Call<ApiResponse<Void>> removeFromWhitelist(@Path("phone") String phone);

    // ── Personal blacklist ───────────────────────────────────────────────────
    @GET("api/v1/users/me/personal-blacklist")
    Call<ApiResponse<List<PersonalListEntry>>> getPersonalBlacklist();

    @POST("api/v1/users/me/personal-blacklist")
    Call<ApiResponse<PersonalListEntry>> addToBlacklist(@Body Map<String, String> body);

    @DELETE("api/v1/users/me/personal-blacklist/{phone}")
    Call<ApiResponse<Void>> removeFromBlacklist(@Path("phone") String phone);

    // ── Call log ─────────────────────────────────────────────────────────────
    @GET("api/v1/users/me/call-log")
    Call<ApiResponse<PagedResponse<BlockedCallLogEntry>>> getCallLog(
            @Query("page") int page, @Query("size") int size);

    // ── User preferences ─────────────────────────────────────────────────────
    @GET("api/v1/users/me/preferences")
    Call<ApiResponse<UserPreference>> getPreferences();

    @PUT("api/v1/users/me/preferences")
    Call<ApiResponse<UserPreference>> updatePreferences(@Body Map<String, Object> body);

    // ── Badges ───────────────────────────────────────────────────────────────
    @GET("api/v1/users/me/badges")
    Call<ApiResponse<List<Badge>>> getBadges();

    // ── API Keys ─────────────────────────────────────────────────────────────
    @GET("api/v1/users/me/api-keys")
    Call<ApiResponse<List<ApiKeyModel>>> getApiKeys();

    @POST("api/v1/users/me/api-keys")
    Call<ApiResponse<ApiKeyModel>> createApiKey(@Body Map<String, String> body);

    @DELETE("api/v1/users/me/api-keys/{keyId}")
    Call<ApiResponse<Void>> revokeApiKey(@Path("keyId") long keyId);

    // ── FCM ──────────────────────────────────────────────────────────────────
    @POST("api/v1/users/me/fcm")
    Call<ApiResponse<Void>> registerFcmToken(@Body Map<String, String> body);

    // ── Announcements (public) ───────────────────────────────────────────────
    @GET("api/v1/announcements")
    Call<ApiResponse<List<Announcement>>> getAnnouncements();

    // ── Public whitelist ─────────────────────────────────────────────────────
    @GET("api/v1/public-whitelist/check/{phone}")
    Call<ApiResponse<PublicWhitelistEntry>> checkPublicWhitelist(@Path("phone") String phone);

    // ── Admin ─────────────────────────────────────────────────────────────────
    @GET("api/v1/admin/users")
    Call<ApiResponse<PagedResponse<AdminUser>>> adminListUsers(
            @Query("page") int page, @Query("size") int size);

    @POST("api/v1/admin/users/suspend")
    Call<ApiResponse<Void>> adminSuspendUser(@Body Map<String, Object> body);

    @POST("api/v1/admin/users/promote")
    Call<ApiResponse<Void>> adminPromoteUser(@Body Map<String, Object> body);

    @GET("api/v1/admin/numbers/pending")
    Call<ApiResponse<PagedResponse<BlockedNumber>>> adminPendingNumbers(
            @Query("page") int page, @Query("size") int size);

    @POST("api/v1/admin/numbers/bulk-approve")
    Call<ApiResponse<Void>> adminBulkApprove(@Body Map<String, List<Long>> body);

    @POST("api/v1/admin/numbers/bulk-reject")
    Call<ApiResponse<Void>> adminBulkReject(@Body Map<String, List<Long>> body);
}
