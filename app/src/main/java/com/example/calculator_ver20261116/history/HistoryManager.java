package com.example.calculator_ver20261116.history;

import com.example.calculator_ver20261116.model.HistoryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryManager {

    private static HistoryManager instance;
    private final List<HistoryItem> historyList = new ArrayList<>();

    private HistoryManager() {}

    public static synchronized HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager();
        }
        return instance;
    }

    public synchronized void addRecord(String expression, String result) {
        if (expression != null && result != null) {
            historyList.add(0, new HistoryItem(expression, result));
        }
    }

    public synchronized List<HistoryItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(historyList));
    }

    public synchronized void clear() {
        historyList.clear();
    }

    public synchronized boolean isEmpty() {
        return historyList.isEmpty();
    }
}