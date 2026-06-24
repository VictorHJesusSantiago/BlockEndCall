package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityMyReportsBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.UserReport;
import com.blockendcall.android.ui.adapter.UserReportAdapter;
import com.blockendcall.android.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReportsActivity extends AppCompatActivity {

    private ActivityMyReportsBinding binding;
    private UserReportAdapter adapter;
    private BlockedNumberApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new UserReportAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright, android.R.color.holo_orange_light);
        binding.swipeRefresh.setOnRefreshListener(this::loadReports);

        loadReports();
    }

    private void loadReports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        api.getMyReports().enqueue(new Callback<ApiResponse<List<UserReport>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserReport>>> call,
                                   Response<ApiResponse<List<UserReport>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserReport> reports = response.body().getData();
                    adapter.setItems(reports);
                    binding.layoutEmpty.setVisibility(reports.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(MyReportsActivity.this, "Falha ao carregar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserReport>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(MyReportsActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
