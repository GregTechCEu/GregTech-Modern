package com.gregtechceu.gtceu.utils.math;

import com.ezylang.evalex.Expression;

import java.math.BigDecimal;

public enum SIPrefix {

    Infinite('∞', Double.MAX_VALUE, true),
    Quetta('Q', 30),
    Ronna('R', 27),
    Yotta('Y', 24),
    Zetta('Z', 21),
    Exa('X', 18), // this should actually be E, but this clashes with euler's number e = 2.71...
    Peta('P', 15),
    Tera('T', 12),
    Giga('G', 9),
    Mega('M', 6),
    Kilo('k', 3),
    One(),
    Milli('m', -3),
    Micro('µ', -6),
    Nano('n', -9),
    Pico('p', -12),
    Femto('f', -15),
    Atto('a', -18),
    Zepto('z', -21),
    Yocto('y', -24),
    Ronto('r', -27),
    Quecto('q', -30),
    Infinitesimal('∞', Double.MIN_NORMAL, true);

    public final char symbol;
    public final String stringSymbol;
    public final double factor;
    public final double oneOverFactor;
    public final BigDecimal bigFactor;
    public final BigDecimal bigOneOverFactor;
    public final boolean infiniteLike;

    SIPrefix() {
        this.symbol = Character.MIN_VALUE;
        this.stringSymbol = "";
        this.factor = 1.0;
        this.oneOverFactor = 1.0;
        this.bigFactor = BigDecimal.ONE;
        this.bigOneOverFactor = BigDecimal.ONE;
        this.infiniteLike = false;
    }

    SIPrefix(char symbol, double f, boolean inf) {
        this.symbol = symbol;
        this.stringSymbol = Character.toString(symbol);
        this.factor = f;
        this.oneOverFactor = 1 / f;
        this.bigFactor = new BigDecimal(f);
        this.bigOneOverFactor = new BigDecimal(this.oneOverFactor);
        this.infiniteLike = inf;
    }

    SIPrefix(char symbol, int powerOfTen) {
        this(symbol, Math.pow(10, powerOfTen), false);
    }

    public boolean isOne() {
        return this == One;
    }

    public void addToExpression(Expression e) {
        e.with(String.valueOf(this.symbol), this.factor);
    }

    public static final SIPrefix[] VALUES = values();
    public static final SIPrefix[] HIGH = new SIPrefix[values().length / 2];
    public static final SIPrefix[] LOW = new SIPrefix[values().length / 2];

    static {
        SIPrefix[] values = values();
        for (int i = 0; i < HIGH.length; i++) {
            HIGH[i] = values[HIGH.length - 1 - i];
            LOW[i] = values[HIGH.length + 1 + i];
        }
    }

    public static void addAllToExpression(Expression e) {
        for (SIPrefix siPrefix : VALUES) {
            siPrefix.addToExpression(e);
        }
    }
}
