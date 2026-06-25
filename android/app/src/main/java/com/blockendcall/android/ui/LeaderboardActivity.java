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
import com.blockendcall.android.databinding.ActivityLeaderboardBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.LeaderboardEntry;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private ActivityLeaderboardBinding binding;
    private final List<LeaderboardEntry> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        loadLeaderboard();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadLeaderboard() {
        ApiClient.getInstance(this).getApi().getLeaderboard(50).enqueue(new Callback<ApiResponse<List<LeaderboardEntry>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LeaderboardEntry>>> call, Response<ApiResponse<List<LeaderboardEntry>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    if (response.body().getData() != null) items.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LeaderboardActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<LeaderboardEntry>>> call, Throwable t) {
                Toast.makeText(LeaderboardActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<LeaderboardEntry> data;
        Adapter(List<LeaderboardEntry> data) { this.data = data; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            LeaderboardEntry e = data.get(pos);
            String medal = e.getRank() == 1 ? "🥇" : e.getRank() == 2 ? "🥈" : e.getRank() == 3 ? "🥉" : String.valueOf(e.getRank());
            h.tvRank.setText(medal);
            h.tvName.setText(e.getName());
            h.tvReports.setText(e.getTotalReports() + " reportes");
            h.tvScore.setText(e.getReputationScore() + " pts");
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvReports, tvScore;
            VH(View v) {
                super(v);
                tvRank = v.findViewById(R.id.tv_rank);
                tvName = v.findViewById(R.id.tv_name);
                tvReports = v.findViewById(R.id.tv_reports);
                tvScore = v.findViewById(R.id.tv_score);
            }
        }
    }
}
