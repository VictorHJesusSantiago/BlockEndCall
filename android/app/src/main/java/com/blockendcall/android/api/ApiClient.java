package com.blockendcall.android.api;

import com.blockendcall.android.BuildConfig;
import com.blockendcall.android.util.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit retrofit;
    private static BlockedNumberApi api;

    public static BlockedNumberApi getApi(SessionManager session) {
        if (api == null) {
            api = buildRetrofit(session).create(BlockedNumberApi.class);
        }
        return api;
    }

    public static void reset() {
        retrofit = null;
        api = null;
    }

    private static Retrofit buildRetrofit(SessionManager session) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    String token = session.getToken();
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
