package com.blockendcall.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.databinding.ItemBlockedCallLogBinding;
import com.blockendcall.android.util.BlockedCallLog;

import java.util.ArrayList;
import java.util.List;

public class BlockedCallLogAdapter extends RecyclerView.Adapter<BlockedCallLogAdapter.ViewHolder> {

    public interface OnEntryClickListener {
        void onClick(BlockedCallLog.Entry entry);
    }

    private final List<BlockedCallLog.Entry> items = new ArrayList<>();
    private final OnEntryClickListener listener;

    public BlockedCallLogAdapter(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<BlockedCallLog.Entry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemBlockedCallLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBlockedCallLogBinding binding;

        ViewHolder(ItemBlockedCallLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BlockedCallLog.Entry entry) {
            binding.tvPhoneNumber.setText(entry.phoneNumber);
            binding.tvCategory.setText(getCategoryEmoji(entry.category) + " " + getCategoryLabel(entry.category));
            binding.tvTimestamp.setText(entry.timestamp);
            binding.getRoot().setOnClickListener(v -> listener.onClick(entry));
        }

        private String getCategoryEmoji(String cat) {
            if (cat == null) return "❓";
            switch (cat) {
                case "TELEMARKETING":  return "📞";
                case "SCAM":           return "💀";
                case "ROBOCALL":       return "🤖";
                case "DEBT_COLLECTOR": return "💳";
                case "PHISHING":       return "🎣";
                default:               return "❓";
            }
        }

        private String getCategoryLabel(String cat) {
            if (cat == null) return "Spam";
            switch (cat) {
                case "TELEMARKETING":  return "Telemarketing";
                case "SCAM":           return "Golpe/Scam";
                case "ROBOCALL":       return "Robocall";
                case "DEBT_COLLECTOR": return "Cobrança";
                case "PHISHING":       return "Phishing";
                default:               return "Spam";
            }
        }
    }
}
