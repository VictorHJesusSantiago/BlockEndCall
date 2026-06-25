package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivityAdminUsersBinding;
import com.blockendcall.android.model.AdminUser;
import com.blockendcall.android.model.ApiResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersActivity extends AppCompatActivity {

    private ActivityAdminUsersBinding binding;
    private final List<AdminUser> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items, this::showUserDialog);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        loadUsers();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApi().adminListUsers(0, 50).enqueue(new Callback<ApiResponse<PagedResponse<AdminUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<AdminUser>>> call, Response<ApiResponse<PagedResponse<AdminUser>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    PagedResponse<AdminUser> page = response.body().getData();
                    if (page != null && page.getContent() != null) items.addAll(page.getContent());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminUsersActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<AdminUser>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUsersActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(AdminUser user) {
        String[] options = {"Suspender Usuário", "Promover a Moderador", "Cancelar"};
        new AlertDialog.Builder(this)
                .setTitle(user.getName())
                .setItems(options, (d, which) -> {
                    if (which == 0) suspendUser(user);
                    else if (which == 1) promoteUser(user);
                })
                .show();
    }

    private void suspendUser(AdminUser user) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", user.getId());
        body.put("suspended", true);
        ApiClient.getInstance(this).getApi().adminSuspendUser(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(AdminUsersActivity.this, "Usuário suspenso", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminUsersActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void promoteUser(AdminUser user) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", user.getId());
        body.put("role", "MODERATOR");
        ApiClient.getInstance(this).getApi().adminPromoteUser(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(AdminUsersActivity.this, "Usuário promovido", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AdminUsersActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnItemClick { void onClick(AdminUser u); }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<AdminUser> data;
        private final OnItemClick listener;
        Adapter(List<AdminUser> data, OnItemClick listener) { this.data = data; this.listener = listener; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            AdminUser u = data.get(pos);
            h.tvName.setText(u.getName() != null ? u.getName() : "");
            h.tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");
            h.tvRoleBadge.setText(u.getRole() != null ? u.getRole() : "USER");
            h.tvStatus.setText(u.isSuspended() ? "Suspenso" : u.isActive() ? "Ativo" : "Inativo");
            h.tvStatus.setTextColor(u.isSuspended() ? 0xFFC62828 : 0xFF2E7D32);
            h.tvReports.setText(u.getTotalReports() + " reportes");
            h.itemView.setOnClickListener(v -> listener.onClick(u));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRoleBadge, tvStatus, tvReports;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_name);
                tvEmail = v.findViewById(R.id.tv_email);
                tvRoleBadge = v.findViewById(R.id.tv_role_badge);
                tvStatus = v.findViewById(R.id.tv_status);
                tvReports = v.findViewById(R.id.tv_reports);
            }
        }
    }
}
