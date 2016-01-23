package com.resinet;

import org.junit.Test;

import static org.junit.Assert.*;

public class Resinet3Test {

    @Test
    public void testTextIsNotProbability() throws Exception {
        assertFalse(Resinet3.textIsNotProbability("0"));
        assertFalse(Resinet3.textIsNotProbability("0.000"));
        assertFalse(Resinet3.textIsNotProbability("0.0"));
        assertFalse(Resinet3.textIsNotProbability("1"));
        assertFalse(Resinet3.textIsNotProbability("1.00000"));
        assertFalse(Resinet3.textIsNotProbability("0.0000001"));
        assertFalse(Resinet3.textIsNotProbability("0.932133"));
        assertFalse(Resinet3.textIsNotProbability("0.99999999"));

        assertTrue(Resinet3.textIsNotProbability("1.1"));
        assertTrue(Resinet3.textIsNotProbability("1.000001"));
        assertTrue(Resinet3.textIsNotProbability("-1"));
        assertTrue(Resinet3.textIsNotProbability("-0.000"));
        assertTrue(Resinet3.textIsNotProbability("-0"));
        assertTrue(Resinet3.textIsNotProbability(".000"));
        assertTrue(Resinet3.textIsNotProbability(".."));
        assertTrue(Resinet3.textIsNotProbability("10"));
        assertTrue(Resinet3.textIsNotProbability("2.0"));
        assertTrue(Resinet3.textIsNotProbability("2"));
    }
}