package com.blockendcall.android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityNumberDetailBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.BlockedNumber;
import com.blockendcall.android.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NumberDetailActivity extends AppCompatActivity {

    private ActivityNumberDetailBinding binding;
    private BlockedNumberApi api;
    private BlockedNumber currentNumber;

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
                    currentNumber = response.body().getData();
                    bindData(currentNumber);
                } else {
                    Toast.makeText(NumberDetailActivity.this, "Número não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BlockedNumber>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(NumberDetailActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                finish();
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

        binding.btnCall.setOnClickListener(v -> confirmAndCall(number.getPhoneNumber(), number.isConfirmed()));
        binding.btnShare.setOnClickListener(v -> shareNumber(number));
        binding.btnFalsePositive.setOnClickListener(v -> confirmFalsePositive(number));
        binding.btnCopy.setOnClickListener(v -> copyNumber(number.getPhoneNumber()));
    }

    private void confirmAndCall(String phone, boolean isConfirmed) {
        if (isConfirmed) {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Número de Spam!")
                    .setMessage("Este número foi confirmado como spam. Deseja ligar mesmo assim?")
                    .setPositiveButton("Ligar", (d, w) -> dial(phone))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            dial(phone);
        }
    }

    private void dial(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void shareNumber(BlockedNumber number) {
        String text = "🚨 Cuidado com este número: " + number.getPhoneNumber()
                + "\nCategoria: " + getCategoryLabel(number.getCategory())
                + "\n" + number.getReportCount() + " reportes na comunidade BlockEndCall"
                + (number.isConfirmed() ? " — CONFIRMADO como spam." : " — Em análise.");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Compartilhar via"));
    }

    private void confirmFalsePositive(BlockedNumber number) {
        new AlertDialog.Builder(this)
                .setTitle("Reportar Falso Positivo")
                .setMessage("Você acredita que " + number.getPhoneNumber()
                        + " não é spam? Seu reporte ajuda a proteger números legítimos.")
                .setPositiveButton("Confirmar", (d, w) -> submitFalsePositive(number.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void submitFalsePositive(long numberId) {
        Map<String, String> body = new HashMap<>();
        body.put("reason", "Número legítimo reportado pelo usuário");

        api.reportFalsePositive(numberId, body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                   Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(NumberDetailActivity.this,
                            "Falso positivo registrado. Obrigado!", Toast.LENGTH_SHORT).show();
                    loadDetail(numberId);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Falha ao registrar";
                    Toast.makeText(NumberDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(NumberDetailActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyNumber(String phone) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("phone", phone);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Número copiado!", Toast.LENGTH_SHORT).show();
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
