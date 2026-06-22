package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityReportNumberBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportNumberActivity extends AppCompatActivity {

    private ActivityReportNumberBinding binding;
    private BlockedNumberApi api;

    private static final String[] CATEGORIES = {
            "TELEMARKETING", "SCAM", "ROBOCALL", "DEBT_COLLECTOR", "PHISHING", "UNKNOWN"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        binding.btnReport.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String phone = binding.etPhoneNumber.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String description = binding.etDescription.getText().toString().trim();

        if (phone.isEmpty()) {
            binding.etPhoneNumber.setError("Phone number is required");
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("phoneNumber", phone);
        body.put("category", category);
        if (!description.isEmpty()) body.put("description", description);

        api.reportNumber(body).enqueue(new Callback<ApiResponse<BlockedNumber>>() {
            @Override
            public void onResponse(Call<ApiResponse<BlockedNumber>> call,
                                   Response<ApiResponse<BlockedNumber>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BlockedNumber bn = response.body().getData();
                    String msg = bn.isConfirmed()
                            ? "Number confirmed as spam! It is now blocked for all users."
                            : "Report submitted! " + bn.getReportCount() + " reports so far.";
                    Toast.makeText(ReportNumberActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Report failed";
                    Toast.makeText(ReportNumberActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BlockedNumber>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ReportNumberActivity.this, "Connection error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnReport.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
