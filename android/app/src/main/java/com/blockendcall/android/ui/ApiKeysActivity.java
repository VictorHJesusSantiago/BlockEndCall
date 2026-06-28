package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityApiKeysBinding;
import com.blockendcall.android.model.ApiKey;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiKeysActivity extends AppCompatActivity {

    private ActivityApiKeysBinding binding;
    private BlockedNumberApi api;
    private ApiKeysAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApiKeysBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new ApiKeysAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> showCreateKeyDialog());

        loadKeys();
    }

    private void loadKeys() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getApiKeys().enqueue(new Callback<ApiResponse<List<ApiKey>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ApiKey>>> call,
                                   Response<ApiResponse<List<ApiKey>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setItems(response.body().getData());
                } else {
                    Toast.makeText(ApiKeysActivity.this, "Erro ao carregar chaves", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ApiKey>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ApiKeysActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateKeyDialog() {
        EditText etLabel = new EditText(this);
        etLabel.setHint("Label da chave");
        etLabel.setPadding(48, 32, 48, 16);

        new AlertDialog.Builder(this)
                .setTitle("Criar Chave de API")
                .setView(etLabel)
                .setPositiveButton("Criar", (d, w) -> {
                    String label = etLabel.getText().toString().trim();
                    if (label.isEmpty()) {
                        Toast.makeText(this, "Digite um label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createKey(label);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createKey(String label) {
        Map<String, String> body = new HashMap<>();
        body.put("label", label);

        api.createApiKey(body).enqueue(new Callback<ApiResponse<ApiKey>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiKey>> call, Response<ApiResponse<ApiKey>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiKey key = response.body().getData();
                    new AlertDialog.Builder(ApiKeysActivity.this)
                            .setTitle("Chave Criada!")
                            .setMessage("Guarde esta chave, ela não será exibida novamente:\n\n" + key.getKeyValue())
                            .setPositiveButton("Copiar e fechar", (d, w) -> {
                                android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                        getSystemService(CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("api_key", key.getKeyValue()));
                                Toast.makeText(ApiKeysActivity.this, "Chave copiada!", Toast.LENGTH_SHORT).show();
                                loadKeys();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(ApiKeysActivity.this, "Erro ao criar chave", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiKey>> call, Throwable t) {
                Toast.makeText(ApiKeysActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revokeKey(ApiKey key) {
        new AlertDialog.Builder(this)
                .setTitle("Revogar Chave")
                .setMessage("Revogar a chave \"" + key.getLabel() + "\"? Ela não poderá mais ser usada.")
                .setPositiveButton("Revogar", (d, w) -> {
                    api.revokeApiKey(key.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(ApiKeysActivity.this, "Chave revogada", Toast.LENGTH_SHORT).show();
                                loadKeys();
                            } else {
                                Toast.makeText(ApiKeysActivity.this, "Erro ao revogar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(ApiKeysActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private class ApiKeysAdapter extends RecyclerView.Adapter<ApiKeysAdapter.VH> {

        private List<ApiKey> items = new ArrayList<>();

        void setItems(List<ApiKey> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.blockendcall.android.R.layout.item_api_key, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ApiKey key = items.get(position);
            holder.tvLabel.setText(key.getLabel());
            String keyVal = key.getKeyValue();
            String masked = keyVal != null && keyVal.length() > 8
                    ? keyVal.substring(0, 8) + "..."
                    : (keyVal != null ? keyVal : "");
            holder.tvKeyMasked.setText(masked);
            holder.tvStatus.setText(key.isActive() ? "ATIVA" : "REVOGADA");
            holder.tvStatus.setTextColor(key.isActive()
                    ? android.graphics.Color.parseColor("#2E7D32")
                    : android.graphics.Color.RED);
            String date = key.getCreatedAt() != null ? key.getCreatedAt() : "";
            holder.tvCreatedAt.setText(date.length() >= 10 ? date.substring(0, 10) : date);

            holder.itemView.setOnLongClickListener(v -> {
                if (key.isActive()) revokeKey(key);
                return true;
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvLabel, tvKeyMasked, tvStatus, tvCreatedAt;

            VH(View v) {
                super(v);
                tvLabel = v.findViewById(com.blockendcall.android.R.id.tv_label);
                tvKeyMasked = v.findViewById(com.blockendcall.android.R.id.tv_key_masked);
                tvStatus = v.findViewById(com.blockendcall.android.R.id.tv_status);
                tvCreatedAt = v.findViewById(com.blockendcall.android.R.id.tv_created_at);
            }
        }
    }
}
