package com.gregtechceu.gtceu.utils.math;

public enum SIPrefix {

    Quetta('Q', 30),
    Ronna('R', 27),
    Yotta('Y', 24),
    Zetta('Z', 21),
    Exa('E', 18),
    Peta('P', 15),
    Tera('T', 12),
    Giga('G', 9),
    Mega('M', 6),
    Kilo('k', 3),
    One(Character.MIN_VALUE, 0),
    Milli('m', -3),
    Micro('µ', -6),
    Nano('n', -9),
    Pico('p', -12),
    Femto('f', -15),
    Atto('a', -18),
    Zepto('z', -21),
    Yocto('y', -24),
    Ronto('r', -27),
    Quecto('q', -30);

    public final char symbol;
    public final String stringSymbol;
    public final double factor;
    public final double oneOverFactor;

    SIPrefix(char symbol, int powerOfTen) {
        this.symbol = symbol;
        this.stringSymbol = symbol != Character.MIN_VALUE ? Character.toString(symbol) : "";
        this.factor = Math.pow(10, powerOfTen);
        this.oneOverFactor = 1 / this.factor;
    }

    public boolean isOne() {
        return this == One;
    }

    public static final SIPrefix[] HIGH = new SIPrefix[values().length / 2];
    public static final SIPrefix[] LOW = new SIPrefix[values().length / 2];

    static {
        SIPrefix[] values = values();
        for (int i = 0; i < HIGH.length; i++) {
            HIGH[i] = values[HIGH.length - 1 - i];
            LOW[i] = values[HIGH.length + 1 + i];
        }
    }
}
