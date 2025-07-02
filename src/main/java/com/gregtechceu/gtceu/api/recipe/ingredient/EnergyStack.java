package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import net.minecraft.network.FriendlyByteBuf;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.With;
import org.jetbrains.annotations.Range;

import java.util.function.Function;
import java.util.function.Supplier;

@With
public record EnergyStack(@Range(from = 0, to = Long.MAX_VALUE) long voltage,
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
                    Supplier<String> error = () -> "primitive EnergyStacks must have 1A, got " + s.amperage();
                    return DataResult.error(error, s.voltage());
                }
            });

    public static final Codec<EnergyStack> CODEC = Codec.either(VOLTAGE_ONLY_CODEC, FULL_CODEC)
            .xmap(e -> e.map(Function.identity(), Function.identity()), stack -> {
                if (stack.amperage() == 1) return Either.left(stack);
                else return Either.right(stack);
            });
    // spotless:on

    public static final EnergyStack EMPTY = new EnergyStack(0, 1);
    public static final EnergyStack MAX = new EnergyStack(Long.MAX_VALUE, 1);

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
        return this == EMPTY || this.voltage <= 0;
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

    public static EnergyStack sum(EnergyStack a, EnergyStack b) {
        long totalEU = a.getTotalEU() + b.getTotalEU();
        long amperage = a.amperage() + b.amperage();
        return EnergyContainerList.calculateVoltageAmperage(totalEU, amperage);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarLong(this.voltage);
        buf.writeVarLong(this.amperage);
    }

    public static EnergyStack fromNetwork(FriendlyByteBuf buf) {
        return new EnergyStack(buf.readVarLong(), buf.readVarLong());
    }

    public record WithIO(EnergyStack stack, IO io) {

        public WithIO {
            Preconditions.checkArgument(io != IO.BOTH, "The I/O direction cannot be IO.BOTH!");
        }

        public boolean isEmpty() {
            return io == IO.NONE || stack.isEmpty();
        }

        public boolean isInput() {
            return io == IO.IN;
        }

        public boolean isOutput() {
            return io == IO.OUT;
        }

        public long voltage() {
            return stack.voltage();
        }

        public long amperage() {
            return stack.amperage();
        }

        public long getTotalEU() {
            return stack.getTotalEU();
        }
    }
}
