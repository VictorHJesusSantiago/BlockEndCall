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
import com.blockendcall.android.databinding.ActivityNumberTimelineBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.NumberTimeline;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NumberTimelineActivity extends AppCompatActivity {

    private ActivityNumberTimelineBinding binding;
    private final List<NumberTimeline> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNumberTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long numberId = getIntent().getLongExtra("numberId", -1);
        adapter = new Adapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        if (numberId >= 0) loadTimeline(numberId);
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadTimeline(long numberId) {
        ApiClient.getInstance(this).getApi().getTimeline(numberId).enqueue(new Callback<ApiResponse<List<NumberTimeline>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NumberTimeline>>> call, Response<ApiResponse<List<NumberTimeline>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    if (response.body().getData() != null) items.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(NumberTimelineActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<NumberTimeline>>> call, Throwable t) {
                Toast.makeText(NumberTimelineActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String eventDisplayName(String type) {
        if (type == null) return "";
        switch (type) {
            case "NUMBER_REPORTED": return "Reportado";
            case "NUMBER_CONFIRMED": return "Confirmado como Spam";
            case "ME_TOO_CONFIRMATION": return "Confirmação Comunitária";
            case "REPORTED_NAME": return "Nome Relatado";
            case "FALSE_POSITIVE": return "Falso Positivo Relatado";
            case "WHITELISTED": return "Adicionado à Whitelist";
            default: return type;
        }
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<NumberTimeline> data;
        Adapter(List<NumberTimeline> data) { this.data = data; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            NumberTimeline e = data.get(pos);
            h.tvType.setText(eventDisplayName(e.getEventType()));
            h.tvDetails.setText(e.getDetails() != null ? e.getDetails() : "");
            String date = e.getCreatedAt();
            h.tvDate.setText(date != null && date.length() >= 10 ? date.substring(0, 10) : date != null ? date : "");
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvType, tvDetails, tvDate;
            VH(View v) {
                super(v);
                tvType = v.findViewById(R.id.tv_event_type);
                tvDetails = v.findViewById(R.id.tv_details);
                tvDate = v.findViewById(R.id.tv_date);
            }
        }
    }
}
