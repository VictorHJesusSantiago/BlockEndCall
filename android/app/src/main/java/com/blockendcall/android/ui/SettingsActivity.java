package com.blockendcall.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivitySettingsBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.UserPreference;
import com.blockendcall.android.receiver.ScheduledBlockingReceiver;
import com.blockendcall.android.util.BlockedCallLog;
import com.blockendcall.android.util.SessionManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "blockendcall_settings";
    private static final String KEY_NOTIFY_BLOCKED = "notify_blocked";
    private static final String KEY_ONLY_CONFIRMED = "only_confirmed";
    private static final String KEY_SENSITIVITY = "sensitivity";
    private static final String KEY_SCHEDULED = "scheduled_blocking";
    private static final String KEY_START_HOUR = "schedule_start_hour";
    private static final String KEY_END_HOUR = "schedule_end_hour";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;
    private BlockedNumberApi api;

    private int startHour = 22;
    private int endHour = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        loadSettings();
        attachSwitchListeners();
        loadPreferencesFromApi();

        binding.sliderSensitivity.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int v = (int) value;
                binding.tvSensitivityValue.setText(String.valueOf(v));
                prefs.edit().putInt(KEY_SENSITIVITY, v).apply();
                pushPreferencesToApi();
            }
        });

        binding.itemClearLog.setOnClickListener(v -> confirmClearLog());

        binding.switchScheduledBlocking.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_SCHEDULED, checked).apply();
            binding.layoutScheduleTimes.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (checked) ScheduledBlockingReceiver.schedule(this, startHour, endHour);
            else ScheduledBlockingReceiver.cancel(this);
        });

        binding.btnStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.btnEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void loadSettings() {
        binding.switchBlockedNotifications.setChecked(prefs.getBoolean(KEY_NOTIFY_BLOCKED, true));
        binding.switchOnlyConfirmed.setChecked(prefs.getBoolean(KEY_ONLY_CONFIRMED, true));

        int sensitivity = prefs.getInt(KEY_SENSITIVITY, 1);
        binding.sliderSensitivity.setValue(sensitivity);
        binding.tvSensitivityValue.setText(String.valueOf(sensitivity));

        int logCount = BlockedCallLog.getAll(this).size();
        binding.tvLogCount.setText(logCount + " registros");

        startHour = prefs.getInt(KEY_START_HOUR, 22);
        endHour = prefs.getInt(KEY_END_HOUR, 8);
        boolean scheduled = prefs.getBoolean(KEY_SCHEDULED, false);
        binding.switchScheduledBlocking.setChecked(scheduled);
        binding.layoutScheduleTimes.setVisibility(scheduled ? View.VISIBLE : View.GONE);
        updateTimeButtonLabels();
    }

    private void attachSwitchListeners() {
        binding.switchBlockedNotifications.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_NOTIFY_BLOCKED, checked).apply();
            pushPreferencesToApi();
        });
        binding.switchOnlyConfirmed.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_ONLY_CONFIRMED, checked).apply();
            pushPreferencesToApi();
        });
    }

    private void loadPreferencesFromApi() {
        api.getPreferences().enqueue(new Callback<ApiResponse<UserPreference>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserPreference>> call,
                                   Response<ApiResponse<UserPreference>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserPreference serverPrefs = response.body().getData();
                    // Temporarily remove listeners to avoid triggering pushPreferencesToApi
                    binding.switchBlockedNotifications.setOnCheckedChangeListener(null);
                    binding.switchOnlyConfirmed.setOnCheckedChangeListener(null);
                    binding.switchBlockedNotifications.setChecked(serverPrefs.isNotifyOnConfirm());
                    binding.switchOnlyConfirmed.setChecked(serverPrefs.isBlockOnlyConfirmed());
                    int sens = serverPrefs.getSensitivity();
                    if (sens >= 1 && sens <= 10) {
                        binding.sliderSensitivity.setValue(sens);
                        binding.tvSensitivityValue.setText(String.valueOf(sens));
                    }
                    // Re-attach listeners
                    attachSwitchListeners();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserPreference>> call, Throwable t) {}
        });
    }

    private void pushPreferencesToApi() {
        Map<String, Object> body = new HashMap<>();
        body.put("notifyOnConfirm", prefs.getBoolean(KEY_NOTIFY_BLOCKED, true));
        body.put("blockOnlyConfirmed", prefs.getBoolean(KEY_ONLY_CONFIRMED, true));
        body.put("sensitivity", prefs.getInt(KEY_SENSITIVITY, 1));
        api.updatePreferences(body).enqueue(new Callback<ApiResponse<UserPreference>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserPreference>> call,
                                   Response<ApiResponse<UserPreference>> response) {}

            @Override
            public void onFailure(Call<ApiResponse<UserPreference>> call, Throwable t) {}
        });
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

    private void showTimePicker(boolean isStart) {
        int hour = isStart ? startHour : endHour;
        new android.app.TimePickerDialog(this, (view, h, m) -> {
            if (isStart) {
                startHour = h;
                prefs.edit().putInt(KEY_START_HOUR, h).apply();
            } else {
                endHour = h;
                prefs.edit().putInt(KEY_END_HOUR, h).apply();
            }
            updateTimeButtonLabels();
            if (prefs.getBoolean(KEY_SCHEDULED, false))
                ScheduledBlockingReceiver.schedule(this, startHour, endHour);
        }, hour, 0, true).show();
    }

    private void updateTimeButtonLabels() {
        binding.btnStartTime.setText(String.format(Locale.getDefault(), "%02d:00", startHour));
        binding.btnEndTime.setText(String.format(Locale.getDefault(), "%02d:00", endHour));
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
