package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityStatsBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.Stats;
import com.blockendcall.android.ui.adapter.BlockedNumberAdapter;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsActivity extends AppCompatActivity {

    private ActivityStatsBinding binding;
    private BlockedNumberApi api;
    private BlockedNumberAdapter trendingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        trendingAdapter = new BlockedNumberAdapter(number -> {});
        binding.rvTrending.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTrending.setAdapter(trendingAdapter);
        binding.rvTrending.setNestedScrollingEnabled(false);

        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright, android.R.color.holo_green_light);
        binding.swipeRefresh.setOnRefreshListener(this::loadStats);

        loadStats();
    }

    private void loadStats() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getStats().enqueue(new Callback<ApiResponse<Stats>>() {
            @Override
            public void onResponse(Call<ApiResponse<Stats>> call,
                                   Response<ApiResponse<Stats>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bindStats(response.body().getData());
                } else {
                    Toast.makeText(StatsActivity.this, "Falha ao carregar estatísticas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Stats>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(StatsActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStats(Stats stats) {
        binding.tvTotalConfirmed.setText(String.valueOf(stats.getTotalConfirmed()));
        binding.tvTotalPending.setText(String.valueOf(stats.getTotalPending()));
        binding.tvTotalReports.setText(String.valueOf(stats.getTotalReports()));
        binding.tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));

        if (stats.getByCategory() != null) {
            binding.tvCountTelemarketing.setText(fmt(stats.getByCategory().get("TELEMARKETING")));
            binding.tvCountScam.setText(fmt(stats.getByCategory().get("SCAM")));
            binding.tvCountRobocall.setText(fmt(stats.getByCategory().get("ROBOCALL")));
            binding.tvCountDebt.setText(fmt(stats.getByCategory().get("DEBT_COLLECTOR")));
            binding.tvCountPhishing.setText(fmt(stats.getByCategory().get("PHISHING")));
            binding.tvCountUnknown.setText(fmt(stats.getByCategory().get("UNKNOWN")));
        }

        trendingAdapter.clear();
        if (stats.getTrending() != null && !stats.getTrending().isEmpty()) {
            trendingAdapter.addAll(stats.getTrending());
            binding.layoutTrending.setVisibility(View.VISIBLE);
        } else {
            binding.layoutTrending.setVisibility(View.GONE);
        }
    }

    private String fmt(Long v) { return v != null ? String.valueOf(v) : "0"; }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
