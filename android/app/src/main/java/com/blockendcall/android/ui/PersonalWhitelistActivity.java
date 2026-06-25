package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.databinding.ActivityPersonalWhitelistBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.PersonalListEntry;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalWhitelistActivity extends AppCompatActivity {

    private ActivityPersonalWhitelistBinding binding;
    private final List<PersonalListEntry> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalWhitelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items, this::removeItem);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(v -> showAddDialog());
        loadList();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadList() {
        ApiClient.getInstance(this).getApi().getPersonalWhitelist().enqueue(new Callback<ApiResponse<List<PersonalListEntry>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PersonalListEntry>>> call, Response<ApiResponse<List<PersonalListEntry>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    List<PersonalListEntry> data = response.body().getData();
                    if (data != null) items.addAll(data);
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<PersonalListEntry>>> call, Throwable t) {
                Toast.makeText(PersonalWhitelistActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);
        EditText etPhone = new EditText(this);
        etPhone.setHint("Número de telefone");
        EditText etNote = new EditText(this);
        etNote.setHint("Observação (opcional)");
        layout.addView(etPhone);
        layout.addView(etNote);
        new AlertDialog.Builder(this)
                .setTitle("Adicionar à Whitelist")
                .setView(layout)
                .setPositiveButton("Adicionar", (d, w) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (phone.isEmpty()) return;
                    Map<String, String> body = new HashMap<>();
                    body.put("phoneNumber", phone);
                    body.put("note", etNote.getText().toString().trim());
                    ApiClient.getInstance(this).getApi().addToWhitelist(body).enqueue(new Callback<ApiResponse<PersonalListEntry>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<PersonalListEntry>> call, Response<ApiResponse<PersonalListEntry>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(PersonalWhitelistActivity.this, "Adicionado", Toast.LENGTH_SHORT).show();
                                loadList();
                            } else {
                                Toast.makeText(PersonalWhitelistActivity.this, "Erro ao adicionar", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<PersonalListEntry>> call, Throwable t) {
                            Toast.makeText(PersonalWhitelistActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeItem(PersonalListEntry entry) {
        ApiClient.getInstance(this).getApi().removeFromWhitelist(entry.getPhoneNumber()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(PersonalWhitelistActivity.this, "Removido", Toast.LENGTH_SHORT).show();
                loadList();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(PersonalWhitelistActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnDeleteClick { void onDelete(PersonalListEntry e); }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<PersonalListEntry> data;
        private final OnDeleteClick listener;
        Adapter(List<PersonalListEntry> data, OnDeleteClick listener) {
            this.data = data; this.listener = listener;
        }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_list, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            PersonalListEntry e = data.get(pos);
            h.tvPhone.setText(e.getPhoneNumber());
            h.tvNote.setText(e.getNote() != null ? e.getNote() : "");
            h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvPhone, tvNote;
            MaterialButton btnDelete;
            VH(View v) {
                super(v);
                tvPhone = v.findViewById(R.id.tv_phone);
                tvNote = v.findViewById(R.id.tv_note);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}
