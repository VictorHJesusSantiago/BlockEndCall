package com.blockendcall.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            binding.tvCategory.setText(number.getCategory());
            binding.tvReportCount.setText(number.getReportCount() + " reports");
            binding.tvConfirmed.setVisibility(number.isConfirmed()
                    ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(number));
        }
    }
}
