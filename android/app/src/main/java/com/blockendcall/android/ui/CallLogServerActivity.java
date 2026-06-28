package com.blockendcall.android.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivityCallLogServerBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedCallLog;
import com.blockendcall.android.util.SessionManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallLogServerActivity extends AppCompatActivity {

    private ActivityCallLogServerBinding binding;
    private BlockedNumberApi api;
    private CallLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallLogServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new CallLogAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        loadCallLog();
    }

    private void loadCallLog() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getCallLog(0, 50).enqueue(new Callback<ApiResponse<PagedResponse<BlockedCallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<BlockedCallLog>>> call,
                                   Response<ApiResponse<PagedResponse<BlockedCallLog>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PagedResponse<BlockedCallLog> page = response.body().getData();
                    List<BlockedCallLog> list = page.getContent();
                    adapter.setItems(list);
                    binding.tvTotalCount.setText(String.valueOf(page.getTotalElements()));
                } else {
                    Toast.makeText(CallLogServerActivity.this, "Erro ao carregar log", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<BlockedCallLog>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CallLogServerActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private static class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.VH> {

        private List<BlockedCallLog> items = new ArrayList<>();

        void setItems(List<BlockedCallLog> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.blockendcall.android.R.layout.item_call_log, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            BlockedCallLog entry = items.get(position);
            holder.tvPhone.setText(entry.getPhoneNumber() != null ? entry.getPhoneNumber() : "—");
            String date = entry.getBlockedAt() != null ? entry.getBlockedAt() : "";
            holder.tvBlockedAt.setText(date.length() >= 16 ? date.substring(0, 16) : date);

            String result = entry.getBlockResult() != null ? entry.getBlockResult() : "BLOCKED";
            holder.chipResult.setText(result);
            switch (result) {
                case "REJECTED":
                    holder.chipResult.setChipBackgroundColorResource(android.R.color.holo_red_light);
                    break;
                case "SILENCED":
                    holder.chipResult.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                    break;
                default:
                    holder.chipResult.setChipBackgroundColorResource(android.R.color.darker_gray);
                    break;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvPhone, tvBlockedAt;
            Chip chipResult;

            VH(View v) {
                super(v);
                tvPhone = v.findViewById(com.blockendcall.android.R.id.tv_phone);
                tvBlockedAt = v.findViewById(com.blockendcall.android.R.id.tv_blocked_at);
                chipResult = v.findViewById(com.blockendcall.android.R.id.chip_result);
            }
        }
    }
}
