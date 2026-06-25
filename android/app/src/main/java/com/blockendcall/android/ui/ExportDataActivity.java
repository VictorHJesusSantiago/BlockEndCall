package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.PagedResponse;
import com.blockendcall.android.databinding.ActivityExportDataBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedCallLogEntry;
import com.blockendcall.android.model.UserReport;
import com.google.gson.Gson;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExportDataActivity extends AppCompatActivity {

    private ActivityExportDataBinding binding;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExportDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnExportReports.setOnClickListener(v -> exportReports());
        binding.btnExportCallLog.setOnClickListener(v -> exportCallLog());
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void exportReports() {
        ApiClient.getInstance(this).getApi().getMyReports().enqueue(new Callback<ApiResponse<List<UserReport>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserReport>>> call, Response<ApiResponse<List<UserReport>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String json = gson.toJson(response.body().getData());
                    shareText(json, "meus_reportes.json");
                } else {
                    Toast.makeText(ExportDataActivity.this, "Erro ao exportar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<UserReport>>> call, Throwable t) {
                Toast.makeText(ExportDataActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportCallLog() {
        ApiClient.getInstance(this).getApi().getCallLog(0, 1000).enqueue(new Callback<ApiResponse<PagedResponse<BlockedCallLogEntry>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<BlockedCallLogEntry>>> call, Response<ApiResponse<PagedResponse<BlockedCallLogEntry>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String json = gson.toJson(response.body().getData());
                    shareText(json, "call_log.json");
                } else {
                    Toast.makeText(ExportDataActivity.this, "Erro ao exportar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<BlockedCallLogEntry>>> call, Throwable t) {
                Toast.makeText(ExportDataActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareText(String content, String filename) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_SUBJECT, filename);
        startActivity(Intent.createChooser(intent, "Exportar " + filename));
    }
}
