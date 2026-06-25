package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.databinding.ActivityDeleteAccountBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.util.SessionManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnDelete.setOnClickListener(v -> attemptDelete());
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void attemptDelete() {
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        if (password.isEmpty()) {
            Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!binding.cbConfirm.isChecked()) {
            Toast.makeText(this, "Marque a confirmação", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (d, w) -> performDelete(password))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performDelete(String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("password", password);
        body.put("confirmDelete", true);
        ApiClient.getInstance(this).getApi().deleteAccount(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    new SessionManager(DeleteAccountActivity.this).clearSession();
                    Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(DeleteAccountActivity.this, "Senha incorreta ou erro", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(DeleteAccountActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
