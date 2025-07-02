package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.network.FriendlyByteBuf;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.With;
import org.jetbrains.annotations.Range;

import java.util.function.Function;
import java.util.function.Supplier;

// TODO rename to whatever that saner name was? I forgor
@With
public record EnergyStack(long voltage,
                          @Range(from = 1, to = Long.MAX_VALUE) long amperage) {

    // spotless:off
    public static final Codec<EnergyStack> FULL_CODEC = Codec.mapPair(
            Codec.LONG.fieldOf("voltage"),
            Codec.LONG.fieldOf("amperage")
    ).xmap(pair -> new EnergyStack(pair.getFirst(), pair.getSecond()),
            stack -> new Pair<>(stack.voltage(), stack.amperage())
    ).codec();
    public static final Codec<EnergyStack> VOLTAGE_ONLY_CODEC = Codec.LONG.flatComapMap(
            v -> new EnergyStack(v, 1), s -> {
                if (s.amperage() == 1) {
                    return DataResult.success(s.voltage());
                } else {
                    Supplier<String> error = () -> "primitive EnergyStacks must have 1A, is " + s.amperage();
                    if (s.amperage() > 1) {
                        return DataResult.error(error, s.voltage());
                    } else {
                        return DataResult.error(error);
                    }
                }
            });

    public static final Codec<EnergyStack> CODEC = Codec.either(VOLTAGE_ONLY_CODEC, FULL_CODEC)
            .xmap(e -> e.map(Function.identity(), Function.identity()), stack -> {
                if (stack.amperage() == 1) return Either.left(stack);
                else return Either.right(stack);
            });
    // spotless:on

    public static final EnergyStack EMPTY = new EnergyStack(0, 1);

    /**
     * Voltage-only constructor for 1A uses, e.g. most of them
     *
     * @param voltage The EU value
     */
    public EnergyStack(long voltage) {
        this(voltage, 1);
    }

    public long getTotalEU() {
        return voltage * amperage;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.voltage == 0;
    }

    public EnergyStack inverse() {
        return withVoltage(-voltage);
    }

    public EnergyStack absolute() {
        return withVoltage(Math.abs(voltage));
    }

    public EnergyStack addVoltage(long voltage) {
        return withVoltage(this.voltage + voltage);
    }

    public EnergyStack subsVoltage(long voltage) {
        return withVoltage(this.voltage - voltage);
    }

    public EnergyStack multiplyVoltage(long voltage) {
        return withVoltage(this.voltage * voltage);
    }

    public EnergyStack multiplyVoltage(double voltage) {
        return withVoltage((long) (this.voltage * voltage));
    }

    public EnergyStack addAmperage(long amperage) {
        return withAmperage(this.amperage + amperage);
    }

    public EnergyStack multiplyAmperage(long amperage) {
        return withAmperage(this.amperage * amperage);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarLong(this.voltage);
        buf.writeVarLong(this.amperage);
    }

    public static EnergyStack fromNetwork(FriendlyByteBuf buf) {
        return new EnergyStack(buf.readVarLong(), buf.readVarLong());
    }
}
