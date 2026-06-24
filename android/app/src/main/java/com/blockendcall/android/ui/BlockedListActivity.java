package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blockendcall.android.R;
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
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private String activeCategory = null;
    private String activeSearch = null;

    private final Handler searchDebounce = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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

        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        binding.swipeRefresh.setOnRefreshListener(this::resetAndLoad);

        binding.fabReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportNumberActivity.class)));

        // Search with 400ms debounce
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchDebounce.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    activeSearch = s.toString().trim().isEmpty() ? null : s.toString().trim();
                    resetAndLoad();
                };
                searchDebounce.postDelayed(searchRunnable, 400);
            }
        });

        // Category chip filter
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_filter_all)          activeCategory = null;
            else if (id == R.id.chip_filter_telemarketing) activeCategory = "TELEMARKETING";
            else if (id == R.id.chip_filter_scam)    activeCategory = "SCAM";
            else if (id == R.id.chip_filter_robocall) activeCategory = "ROBOCALL";
            else if (id == R.id.chip_filter_debt)    activeCategory = "DEBT_COLLECTOR";
            else if (id == R.id.chip_filter_phishing) activeCategory = "PHISHING";
            else                                     activeCategory = null;
            resetAndLoad();
        });

        loadPage();
    }

    private void resetAndLoad() {
        currentPage = 0;
        isLastPage = false;
        adapter.clear();
        loadPage();
    }

    private void loadPage() {
        if (isLoading || isLastPage) return;
        isLoading = true;
        binding.progressBar.setVisibility(currentPage == 0 ? View.VISIBLE : View.GONE);

        Call<ApiResponse<PagedResponse<BlockedNumber>>> call;

        if (activeSearch != null && !activeSearch.isEmpty()) {
            call = api.searchNumbers(activeSearch, currentPage, 20);
        } else if (activeCategory != null) {
            call = api.listByCategory(activeCategory, currentPage, 20);
        } else {
            call = api.listNumbers(currentPage, 20, "reportCount,desc");
        }

        call.enqueue(new Callback<ApiResponse<PagedResponse<BlockedNumber>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<BlockedNumber>>> call,
                                   Response<ApiResponse<PagedResponse<BlockedNumber>>> response) {
                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PagedResponse<BlockedNumber> page = response.body().getData();
                    adapter.addAll(page.getContent());
                    isLastPage = page.isLast();
                    if (!isLastPage) currentPage++;

                    binding.layoutEmpty.setVisibility(
                            adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(BlockedListActivity.this, "Falha ao carregar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Throwable t) {
                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(BlockedListActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.getItemCount() == 0 && !isLoading) loadPage();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
