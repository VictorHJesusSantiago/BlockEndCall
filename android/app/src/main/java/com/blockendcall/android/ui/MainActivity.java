package com.blockendcall.android.ui;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blockendcall.android.R;
import com.blockendcall.android.databinding.ActivityMainBinding;
import com.blockendcall.android.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager session;

    private final ActivityResultLauncher<Intent> roleRequestLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    binding.tvScreeningStatus.setText("Triagem ativa — chamadas spam serão bloqueadas");
                    binding.ivShieldStatus.setImageResource(
                            com.blockendcall.android.R.drawable.ic_shield);
                    Toast.makeText(this, "Triagem de chamadas ativada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permissão negada — chamadas não serão bloqueadas automaticamente",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        binding.tvWelcome.setText("Olá, " + session.getUserName() + "!");

        setSupportActionBar(binding.toolbar);

        updateScreeningStatus();
        requestNotificationPermission();

        binding.cardCheck.setOnClickListener(v ->
                startActivity(new Intent(this, CheckNumberActivity.class)));

        binding.cardStats.setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));

        binding.cardBlockedLog.setOnClickListener(v ->
                startActivity(new Intent(this, BlockedCallLogActivity.class)));

        binding.cardMyReports.setOnClickListener(v ->
                startActivity(new Intent(this, MyReportsActivity.class)));

        binding.btnViewBlocked.setOnClickListener(v ->
                startActivity(new Intent(this, BlockedListActivity.class)));

        binding.btnReportNumber.setOnClickListener(v ->
                startActivity(new Intent(this, ReportNumberActivity.class)));

        binding.btnEnableScreening.setOnClickListener(v -> requestCallScreeningRole());

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void updateScreeningStatus() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            binding.tvScreeningStatus.setText("Triagem ativa — chamadas spam serão bloqueadas");
            binding.ivShieldStatus.setImageResource(
                    com.blockendcall.android.R.drawable.ic_shield);
            binding.btnEnableScreening.setText("Configurar");
        } else {
            binding.tvScreeningStatus.setText("Não configurado — toque em Ativar");
            binding.ivShieldStatus.setImageResource(
                    com.blockendcall.android.R.drawable.ic_shield_off);
        }
    }

    private void requestCallScreeningRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);
        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            new AlertDialog.Builder(this)
                    .setTitle("Ativar Bloqueio de Chamadas")
                    .setMessage("Defina o BlockEndCall como app de triagem para bloquear spam automaticamente.")
                    .setPositiveButton("Ativar", (d, w) -> {
                        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                        roleRequestLauncher.launch(intent);
                    })
                    .setNegativeButton("Agora não", null)
                    .show();
        } else {
            Toast.makeText(this, "Triagem já está ativa", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("BlockEndCall")
                .setMessage("Versão 1.0.0\n\nBlockEndCall é uma plataforma comunitária de proteção contra spam. "
                        + "Cada reporte que você faz protege toda a comunidade de usuários."
                        + "\n\nDesenvolvido com Java + Spring Boot + Android.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
