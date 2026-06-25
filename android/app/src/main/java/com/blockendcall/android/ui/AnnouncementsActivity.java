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
import com.blockendcall.android.databinding.ActivityAnnouncementsBinding;
import com.blockendcall.android.model.Announcement;
import com.blockendcall.android.model.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsActivity extends AppCompatActivity {

    private ActivityAnnouncementsBinding binding;
    private final List<Announcement> items = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new Adapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        loadAnnouncements();
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void loadAnnouncements() {
        ApiClient.getInstance(this).getApi().getAnnouncements().enqueue(new Callback<ApiResponse<List<Announcement>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Announcement>>> call, Response<ApiResponse<List<Announcement>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    items.clear();
                    if (response.body().getData() != null) items.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AnnouncementsActivity.this, "Erro ao carregar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Announcement>>> call, Throwable t) {
                Toast.makeText(AnnouncementsActivity.this, "Sem conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<Announcement> data;
        Adapter(List<Announcement> data) { this.data = data; }
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int pos) {
            Announcement a = data.get(pos);
            h.tvTitle.setText(a.getTitle() != null ? a.getTitle() : "");
            h.tvBody.setText(a.getBody() != null ? a.getBody() : "");
            String date = a.getCreatedAt();
            String dateStr = date != null && date.length() >= 10 ? date.substring(0, 10) : date != null ? date : "";
            h.tvMeta.setText("por " + (a.getAuthorName() != null ? a.getAuthorName() : "Admin") + " em " + dateStr);
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBody, tvMeta;
            VH(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvBody = v.findViewById(R.id.tv_body);
                tvMeta = v.findViewById(R.id.tv_meta);
            }
        }
    }
}
