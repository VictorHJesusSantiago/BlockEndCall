package com.blockendcall.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityProfileBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.UserProfile;
import com.blockendcall.android.model.UserReport;
import com.blockendcall.android.util.NotificationHelper;
import com.blockendcall.android.util.SessionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private BlockedNumberApi api;
    private SessionManager session;
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        api = ApiClient.getApi(session);

        binding.btnEditProfile.setOnClickListener(v -> showEditNameDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnMyReports.setOnClickListener(v ->
                startActivity(new Intent(this, MyReportsActivity.class)));

        loadProfile();
    }

    private void loadProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getMyProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call,
                                   Response<ApiResponse<UserProfile>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentProfile = response.body().getData();
                    bindProfile(currentProfile);
                } else {
                    Toast.makeText(ProfileActivity.this, "Falha ao carregar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfile(UserProfile profile) {
        String initials = profile.getName() != null && !profile.getName().isEmpty()
                ? String.valueOf(profile.getName().charAt(0)).toUpperCase()
                : "?";
        binding.tvAvatar.setText(initials);
        binding.tvName.setText(profile.getName());
        binding.tvEmail.setText(profile.getEmail());
        binding.tvRole.setText("ADMIN".equals(profile.getRole()) ? "👑 Admin" : "👤 Usuário");
        binding.tvTotalReports.setText(String.valueOf(profile.getTotalReports()));
        binding.tvPhone.setText(profile.getPhone().isEmpty() ? "Não informado" : profile.getPhone());

        String since = profile.getCreatedAt();
        binding.tvMemberSince.setText(since.length() >= 7 ? since.substring(0, 7) : since);

        checkAndNotifyConfirmedReports();
    }

    private void checkAndNotifyConfirmedReports() {
        SharedPreferences seenPrefs = getSharedPreferences("blockendcall_notifications", MODE_PRIVATE);
        Set<String> seen = new HashSet<>(seenPrefs.getStringSet("seen_confirmed", new HashSet<>()));

        api.getMyReports().enqueue(new Callback<ApiResponse<List<UserReport>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserReport>>> call,
                                   Response<ApiResponse<List<UserReport>>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) return;
                List<UserReport> reports = response.body().getData();
                if (reports == null) return;
                Set<String> newSeen = new HashSet<>(seen);
                for (UserReport r : reports) {
                    if (r.isConfirmed() && r.getPhoneNumber() != null && !seen.contains(r.getPhoneNumber())) {
                        NotificationHelper.showReportConfirmedNotification(ProfileActivity.this, r.getPhoneNumber());
                        newSeen.add(r.getPhoneNumber());
                    }
                }
                seenPrefs.edit().putStringSet("seen_confirmed", newSeen).apply();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserReport>>> call, Throwable t) {}
        });
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setHint("Novo nome");
        if (currentProfile != null) input.setText(currentProfile.getName());
        input.setPadding(48, 32, 48, 16);

        new AlertDialog.Builder(this)
                .setTitle("Editar Nome")
                .setView(input)
                .setPositiveButton("Salvar", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) updateProfile(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateProfile(String name) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);

        api.updateProfile(body).enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call,
                                   Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentProfile = response.body().getData();
                    bindProfile(currentProfile);
                    session.saveUserName(currentProfile.getName());
                    Toast.makeText(ProfileActivity.this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Falha ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(
                android.R.layout.simple_list_item_2, null);

        EditText etCurrent = new EditText(this);
        etCurrent.setHint("Senha atual");
        etCurrent.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText etNew = new EditText(this);
        etNew.setHint("Nova senha (mín. 6 caracteres)");
        etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);
        layout.addView(etCurrent);
        layout.addView(etNew);

        new AlertDialog.Builder(this)
                .setTitle("Alterar Senha")
                .setView(layout)
                .setPositiveButton("Alterar", (d, w) -> {
                    String current = etCurrent.getText().toString();
                    String newPass = etNew.getText().toString();
                    if (newPass.length() < 6) {
                        Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    changePassword(current, newPass);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void changePassword(String current, String newPass) {
        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", current);
        body.put("newPassword", newPass);

        api.changePassword(body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Falha ao alterar senha";
                    Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
