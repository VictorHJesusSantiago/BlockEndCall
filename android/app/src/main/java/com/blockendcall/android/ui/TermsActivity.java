package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.databinding.ActivityTermsBinding;
import com.blockendcall.android.model.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TermsActivity extends AppCompatActivity {

    private ActivityTermsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean requireAccept = getIntent().getBooleanExtra("REQUIRE_ACCEPT", false);
        if (requireAccept) {
            binding.btnAccept.setVisibility(View.VISIBLE);
            binding.btnAccept.setOnClickListener(v -> acceptTerms());
        }
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void acceptTerms() {
        ApiClient.getInstance(this).getApi().acceptTerms().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(TermsActivity.this, "Termos aceitos!", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                finish();
            }
        });
    }
}
