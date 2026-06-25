package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.databinding.ActivityBadgesBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.Badge;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BadgesActivity extends AppCompatActivity {

    private ActivityBadgesBinding binding;
    private final List<Badge> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBadgesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        loadBadges();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadBadges() {
        ApiClient.getInstance(this).getApi().getBadges().enqueue(new Callback<ApiResponse<List<Badge>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Badge>>> call, Response<ApiResponse<List<Badge>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    if (response.body().getData() != null) items.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(BadgesActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Badge>>> call, Throwable t) {
                Toast.makeText(BadgesActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String badgeEmoji(String type) {
        if (type == null) return "🏅";
        switch (type) {
            case "FIRST_REPORT": return "🏅";
            case "REPORTER_10": return "⭐";
            case "REPORTER_50": return "🌟";
            case "REPORTER_100": return "🏆";
            case "REPORTER_500": return "👑";
            case "FIRST_CONFIRMED": return "✅";
            case "STREAK_7": return "🔥";
            case "EARLY_ADOPTER": return "🚀";
            default: return "🎖️";
        }
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<Badge> data;
        Adapter(List<Badge> data) { this.data = data; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            Badge b = data.get(pos);
            h.tvEmoji.setText(badgeEmoji(b.getBadgeType()));
            h.tvName.setText(b.getDisplayName() != null ? b.getDisplayName() : b.getBadgeType());
            String date = b.getAwardedAt();
            h.tvDate.setText(date != null && date.length() >= 10 ? date.substring(0, 10) : date != null ? date : "");
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvDate;
            VH(View v) {
                super(v);
                tvEmoji = v.findViewById(R.id.tv_emoji);
                tvName = v.findViewById(R.id.tv_name);
                tvDate = v.findViewById(R.id.tv_date);
            }
        }
    }
}
