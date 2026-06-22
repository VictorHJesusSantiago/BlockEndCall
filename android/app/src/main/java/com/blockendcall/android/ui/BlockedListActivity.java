package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivityBlockedListBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.ui.adapter.BlockedNumberAdapter;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedListActivity extends AppCompatActivity {

    private ActivityBlockedListBinding binding;
    private BlockedNumberAdapter adapter;
    private BlockedNumberApi api;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new BlockedNumberAdapter(number -> {
            Intent intent = new Intent(this, NumberDetailActivity.class);
            intent.putExtra("number_id", number.getId());
            intent.putExtra("number_phone", number.getPhoneNumber());
            startActivity(intent);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 0;
            adapter.clear();
            loadPage();
        });

        binding.fabReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportNumberActivity.class)));

        loadPage();
    }

    private void loadPage() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.listNumbers(currentPage, 20, "reportCount,desc")
                .enqueue(new Callback<ApiResponse<PagedResponse<BlockedNumber>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PagedResponse<BlockedNumber>>> call,
                                           Response<ApiResponse<PagedResponse<BlockedNumber>>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            PagedResponse<BlockedNumber> page = response.body().getData();
                            adapter.addAll(page.getContent());

                            if (!page.isLast()) {
                                currentPage++;
                            }
                        } else {
                            Toast.makeText(BlockedListActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        Toast.makeText(BlockedListActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
