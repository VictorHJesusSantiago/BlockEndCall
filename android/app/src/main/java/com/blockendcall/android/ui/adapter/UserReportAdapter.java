package com.blockendcall.android.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.databinding.ItemUserReportBinding;
import com.blockendcall.android.model.UserReport;

import java.util.ArrayList;
import java.util.List;

public class UserReportAdapter extends RecyclerView.Adapter<UserReportAdapter.ViewHolder> {

    private final List<UserReport> items = new ArrayList<>();

    public void setItems(List<UserReport> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemUserReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserReportBinding binding;

        ViewHolder(ItemUserReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(UserReport report) {
            binding.tvPhoneNumber.setText(report.getPhoneNumber());
            binding.tvCategory.setText(getCategoryLabel(report.getCategory()));
            binding.tvCategoryEmoji.setText(getCategoryEmoji(report.getCategory()));
            binding.tvReportCount.setText(report.getTotalReports() + " reportes na comunidade");
            binding.tvDate.setText(formatDate(report.getReportedAt()));

            if (report.isConfirmed()) {
                binding.tvStatus.setText("✓ Confirmado");
                binding.tvStatus.setTextColor(Color.parseColor("#C62828"));
            } else {
                binding.tvStatus.setText("⏳ Em análise");
                binding.tvStatus.setTextColor(Color.parseColor("#F57F17"));
            }
        }

        private String getCategoryEmoji(String category) {
            if (category == null) return "❓";
            switch (category) {
                case "TELEMARKETING":  return "📞";
                case "SCAM":           return "💀";
                case "ROBOCALL":       return "🤖";
                case "DEBT_COLLECTOR": return "💳";
                case "PHISHING":       return "🎣";
                default:               return "❓";
            }
        }

        private String getCategoryLabel(String category) {
            if (category == null) return "Desconhecido";
            switch (category) {
                case "TELEMARKETING":  return "Telemarketing";
                case "SCAM":           return "Golpe/Scam";
                case "ROBOCALL":       return "Robocall";
                case "DEBT_COLLECTOR": return "Cobrança";
                case "PHISHING":       return "Phishing";
                default:               return "Desconhecido";
            }
        }

        private String formatDate(String isoDate) {
            if (isoDate == null) return "";
            return isoDate.length() >= 10 ? isoDate.substring(0, 10) : isoDate;
        }
    }
}
