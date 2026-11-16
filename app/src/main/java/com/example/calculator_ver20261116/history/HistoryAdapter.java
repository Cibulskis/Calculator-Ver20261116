package com.example.calculator_ver20261116.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator_ver20261116.R;
import com.example.calculator_ver20261116.model.HistoryItem;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private final List<HistoryItem> items;
    private final OnItemClickListener clickListener;

    public HistoryAdapter(List<HistoryItem> items, OnItemClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.tvExpression.setText(item.getExpression());
        holder.tvResult.setText(item.getResult());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvExpression;
        final TextView tvResult;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvItemExpression);
            tvResult = itemView.findViewById(R.id.tvItemResult);
        }
    }
}