package com.example.calculator_ver20261116.model;

import java.io.Serializable;

public class HistoryItem implements Serializable {
    private final String expression;
    private final String result;
    private final long timestamp;

    public HistoryItem(String expression, String result) {
        this.expression = expression;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
    }

    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }
}