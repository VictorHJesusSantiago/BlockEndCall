package com.blockendcall.android.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityCheckNumberBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.NumberCheckResult;
import com.blockendcall.android.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckNumberActivity extends AppCompatActivity {

    private ActivityCheckNumberBinding binding;
    private BlockedNumberApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        String prefill = getIntent().getStringExtra("prefill_number");
        if (prefill != null) {
            binding.etPhoneNumber.setText(prefill);
            checkNumber(prefill);
        }

        binding.btnCheck.setOnClickListener(v -> {
            String number = binding.etPhoneNumber.getText().toString().trim();
            if (number.isEmpty()) {
                binding.etPhoneNumber.setError("Digite um número");
                return;
            }
            checkNumber(number);
        });
    }

    private void checkNumber(String phoneNumber) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cardResult.setVisibility(View.GONE);
        binding.btnCheck.setEnabled(false);

        api.checkNumber(phoneNumber).enqueue(new Callback<ApiResponse<NumberCheckResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<NumberCheckResult>> call,
                                   Response<ApiResponse<NumberCheckResult>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCheck.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showResult(response.body().getData());
                } else {
                    Toast.makeText(CheckNumberActivity.this, "Erro ao consultar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<NumberCheckResult>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCheck.setEnabled(true);
                Toast.makeText(CheckNumberActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResult(NumberCheckResult result) {
        binding.cardResult.setVisibility(View.VISIBLE);

        if (!result.isBlocked()) {
            applyTheme("#2E7D32", "#E8F5E9", "✅", "Número Seguro",
                    "Nenhum reporte de spam encontrado para este número.");
            binding.tvScoreValue.setText("0");
            binding.tvReportCount.setText("0 reportes");
            binding.tvCategory.setText("—");
            binding.tvRiskLevel.setText("SEGURO");
            binding.tvRiskLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        } else {
            String color = getRiskColor(result.getRiskLevel());
            String bg = getRiskBg(result.getRiskLevel());
            applyTheme(color, bg, getRiskEmoji(result.getRiskLevel()),
                    getTitleFor(result),
                    result.getDescription() != null ? result.getDescription() : "Número reportado pela comunidade como spam.");

            binding.tvScoreValue.setText(String.valueOf(result.getSpamScore()));
            binding.tvReportCount.setText(result.getReportCount() + " reportes");
            binding.tvCategory.setText(getCategoryLabel(result.getCategory()));
            binding.tvRiskLevel.setText(result.getRiskLevel());
            binding.tvRiskLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
        }

        // Score arc color
        binding.progressScore.setProgress(result.isBlocked() ? result.getSpamScore() : 0);
        binding.progressScore.setProgressTintList(ColorStateList.valueOf(
                Color.parseColor(result.isBlocked() ? getRiskColor(result.getRiskLevel()) : "#2E7D32")));
    }

    private void applyTheme(String textColor, String bgColor, String emoji, String title, String description) {
        binding.cardResult.setCardBackgroundColor(Color.parseColor(bgColor));
        binding.tvResultEmoji.setText(emoji);
        binding.tvResultTitle.setText(title);
        binding.tvResultTitle.setTextColor(Color.parseColor(textColor));
        binding.tvResultDescription.setText(description);
    }

    private String getRiskColor(String risk) {
        switch (risk) {
            case "HIGH":   return "#C62828";
            case "MEDIUM": return "#F57F17";
            case "LOW":    return "#1565C0";
            default:       return "#2E7D32";
        }
    }

    private String getRiskBg(String risk) {
        switch (risk) {
            case "HIGH":   return "#FFEBEE";
            case "MEDIUM": return "#FFF8E1";
            case "LOW":    return "#E3F2FD";
            default:       return "#E8F5E9";
        }
    }

    private String getRiskEmoji(String risk) {
        switch (risk) {
            case "HIGH":   return "🚫";
            case "MEDIUM": return "⚠️";
            case "LOW":    return "❗";
            default:       return "✅";
        }
    }

    private String getTitleFor(NumberCheckResult result) {
        if (result.isConfirmed()) return "Spam Confirmado";
        switch (result.getRiskLevel()) {
            case "HIGH":   return "Alto Risco";
            case "MEDIUM": return "Risco Médio";
            default:       return "Possível Spam";
        }
    }

    private String getCategoryLabel(String category) {
        if (category == null) return "Desconhecido";
        switch (category) {
            case "TELEMARKETING":  return "📞 Telemarketing";
            case "SCAM":           return "💀 Golpe/Scam";
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
