package com.blockendcall.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blockendcall.android.databinding.ActivityBlockedCallLogBinding;
import com.blockendcall.android.ui.adapter.BlockedCallLogAdapter;
import com.blockendcall.android.util.BlockedCallLog;

import java.util.List;

public class BlockedCallLogActivity extends AppCompatActivity {

    private ActivityBlockedCallLogBinding binding;
    private BlockedCallLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedCallLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new BlockedCallLogAdapter(entry -> {
            Intent intent = new Intent(this, CheckNumberActivity.class);
            intent.putExtra("prefill_number", entry.phoneNumber);
            startActivity(intent);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnClearLog.setOnClickListener(v -> {
            BlockedCallLog.clear(this);
            loadLog();
        });

        loadLog();
    }

    private void loadLog() {
        List<BlockedCallLog.Entry> entries = BlockedCallLog.getAll(this);
        adapter.setItems(entries);
        binding.layoutEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        binding.btnClearLog.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
