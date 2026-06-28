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
import com.blockendcall.android.databinding.ActivityReportedNamesBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.NumberReportedName;
import com.blockendcall.android.util.SessionManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportedNamesActivity extends AppCompatActivity {

    private ActivityReportedNamesBinding binding;
    private BlockedNumberApi api;
    private NamesAdapter adapter;
    private long numberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportedNamesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        numberId = getIntent().getLongExtra("number_id", -1);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new NamesAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> showAddNameDialog());

        if (numberId != -1) loadNames();
    }

    private void loadNames() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        api.getReportedNames(numberId).enqueue(new Callback<ApiResponse<List<NumberReportedName>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NumberReportedName>>> call,
                                   Response<ApiResponse<List<NumberReportedName>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<NumberReportedName> list = response.body().getData();
                    adapter.setItems(list);
                    binding.tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(ReportedNamesActivity.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NumberReportedName>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ReportedNamesActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddNameDialog() {
        EditText etName = new EditText(this);
        etName.setHint("Nome que o chamador usou");
        etName.setPadding(48, 32, 48, 16);

        new AlertDialog.Builder(this)
                .setTitle("Reportar Nome")
                .setView(etName)
                .setPositiveButton("Enviar", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Digite um nome", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitName(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void submitName(String name) {
        Map<String, String> body = new HashMap<>();
        body.put("reportedName", name);

        api.submitReportedName(numberId, body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ReportedNamesActivity.this, "Nome relatado!", Toast.LENGTH_SHORT).show();
                    loadNames();
                } else {
                    Toast.makeText(ReportedNamesActivity.this, "Erro ao enviar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ReportedNamesActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private static class NamesAdapter extends RecyclerView.Adapter<NamesAdapter.VH> {

        private List<NumberReportedName> items = new ArrayList<>();

        void setItems(List<NumberReportedName> list) {
            items = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.blockendcall.android.R.layout.item_reported_name, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            NumberReportedName item = items.get(position);
            holder.tvName.setText(item.getReportedName());
            holder.chipCount.setText(item.getReportCount() + " relatos");
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName;
            Chip chipCount;

            VH(View v) {
                super(v);
                tvName = v.findViewById(com.blockendcall.android.R.id.tv_name);
                chipCount = v.findViewById(com.blockendcall.android.R.id.chip_count);
            }
        }
    }
}
