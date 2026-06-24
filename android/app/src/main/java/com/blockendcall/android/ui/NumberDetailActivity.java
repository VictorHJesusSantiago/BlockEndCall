package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityNumberDetailBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NumberDetailActivity extends AppCompatActivity {

    private ActivityNumberDetailBinding binding;
    private BlockedNumberApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNumberDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long numberId = getIntent().getLongExtra("number_id", -1);
        String phone = getIntent().getStringExtra("number_phone");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(phone != null ? phone : "Detalhe");
        }

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        if (numberId != -1) loadDetail(numberId);
    }

    private void loadDetail(long id) {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getNumberById(id).enqueue(new Callback<ApiResponse<BlockedNumber>>() {
            @Override
            public void onResponse(Call<ApiResponse<BlockedNumber>> call,
                                   Response<ApiResponse<BlockedNumber>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bindData(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BlockedNumber>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void bindData(BlockedNumber number) {
        binding.tvPhoneNumber.setText(number.getPhoneNumber());
        binding.tvCategory.setText(getCategoryLabel(number.getCategory()));
        binding.tvReportCount.setText(String.valueOf(number.getReportCount()));

        if (number.isConfirmed()) {
            binding.tvStatusBadge.setText("✓ CONFIRMADO");
        } else {
            binding.tvStatusBadge.setText("⏳ EM ANÁLISE");
        }

        binding.tvDescription.setText(
                (number.getDescription() != null && !number.getDescription().isEmpty())
                        ? number.getDescription()
                        : "Sem descrição disponível.");
    }

    private String getCategoryLabel(String category) {
        if (category == null) return "Desconhecido";
        switch (category) {
            case "TELEMARKETING":  return "📞 Telemarketing";
            case "SCAM":           return "💀 Golpe / Scam";
            case "ROBOCALL":       return "🤖 Robocall";
            case "DEBT_COLLECTOR": return "💳 Cobrança";
            case "PHISHING":       return "🎣 Phishing";
            default:               return "❓ Desconhecido";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
