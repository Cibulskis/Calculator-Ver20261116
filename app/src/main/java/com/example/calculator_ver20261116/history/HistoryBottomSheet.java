package com.example.calculator_ver20261116.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator_ver20261116.R;
import com.example.calculator_ver20261116.model.HistoryItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class HistoryBottomSheet extends BottomSheetDialogFragment {

    public interface HistorySelectionListener {
        void onHistoryItemSelected(String result);
    }

    private HistorySelectionListener listener;
    private final List<HistoryItem> items = new ArrayList<>();
    private HistoryAdapter adapter;
    private TextView tvEmpty;
    private ImageButton btnClear;

    public static HistoryBottomSheet newInstance() {
        return new HistoryBottomSheet();
    }

    public void setHistorySelectionListener(HistorySelectionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_history_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);
        btnClear = view.findViewById(R.id.btnClearHistory);

        items.clear();
        items.addAll(HistoryManager.getInstance().getItems());

        adapter = new HistoryAdapter(items, item -> {
            if (listener != null) {
                listener.onHistoryItemSelected(item.getResult());
            }
            dismiss();
        });

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        updateEmptyState();

        btnClear.setOnClickListener(v -> {
            HistoryManager.getInstance().clear();
            items.clear();
            adapter.notifyDataSetChanged();
            updateEmptyState();
            Toast.makeText(requireContext(), R.string.history_cleared_toast, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEmptyState() {
        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            btnClear.setEnabled(false);
            btnClear.setAlpha(0.3f);
        } else {
            tvEmpty.setVisibility(View.GONE);
            btnClear.setEnabled(true);
            btnClear.setAlpha(1.0f);
        }
    }
}