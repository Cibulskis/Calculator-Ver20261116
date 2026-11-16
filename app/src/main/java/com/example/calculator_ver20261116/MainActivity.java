package com.example.calculator_ver20261116;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.calculator_ver20261116.engine.CalculatorEngine;
import com.example.calculator_ver20261116.history.HistoryBottomSheet;
import com.example.calculator_ver20261116.history.HistoryManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private TextView tvExpression;

    // Atminties mygtukai
    private Button btnMC;
    private Button btnMR;
    private Button btnMPlus;
    private Button btnMMinus;
    private Button btnMS;
    private Button btnMList;

    private CalculatorEngine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEngine();
        setupKeypadListeners();
        setupMemoryListeners();
        setupHeaderListeners();

        updateUi();
    }

    private void initViews() {
        tvDisplay = findViewById(R.id.tvDisplay);
        tvExpression = findViewById(R.id.tvExpression);

        btnMC = findViewById(R.id.btnMC);
        btnMR = findViewById(R.id.btnMR);
        btnMPlus = findViewById(R.id.btnMPlus);
        btnMMinus = findViewById(R.id.btnMMinus);
        btnMS = findViewById(R.id.btnMS);
        btnMList = findViewById(R.id.btnMList);
    }

    private void initEngine() {
        engine = new CalculatorEngine();

        // Rezultato fiksavimas į runtime istoriją
        engine.setHistoryCallback((expression, result) -> {
            HistoryManager.getInstance().addRecord(expression, result);
        });
    }

    private void setupKeypadListeners() {
        int[] digitIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : digitIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                char digit = btn.getText().charAt(0);
                engine.inputDigit(digit);
                updateUi();
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            engine.inputDecimal();
            updateUi();
        });

        findViewById(R.id.btnNegate).setOnClickListener(v -> {
            engine.inputNegate();
            updateUi();
        });

        findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            engine.inputBackspace();
            updateUi();
        });

        findViewById(R.id.btnCE).setOnClickListener(v -> {
            engine.clearEntry();
            updateUi();
        });

        findViewById(R.id.btnC).setOnClickListener(v -> {
            engine.resetAll();
            updateUi();
        });

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            engine.inputOperator(CalculatorEngine.Operator.ADD);
            updateUi();
        });

        findViewById(R.id.btnSubtract).setOnClickListener(v -> {
            engine.inputOperator(CalculatorEngine.Operator.SUBTRACT);
            updateUi();
        });

        findViewById(R.id.btnMultiply).setOnClickListener(v -> {
            engine.inputOperator(CalculatorEngine.Operator.MULTIPLY);
            updateUi();
        });

        findViewById(R.id.btnDivide).setOnClickListener(v -> {
            engine.inputOperator(CalculatorEngine.Operator.DIVIDE);
            updateUi();
        });

        findViewById(R.id.btnSquare).setOnClickListener(v -> {
            engine.inputSquare();
            updateUi();
        });

        findViewById(R.id.btnSquareRoot).setOnClickListener(v -> {
            engine.inputSquareRoot();
            updateUi();
        });

        findViewById(R.id.btnReciprocal).setOnClickListener(v -> {
            engine.inputReciprocal();
            updateUi();
        });

        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            engine.inputPercent();
            updateUi();
        });

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            engine.inputEquals();
            updateUi();
        });
    }

    private void setupMemoryListeners() {
        btnMS.setOnClickListener(v -> {
            engine.memoryStore();
            updateMemoryButtonsState();
            Toast.makeText(this, R.string.memory_stored_toast, Toast.LENGTH_SHORT).show();
        });

        btnMR.setOnClickListener(v -> {
            engine.memoryRecall();
            updateUi();
        });

        btnMC.setOnClickListener(v -> {
            engine.memoryClear();
            updateMemoryButtonsState();
            Toast.makeText(this, R.string.memory_cleared_toast, Toast.LENGTH_SHORT).show();
        });

        btnMPlus.setOnClickListener(v -> {
            engine.memoryAdd();
            updateMemoryButtonsState();
        });

        btnMMinus.setOnClickListener(v -> {
            engine.memorySubtract();
            updateMemoryButtonsState();
        });

        btnMList.setOnClickListener(v -> {
            if (engine.hasMemory()) {
                engine.memoryRecall();
                updateUi();
            } else {
                Toast.makeText(this, R.string.memory_empty_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHeaderListeners() {
        ImageButton btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> showHistoryBottomSheet());

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> Toast.makeText(this, R.string.header_standard, Toast.LENGTH_SHORT).show());

        ImageButton btnKeepOnTop = findViewById(R.id.btnKeepOnTop);
        btnKeepOnTop.setOnClickListener(v -> Toast.makeText(this, R.string.cd_keep_on_top, Toast.LENGTH_SHORT).show());
    }

    private void showHistoryBottomSheet() {
        HistoryBottomSheet sheet = HistoryBottomSheet.newInstance();
        sheet.setHistorySelectionListener(result -> {
            engine.loadResult(result);
            updateUi();
            Toast.makeText(this, R.string.history_item_selected_toast, Toast.LENGTH_SHORT).show();
        });
        sheet.show(getSupportFragmentManager(), "HistoryBottomSheet");
    }

    private void updateUi() {
        tvExpression.setText(engine.getExpressionText());

        if (engine.hasError()) {
            tvDisplay.setText(getLocalizedErrorMessage(engine.getErrorCode()));
        } else {
            tvDisplay.setText(engine.getDisplayText());
        }

        updateMemoryButtonsState();
    }

    private void updateMemoryButtonsState() {
        boolean hasMem = engine.hasMemory();
        btnMC.setEnabled(hasMem);
        btnMR.setEnabled(hasMem);
        btnMList.setEnabled(hasMem);

        int colorDisabled = ContextCompat.getColor(this, R.color.text_memory_disabled);
        int colorActive = ContextCompat.getColor(this, R.color.text_memory_active);

        btnMC.setTextColor(hasMem ? colorActive : colorDisabled);
        btnMR.setTextColor(hasMem ? colorActive : colorDisabled);
        btnMList.setTextColor(hasMem ? colorActive : colorDisabled);
    }

    private String getLocalizedErrorMessage(CalculatorEngine.ErrorCode code) {
        switch (code) {
            case DIVIDE_BY_ZERO:
                return getString(R.string.error_divide_by_zero);
            case INVALID_INPUT:
                return getString(R.string.error_invalid_input);
            case GENERAL_ERROR:
            default:
                return getString(R.string.error_general);
        }
    }
}