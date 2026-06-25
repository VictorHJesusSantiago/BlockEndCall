package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivitySearchBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private final List<BlockedNumber> results = new ArrayList<>();
    private Adapter adapter;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(results, number -> {
            Intent intent = new Intent(this, NumberDetailActivity.class);
            intent.putExtra("number_id", number.getId());
            intent.putExtra("number_phone", number.getPhoneNumber());
            startActivity(intent);
        });
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                String query = s.toString().trim();
                if (query.isEmpty()) { results.clear(); adapter.notifyDataSetChanged(); return; }
                debounceRunnable = () -> search(query);
                debounceHandler.postDelayed(debounceRunnable, 400);
            }
        });
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void search(String query) {
        ApiClient.getInstance(this).getApi().searchNumbers(query, 0, 20).enqueue(new Callback<ApiResponse<PagedResponse<BlockedNumber>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Response<ApiResponse<PagedResponse<BlockedNumber>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    results.clear();
                    PagedResponse<BlockedNumber> page = response.body().getData();
                    if (page != null && page.getContent() != null) results.addAll(page.getContent());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SearchActivity.this, "Erro ao buscar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnItemClick { void onClick(BlockedNumber n); }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<BlockedNumber> data;
        private final OnItemClick listener;
        Adapter(List<BlockedNumber> data, OnItemClick listener) { this.data = data; this.listener = listener; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            BlockedNumber n = data.get(pos);
            h.tvPhone.setText(n.getPhoneNumber());
            h.tvCategory.setText(n.getCategory() != null ? n.getCategory() : "");
            h.tvReports.setText(n.getReportCount() + " reportes");
            h.itemView.setOnClickListener(v -> listener.onClick(n));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvPhone, tvCategory, tvReports;
            VH(View v) {
                super(v);
                tvPhone = v.findViewById(R.id.tv_phone);
                tvCategory = v.findViewById(R.id.tv_category);
                tvReports = v.findViewById(R.id.tv_reports);
            }
        }
    }
}
