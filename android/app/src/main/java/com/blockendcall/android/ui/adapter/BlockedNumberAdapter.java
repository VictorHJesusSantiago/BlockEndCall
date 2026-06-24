package com.blockendcall.android.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.R;
import com.blockendcall.android.databinding.ItemBlockedNumberBinding;
import com.blockendcall.android.model.BlockedNumber;

import java.util.ArrayList;
import java.util.List;

public class BlockedNumberAdapter extends RecyclerView.Adapter<BlockedNumberAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(BlockedNumber number);
    }

    private final List<BlockedNumber> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public BlockedNumberAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addAll(List<BlockedNumber> newItems) {
        int start = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBlockedNumberBinding binding = ItemBlockedNumberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBlockedNumberBinding binding;

        ViewHolder(ItemBlockedNumberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BlockedNumber number) {
            binding.tvPhoneNumber.setText(number.getPhoneNumber());
            binding.tvCategory.setText(getCategoryLabel(number.getCategory()));
            binding.tvCategoryEmoji.setText(getCategoryEmoji(number.getCategory()));
            binding.tvReportCount.setText(number.getReportCount() + " reportes");

            binding.viewCategoryIcon.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor(getCategoryBgColor(number.getCategory()))));

            if (number.isConfirmed()) {
                binding.tvConfirmed.setVisibility(View.VISIBLE);
                binding.tvPending.setVisibility(View.GONE);
            } else {
                binding.tvConfirmed.setVisibility(View.GONE);
                binding.tvPending.setVisibility(View.VISIBLE);
            }

            binding.getRoot().setOnClickListener(v -> listener.onItemClick(number));
        }

        private String getCategoryEmoji(String category) {
            if (category == null) return "❓";
            switch (category) {
                case "TELEMARKETING": return "📞";
                case "SCAM":          return "💀";
                case "ROBOCALL":      return "🤖";
                case "DEBT_COLLECTOR":return "💳";
                case "PHISHING":      return "🎣";
                default:              return "❓";
            }
        }

        private String getCategoryLabel(String category) {
            if (category == null) return "Desconhecido";
            switch (category) {
                case "TELEMARKETING": return "Telemarketing";
                case "SCAM":          return "Golpe / Scam";
                case "ROBOCALL":      return "Robocall";
                case "DEBT_COLLECTOR":return "Cobrança";
                case "PHISHING":      return "Phishing";
                default:              return "Desconhecido";
            }
        }

        private String getCategoryBgColor(String category) {
            if (category == null) return "#ECEFF1";
            switch (category) {
                case "TELEMARKETING": return "#FFF3E0";
                case "SCAM":          return "#FFEBEE";
                case "ROBOCALL":      return "#F3E5F5";
                case "DEBT_COLLECTOR":return "#E0F2F1";
                case "PHISHING":      return "#FCE4EC";
                default:              return "#ECEFF1";
            }
        }
    }
}
