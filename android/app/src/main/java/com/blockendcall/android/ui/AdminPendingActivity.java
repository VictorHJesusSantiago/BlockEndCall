package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivityAdminPendingBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPendingActivity extends AppCompatActivity {

    private ActivityAdminPendingBinding binding;
    private final List<BlockedNumber> items = new ArrayList<>();
    private final Set<Long> selected = new HashSet<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items, selected);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnApprove.setOnClickListener(v -> bulkApprove());
        binding.btnReject.setOnClickListener(v -> bulkReject());
        loadPending();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadPending() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApi().adminPendingNumbers(0, 100).enqueue(new Callback<ApiResponse<PagedResponse<BlockedNumber>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Response<ApiResponse<PagedResponse<BlockedNumber>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    selected.clear();
                    PagedResponse<BlockedNumber> page = response.body().getData();
                    if (page != null && page.getContent() != null) items.addAll(page.getContent());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminPendingActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<BlockedNumber>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminPendingActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bulkApprove() {
        if (selected.isEmpty()) { Toast.makeText(this, "Selecione pelo menos um número", Toast.LENGTH_SHORT).show(); return; }
        Map<String, List<Long>> body = new HashMap<>();
        body.put("ids", new ArrayList<>(selected));
        ApiClient.getInstance(this).getApi().adminBulkApprove(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(AdminPendingActivity.this, "Aprovados com sucesso", Toast.LENGTH_SHORT).show();
                loadPending();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminPendingActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bulkReject() {
        if (selected.isEmpty()) { Toast.makeText(this, "Selecione pelo menos um número", Toast.LENGTH_SHORT).show(); return; }
        Map<String, List<Long>> body = new HashMap<>();
        body.put("ids", new ArrayList<>(selected));
        ApiClient.getInstance(this).getApi().adminBulkReject(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(AdminPendingActivity.this, "Rejeitados com sucesso", Toast.LENGTH_SHORT).show();
                loadPending();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminPendingActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<BlockedNumber> data;
        private final Set<Long> selected;
        Adapter(List<BlockedNumber> data, Set<Long> selected) { this.data = data; this.selected = selected; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_pending, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            BlockedNumber n = data.get(pos);
            h.tvPhone.setText(n.getPhoneNumber());
            h.tvCategory.setText(n.getCategory() != null ? n.getCategory() : "");
            h.tvReports.setText(n.getReportCount() + " reportes");
            h.checkBox.setChecked(n.getId() != null && selected.contains(n.getId()));
            h.checkBox.setOnCheckedChangeListener((btn, checked) -> {
                if (n.getId() == null) return;
                if (checked) selected.add(n.getId());
                else selected.remove(n.getId());
            });
            h.itemView.setOnClickListener(v -> h.checkBox.setChecked(!h.checkBox.isChecked()));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvPhone, tvCategory, tvReports;
            CheckBox checkBox;
            VH(View v) {
                super(v);
                tvPhone = v.findViewById(R.id.tv_phone);
                tvCategory = v.findViewById(R.id.tv_category);
                tvReports = v.findViewById(R.id.tv_reports);
                checkBox = v.findViewById(R.id.cb_select);
            }
        }
    }
}
