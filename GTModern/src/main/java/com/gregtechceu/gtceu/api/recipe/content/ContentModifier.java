package com.gregtechceu.gtceu.api.recipe.content;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ContentModifier {

    @Getter
    private final double multiplier;
    @Getter
    private final double addition;

    public static ContentModifier of(double multiplier, double addition) {
        return new ContentModifier(multiplier, addition);
    }

    public static ContentModifier multiplier(double multiplier) {
        return new ContentModifier(multiplier, 0);
    }

    public static ContentModifier addition(double addition) {
        return new ContentModifier(1, addition);
    }

    public ContentModifier(double multiplier, double addition) {
        this.multiplier = multiplier;
        this.addition = addition;
    }

    public Number apply(Number number) {
        if (number instanceof BigDecimal decimal) {
            return decimal.multiply(BigDecimal.valueOf(multiplier)).add(BigDecimal.valueOf(addition));
        }
        if (number instanceof BigInteger bigInteger) {
            return bigInteger.multiply(BigInteger.valueOf((long) multiplier)).add(BigInteger.valueOf((long) addition));
        }
        return number.doubleValue() * multiplier + addition;
    }
}
