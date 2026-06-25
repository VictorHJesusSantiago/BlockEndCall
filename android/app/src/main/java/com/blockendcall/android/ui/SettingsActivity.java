package com.blockendcall.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.databinding.ActivitySettingsBinding;
import com.blockendcall.android.util.BlockedCallLog;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "blockendcall_settings";
    private static final String KEY_NOTIFY_BLOCKED = "notify_blocked";
    private static final String KEY_ONLY_CONFIRMED = "only_confirmed";
    private static final String KEY_SENSITIVITY = "sensitivity";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadSettings();

        binding.switchBlockedNotifications.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(KEY_NOTIFY_BLOCKED, checked).apply());

        binding.switchOnlyConfirmed.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(KEY_ONLY_CONFIRMED, checked).apply());

        binding.sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int v = (int) value;
                binding.tvSensitivityValue.setText(String.valueOf(v));
                prefs.edit().putInt(KEY_SENSITIVITY, v).apply();
            }
        });

        binding.itemClearLog.setOnClickListener(v -> confirmClearLog());
    }

    private void loadSettings() {
        binding.switchBlockedNotifications.setChecked(prefs.getBoolean(KEY_NOTIFY_BLOCKED, true));
        binding.switchOnlyConfirmed.setChecked(prefs.getBoolean(KEY_ONLY_CONFIRMED, true));

        int sensitivity = prefs.getInt(KEY_SENSITIVITY, 1);
        binding.sliderSensitivity.setValue(sensitivity);
        binding.tvSensitivityValue.setText(String.valueOf(sensitivity));

        int logCount = BlockedCallLog.getAll(this).size();
        binding.tvLogCount.setText(logCount + " registros");
    }

    private void confirmClearLog() {
        int count = BlockedCallLog.getAll(this).size();
        if (count == 0) {
            Toast.makeText(this, "Histórico já está vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Limpar Histórico")
                .setMessage("Apagar " + count + " registros de chamadas bloqueadas?")
                .setPositiveButton("Limpar", (d, w) -> {
                    BlockedCallLog.clear(this);
                    binding.tvLogCount.setText("0 registros");
                    Toast.makeText(this, "Histórico limpo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public static boolean shouldNotifyBlocked(android.content.Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_NOTIFY_BLOCKED, true);
    }

    public static boolean shouldBlockOnlyConfirmed(android.content.Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ONLY_CONFIRMED, true);
    }

    public static int getSensitivity(android.content.Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SENSITIVITY, 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
