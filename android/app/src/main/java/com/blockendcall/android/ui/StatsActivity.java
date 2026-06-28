package com.blockendcall.android.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityStatsBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.EnhancedStats;
import com.blockendcall.android.model.LeaderboardEntry;
import com.blockendcall.android.model.Stats;
import com.blockendcall.android.ui.adapter.BlockedNumberAdapter;
import com.blockendcall.android.util.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatsActivity extends AppCompatActivity {

    private ActivityStatsBinding binding;
    private BlockedNumberApi api;
    private BlockedNumberAdapter trendingAdapter;
    private LeaderboardAdapter leaderboardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        trendingAdapter = new BlockedNumberAdapter(number -> {});
        binding.rvTrending.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTrending.setAdapter(trendingAdapter);
        binding.rvTrending.setNestedScrollingEnabled(false);

        leaderboardAdapter = new LeaderboardAdapter();
        binding.rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLeaderboard.setAdapter(leaderboardAdapter);
        binding.rvLeaderboard.setNestedScrollingEnabled(false);

        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright, android.R.color.holo_green_light);
        binding.swipeRefresh.setOnRefreshListener(this::loadStats);

        loadStats();
    }

    private void loadStats() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.getStats().enqueue(new Callback<ApiResponse<Stats>>() {
            @Override
            public void onResponse(Call<ApiResponse<Stats>> call,
                                   Response<ApiResponse<Stats>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bindStats(response.body().getData());
                } else {
                    Toast.makeText(StatsActivity.this, "Falha ao carregar estatísticas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Stats>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(StatsActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });

        api.getEnhancedStats().enqueue(new Callback<ApiResponse<EnhancedStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<EnhancedStats>> call,
                                   Response<ApiResponse<EnhancedStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    setupLineChart(response.body().getData());
                    setupBarChart(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EnhancedStats>> call, Throwable t) {}
        });

        api.getLeaderboard(5).enqueue(new Callback<ApiResponse<List<LeaderboardEntry>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<LeaderboardEntry>>> call,
                                   Response<ApiResponse<List<LeaderboardEntry>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    leaderboardAdapter.setData(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<LeaderboardEntry>>> call, Throwable t) {}
        });
    }

    private void bindStats(Stats stats) {
        binding.tvTotalConfirmed.setText(String.valueOf(stats.getTotalConfirmed()));
        binding.tvTotalPending.setText(String.valueOf(stats.getTotalPending()));
        binding.tvTotalReports.setText(String.valueOf(stats.getTotalReports()));
        binding.tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));

        if (stats.getByCategory() != null) {
            binding.tvCountTelemarketing.setText(fmt(stats.getByCategory().get("TELEMARKETING")));
            binding.tvCountScam.setText(fmt(stats.getByCategory().get("SCAM")));
            binding.tvCountRobocall.setText(fmt(stats.getByCategory().get("ROBOCALL")));
            binding.tvCountDebt.setText(fmt(stats.getByCategory().get("DEBT_COLLECTOR")));
            binding.tvCountPhishing.setText(fmt(stats.getByCategory().get("PHISHING")));
            binding.tvCountUnknown.setText(fmt(stats.getByCategory().get("UNKNOWN")));
        }

        trendingAdapter.clear();
        if (stats.getTrending() != null && !stats.getTrending().isEmpty()) {
            trendingAdapter.addAll(stats.getTrending());
            binding.layoutTrending.setVisibility(View.VISIBLE);
        } else {
            binding.layoutTrending.setVisibility(View.GONE);
        }
    }

    private void setupLineChart(EnhancedStats stats) {
        if (stats.getDailyCounts() == null || stats.getDailyCounts().isEmpty()) return;
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<EnhancedStats.DailyCount> counts = stats.getDailyCounts();
        for (int i = 0; i < counts.size(); i++) {
            entries.add(new Entry(i, counts.get(i).getCount()));
            String date = counts.get(i).getDate();
            labels.add(date != null && date.length() >= 10 ? date.substring(5) : String.valueOf(i));
        }
        LineDataSet dataset = new LineDataSet(entries, "Reportes");
        dataset.setColor(0xFFE53935);
        dataset.setCircleColor(0xFFE53935);
        dataset.setLineWidth(2f);
        dataset.setDrawValues(false);
        dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        binding.lineChartTemporal.setData(new LineData(dataset));
        binding.lineChartTemporal.getDescription().setEnabled(false);
        binding.lineChartTemporal.getLegend().setEnabled(false);
        binding.lineChartTemporal.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.lineChartTemporal.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.lineChartTemporal.getXAxis().setGranularity(1f);
        binding.lineChartTemporal.getXAxis().setLabelRotationAngle(-45f);
        binding.lineChartTemporal.getAxisRight().setEnabled(false);
        binding.lineChartTemporal.animateY(800);
        binding.lineChartTemporal.invalidate();
    }

    private void setupBarChart(EnhancedStats stats) {
        if (stats.getByCategory() == null || stats.getByCategory().isEmpty()) return;
        String[] catNames = {"TELEMARKETING", "SCAM", "ROBOCALL", "DEBT_COLLECTOR", "PHISHING", "UNKNOWN"};
        String[] catLabels = {"Tele", "Scam", "Robo", "Cobr", "Phish", "Outro"};
        int[] colors = {0xFFFF8F00, 0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFF8E24AA, 0xFF757575};

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> barColors = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Map<String, Long> byCategory = stats.getByCategory();
        int i = 0;
        for (int j = 0; j < catNames.length; j++) {
            Long val = byCategory.get(catNames[j]);
            if (val != null && val > 0) {
                entries.add(new BarEntry(i, val));
                barColors.add(colors[j]);
                labels.add(catLabels[j]);
                i++;
            }
        }
        if (entries.isEmpty()) return;

        BarDataSet dataset = new BarDataSet(entries, "Categorias");
        dataset.setColors(barColors);
        dataset.setDrawValues(false);

        binding.barChartCategory.setData(new BarData(dataset));
        binding.barChartCategory.getDescription().setEnabled(false);
        binding.barChartCategory.getLegend().setEnabled(false);
        binding.barChartCategory.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartCategory.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChartCategory.getXAxis().setGranularity(1f);
        binding.barChartCategory.getAxisRight().setEnabled(false);
        binding.barChartCategory.setFitBars(true);
        binding.barChartCategory.animateY(800);
        binding.barChartCategory.invalidate();
    }

    private String fmt(Long v) { return v != null ? String.valueOf(v) : "0"; }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {
        private List<LeaderboardEntry> data = new ArrayList<>();

        void setData(List<LeaderboardEntry> d) {
            data = d != null ? d : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(16, 12, 16, 12);
            tv.setTextSize(14);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            LeaderboardEntry e = data.get(pos);
            ((TextView) h.itemView).setText(e.getRank() + ". " + e.getName()
                    + "  —  " + e.getTotalReports() + " reportes  ⭐ " + e.getReputationScore());
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            VH(android.view.View v) { super(v); }
        }
    }
}
