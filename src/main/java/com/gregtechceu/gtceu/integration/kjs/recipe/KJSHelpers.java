package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Wrapper;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KJSHelpers {

    // this regex should match any of these, with or without spaces and/or 'V' & 'A'
    // - <voltage>V @ <amperage>A
    // - <voltage> x <amperage>
    // - <voltage> * <amperage>
    // also allows specifying a negative voltage for input/output in GTRecipeSchema#EUt
    public static final Pattern VALUE_REGEX = Pattern.compile(
            "^(?<v>[+-]?\\d+)V?\\s*(?:@|x|\\*)?\\s*?(?<a>\\+?[1-9]\\d*)A?$",
            Pattern.CASE_INSENSITIVE);

    private static final Set<String> VOLTAGE_KEYS = Set.of("voltage", "v", "V", "eu", "EU", "");
    private static final Set<String> AMPERAGE_KEYS = Set.of("amperage", "a", "A");
    private static final long DEFAULT_VOLTAGE = 0;
    private static final long DEFAULT_AMPERAGE = 1;

    public static EnergyStack.WithIO parseIOEnergyStack(Object o) {
        if (o instanceof Wrapper w) {
            o = w.unwrap();
        }

        if (o instanceof EnergyStack.WithIO stack) {
            return stack;
        } else if (o instanceof EnergyStack stack) {
            return new EnergyStack.WithIO(stack, IO.IN);
        } else if (o instanceof Number n) {
            long value = n.longValue();
            return EnergyStack.WithIO.fromVoltage(value);
        } else {
            LongLongPair pair = parseEnergyStackValues(o);
            if (pair == null) {
                return EnergyStack.WithIO.EMPTY;
            }
            return EnergyStack.WithIO.fromVA(pair.firstLong(), pair.secondLong());
        }
    }

    public static EnergyStack parseEnergyStack(Object o) {
        if (o instanceof Wrapper w) {
            o = w.unwrap();
        }

        if (o instanceof EnergyStack stack) {
            return stack;
        } else if (o instanceof EnergyStack.WithIO stack) {
            return stack.stack();
        } else if (o instanceof Number n) {
            return new EnergyStack(n.longValue());
        } else {
            LongLongPair pair = parseEnergyStackValues(o);
            if (pair == null) {
                return EnergyStack.EMPTY;
            }
            return new EnergyStack(Math.abs(pair.firstLong()), Math.abs(pair.secondLong()));
        }
    }

    private static @Nullable LongLongPair parseEnergyStackValues(Object o) {
        long voltage = DEFAULT_VOLTAGE;
        long amperage = DEFAULT_AMPERAGE;

        if (o instanceof CharSequence) {
            String s = o.toString();

            Matcher match = VALUE_REGEX.matcher(s);
            if (!match.matches()) {
                return null;
            }
            voltage = Long.parseLong(match.group("v"));
            if (match.group("a") != null) {
                amperage = Long.parseLong(match.group("a"));
            }
        } else {
            Map<?, ?> map = MapJS.of(o);
            if (map == null) {
                return null;
            }
            for (String key : VOLTAGE_KEYS) {
                if (!map.containsKey(key)) continue;
                voltage = UtilsJS.parseLong(map.get(key), DEFAULT_VOLTAGE);
                if (voltage != DEFAULT_VOLTAGE) break;
            }
            for (String key : AMPERAGE_KEYS) {
                if (!map.containsKey(key)) continue;
                amperage = UtilsJS.parseLong(map.get(key), DEFAULT_AMPERAGE);
                if (amperage != DEFAULT_AMPERAGE) break;
            }
        }
        return LongLongPair.of(voltage, amperage);
    }
}
