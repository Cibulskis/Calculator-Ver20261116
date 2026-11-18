package com.example.calculator_ver20261116.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Autonominis skaičiavimo variklis su patikimu būsenų valdymu.
 */
public class CalculatorEngine {

    public enum Operator {
        ADD("+"),
        SUBTRACT("−"),
        MULTIPLY("×"),
        DIVIDE("÷");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public enum ErrorCode {
        NONE,
        DIVIDE_BY_ZERO,
        INVALID_INPUT,
        GENERAL_ERROR
    }

    public interface HistoryCallback {
        void onCalculationFinished(String expression, String result);
    }

    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final int MAX_INPUT_DIGITS = 16;

    private String currentInput = "0";
    private BigDecimal firstOperand = null;
    private Operator pendingOperator = null;
    private String expressionText = "";
    private boolean isEnteringNewNumber = true;
    private boolean isResultDisplayed = false;

    private ErrorCode errorCode = ErrorCode.NONE;

    private BigDecimal lastSecondOperand = null;
    private Operator lastOperator = null;

    private BigDecimal memory = BigDecimal.ZERO;
    private boolean hasMemory = false;

    private HistoryCallback historyCallback;

    public CalculatorEngine() {
        resetAll();
    }

    public void setHistoryCallback(HistoryCallback callback) {
        this.historyCallback = callback;
    }

    public void resetAll() {
        currentInput = "0";
        firstOperand = null;
        pendingOperator = null;
        expressionText = "";
        isEnteringNewNumber = true;
        isResultDisplayed = false;
        errorCode = ErrorCode.NONE;
        lastSecondOperand = null;
        lastOperator = null;
    }

    public void clearEntry() {
        if (errorCode != ErrorCode.NONE || isResultDisplayed) {
            resetAll();
            return;
        }
        currentInput = "0";
        isEnteringNewNumber = true;
    }

    public void inputDigit(char digit) {
        if (errorCode != ErrorCode.NONE) {
            resetAll();
        }

        if (isResultDisplayed) {
            resetAll();
        }

        if (isEnteringNewNumber) {
            currentInput = String.valueOf(digit);
            isEnteringNewNumber = false;
        } else {
            if (currentInput.equals("0")) {
                currentInput = String.valueOf(digit);
            } else {
                int digitCount = currentInput.replace(".", "").replace("-", "").length();
                if (digitCount < MAX_INPUT_DIGITS) {
                    currentInput += digit;
                }
            }
        }
    }

    public void inputDecimal() {
        if (errorCode != ErrorCode.NONE) {
            resetAll();
        }

        if (isResultDisplayed) {
            resetAll();
        }

        if (isEnteringNewNumber) {
            currentInput = "0.";
            isEnteringNewNumber = false;
        } else {
            if (!currentInput.contains(".")) {
                currentInput += ".";
            }
        }
    }

    public void inputBackspace() {
        if (errorCode != ErrorCode.NONE) {
            resetAll();
            return;
        }

        if (isResultDisplayed || isEnteringNewNumber) {
            return;
        }

        if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.equals("-") || currentInput.equals("-0") || currentInput.isEmpty()) {
                currentInput = "0";
                isEnteringNewNumber = true;
            }
        } else {
            currentInput = "0";
            isEnteringNewNumber = true;
        }
    }

    public void inputNegate() {
        if (errorCode != ErrorCode.NONE) return;

        if (currentInput.equals("0") || currentInput.equals("0.")) {
            return;
        }

        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }
    }

    public void inputOperator(Operator op) {
        if (errorCode != ErrorCode.NONE) return;

        try {
            BigDecimal currentVal = new BigDecimal(currentInput);

            if (isResultDisplayed) {
                firstOperand = currentVal;
                pendingOperator = op;
                expressionText = formatNumber(firstOperand) + " " + op.getSymbol();
                isEnteringNewNumber = true;
                isResultDisplayed = false;
                lastSecondOperand = null;
                lastOperator = null;
                return;
            }

            if (firstOperand != null && pendingOperator != null && !isEnteringNewNumber) {
                BigDecimal result = executeOperation(firstOperand, currentVal, pendingOperator);
                firstOperand = result;
                currentInput = formatNumber(result);
            } else if (firstOperand == null) {
                firstOperand = currentVal;
            }

            pendingOperator = op;
            expressionText = formatNumber(firstOperand) + " " + op.getSymbol();
            isEnteringNewNumber = true;
            lastSecondOperand = null;
            lastOperator = null;
        } catch (ArithmeticException e) {
            setError(ErrorCode.DIVIDE_BY_ZERO);
        } catch (Exception e) {
            setError(ErrorCode.GENERAL_ERROR);
        }
    }

    public void inputEquals() {
        if (errorCode != ErrorCode.NONE) return;

        try {
            if (firstOperand != null && pendingOperator != null) {
                BigDecimal secondOperand = new BigDecimal(currentInput);
                lastSecondOperand = secondOperand;
                lastOperator = pendingOperator;

                BigDecimal result = executeOperation(firstOperand, secondOperand, pendingOperator);
                String expr = formatNumber(firstOperand) + " " + pendingOperator.getSymbol() + " " + formatNumber(secondOperand) + " =";
                String resStr = formatNumber(result);

                expressionText = expr;
                currentInput = resStr;
                firstOperand = null;
                pendingOperator = null;
                isEnteringNewNumber = true;
                isResultDisplayed = true;

                if (historyCallback != null) {
                    historyCallback.onCalculationFinished(expr, resStr);
                }
            } else if (lastOperator != null && lastSecondOperand != null) {
                BigDecimal currentVal = new BigDecimal(currentInput);
                BigDecimal result = executeOperation(currentVal, lastSecondOperand, lastOperator);
                String expr = formatNumber(currentVal) + " " + lastOperator.getSymbol() + " " + formatNumber(lastSecondOperand) + " =";
                String resStr = formatNumber(result);

                expressionText = expr;
                currentInput = resStr;
                isEnteringNewNumber = true;
                isResultDisplayed = true;

                if (historyCallback != null) {
                    historyCallback.onCalculationFinished(expr, resStr);
                }
            }
        } catch (ArithmeticException e) {
            setError(ErrorCode.DIVIDE_BY_ZERO);
        } catch (Exception e) {
            setError(ErrorCode.GENERAL_ERROR);
        }
    }

    public void inputSquare() {
        if (errorCode != ErrorCode.NONE) return;

        try {
            BigDecimal val = new BigDecimal(currentInput);
            BigDecimal result = val.multiply(val, MC);
            String valStr = formatNumber(val);

            if (firstOperand != null && pendingOperator != null) {
                expressionText = formatNumber(firstOperand) + " " + pendingOperator.getSymbol() + " sqr(" + valStr + ")";
            } else {
                expressionText = "sqr(" + valStr + ")";
            }

            currentInput = formatNumber(result);
            isEnteringNewNumber = true;
            isResultDisplayed = false;
        } catch (Exception e) {
            setError(ErrorCode.GENERAL_ERROR);
        }
    }

    public void inputSquareRoot() {
        if (errorCode != ErrorCode.NONE) return;

        try {
            BigDecimal val = new BigDecimal(currentInput);
            if (val.compareTo(BigDecimal.ZERO) < 0) {
                setError(ErrorCode.INVALID_INPUT);
                return;
            }

            double dVal = val.doubleValue();
            double dSqrt = Math.sqrt(dVal);
            BigDecimal result = BigDecimal.valueOf(dSqrt).round(MC);
            String valStr = formatNumber(val);

            if (firstOperand != null && pendingOperator != null) {
                expressionText = formatNumber(firstOperand) + " " + pendingOperator.getSymbol() + " √(" + valStr + ")";
            } else {
                expressionText = "√(" + valStr + ")";
            }

            currentInput = formatNumber(result);
            isEnteringNewNumber = true;
            isResultDisplayed = false;
        } catch (Exception e) {
            setError(ErrorCode.INVALID_INPUT);
        }
    }

    public void inputReciprocal() {
        if (errorCode != ErrorCode.NONE) return;

        try {
            BigDecimal val = new BigDecimal(currentInput);
            if (val.compareTo(BigDecimal.ZERO) == 0) {
                setError(ErrorCode.DIVIDE_BY_ZERO);
                return;
            }

            BigDecimal result = BigDecimal.ONE.divide(val, MC);
            String valStr = formatNumber(val);

            if (firstOperand != null && pendingOperator != null) {
                expressionText = formatNumber(firstOperand) + " " + pendingOperator.getSymbol() + " 1/(" + valStr + ")";
            } else {
                expressionText = "1/(" + valStr + ")";
            }

            currentInput = formatNumber(result);
            isEnteringNewNumber = true;
            isResultDisplayed = false;
        } catch (ArithmeticException e) {
            setError(ErrorCode.DIVIDE_BY_ZERO);
        } catch (Exception e) {
            setError(ErrorCode.GENERAL_ERROR);
        }
    }

    public void inputPercent() {
        if (errorCode != ErrorCode.NONE) return;

        try {
            BigDecimal val = new BigDecimal(currentInput);
            BigDecimal result;

            if (firstOperand != null && pendingOperator != null) {
                if (pendingOperator == Operator.ADD || pendingOperator == Operator.SUBTRACT) {
                    result = firstOperand.multiply(val, MC).divide(BigDecimal.valueOf(100), MC);
                } else {
                    result = val.divide(BigDecimal.valueOf(100), MC);
                }
            } else {
                result = val.divide(BigDecimal.valueOf(100), MC);
            }

            currentInput = formatNumber(result);
            isEnteringNewNumber = true;
            isResultDisplayed = false;
        } catch (Exception e) {
            setError(ErrorCode.GENERAL_ERROR);
        }
    }

    public void memoryStore() {
        if (errorCode != ErrorCode.NONE) return;
        memory = new BigDecimal(currentInput);
        hasMemory = true;
        isEnteringNewNumber = true;
    }

    public void memoryRecall() {
        if (errorCode != ErrorCode.NONE || !hasMemory) return;
        currentInput = formatNumber(memory);
        isEnteringNewNumber = true;
        isResultDisplayed = false;
    }

    public void memoryAdd() {
        if (errorCode != ErrorCode.NONE) return;
        memory = memory.add(new BigDecimal(currentInput), MC);
        hasMemory = true;
        isEnteringNewNumber = true;
    }

    public void memorySubtract() {
        if (errorCode != ErrorCode.NONE) return;
        memory = memory.subtract(new BigDecimal(currentInput), MC);
        hasMemory = true;
        isEnteringNewNumber = true;
    }

    public void memoryClear() {
        memory = BigDecimal.ZERO;
        hasMemory = false;
    }

    public boolean hasMemory() {
        return hasMemory;
    }

    public void loadResult(String val) {
        resetAll();
        currentInput = val;
        isEnteringNewNumber = true;
        isResultDisplayed = true;
    }

    private BigDecimal executeOperation(BigDecimal a, BigDecimal b, Operator op) {
        switch (op) {
            case ADD:
                return a.add(b, MC);
            case SUBTRACT:
                return a.subtract(b, MC);
            case MULTIPLY:
                return a.multiply(b, MC);
            case DIVIDE:
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a.divide(b, MC);
            default:
                return b;
        }
    }

    private void setError(ErrorCode code) {
        this.errorCode = code;
        this.firstOperand = null;
        this.pendingOperator = null;
        this.isEnteringNewNumber = true;
        this.isResultDisplayed = false;
    }

    public boolean hasError() {
        return errorCode != ErrorCode.NONE;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDisplayText() {
        if (errorCode != ErrorCode.NONE) {
            return "";
        }
        return formatDisplayWithSeparators(currentInput);
    }

    public String getExpressionText() {
        return expressionText;
    }

    public static String formatNumber(BigDecimal bd) {
        if (bd == null) return "0";
        BigDecimal stripped = bd.stripTrailingZeros();
        if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return stripped.toPlainString();
    }

    private String formatDisplayWithSeparators(String input) {
        if (input == null || input.isEmpty() || input.equals("0")) {
            return input == null ? "0" : input;
        }

        try {
            boolean isNegative = input.startsWith("-");
            String raw = isNegative ? input.substring(1) : input;

            boolean hasTrailingDot = raw.endsWith(".");
            String[] parts = raw.split("\\.");
            BigDecimal wholePart = parts.length > 0 && !parts[0].isEmpty() ? new BigDecimal(parts[0]) : BigDecimal.ZERO;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(' ');
            DecimalFormat df = new DecimalFormat("#,##0", symbols);
            String formattedWhole = df.format(wholePart);

            StringBuilder sb = new StringBuilder();
            if (isNegative) {
                sb.append("-");
            }
            sb.append(formattedWhole);

            if (parts.length > 1) {
                sb.append(".").append(parts[1]);
            } else if (hasTrailingDot) {
                sb.append(".");
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
}