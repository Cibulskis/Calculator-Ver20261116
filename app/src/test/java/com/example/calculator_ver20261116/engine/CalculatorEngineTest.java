package com.example.calculator_ver20261116;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.calculator_ver20261116.engine.CalculatorEngine;

import org.junit.Before;
import org.junit.Test;

public class CalculatorEngineTest {

    private CalculatorEngine engine;

    @Before
    public void setUp() {
        engine = new CalculatorEngine();
    }

    @Test
    public void testNewCalculationAfterEquals() {
        // 5 + 3 = 8
        engine.inputDigit('5');
        engine.inputOperator(CalculatorEngine.Operator.ADD);
        engine.inputDigit('3');
        engine.inputEquals();
        assertEquals("8", engine.getDisplayText());

        // Įvedame NAUJĄ skaičių: 4 * 2 = 8 (neturi būti 24!)
        engine.inputDigit('4');
        engine.inputOperator(CalculatorEngine.Operator.MULTIPLY);
        engine.inputDigit('2');
        engine.inputEquals();
        assertEquals("8", engine.getDisplayText());
    }

    @Test
    public void testRepeatEquals() {
        // 5 + 3 = 8
        engine.inputDigit('5');
        engine.inputOperator(CalculatorEngine.Operator.ADD);
        engine.inputDigit('3');
        engine.inputEquals();
        assertEquals("8", engine.getDisplayText());

        // Pakartotinis '=' turi pridėti +3
        engine.inputEquals();
        assertEquals("11", engine.getDisplayText());

        engine.inputEquals();
        assertEquals("14", engine.getDisplayText());
    }

    @Test
    public void testNegativeDecimalFormatting() {
        // Įvedame -0.5
        engine.inputDigit('0');
        engine.inputDecimal();
        engine.inputDigit('5');
        engine.inputNegate();
        assertEquals("-0.5", engine.getDisplayText());
    }

    @Test
    public void testDivisionByZero() {
        engine.inputDigit('9');
        engine.inputOperator(CalculatorEngine.Operator.DIVIDE);
        engine.inputDigit('0');
        engine.inputEquals();
        assertTrue(engine.hasError());
        assertEquals(CalculatorEngine.ErrorCode.DIVIDE_BY_ZERO, engine.getErrorCode());
    }
}