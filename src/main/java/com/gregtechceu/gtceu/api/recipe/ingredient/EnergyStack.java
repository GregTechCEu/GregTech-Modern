package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

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

    public EnergyStack add(long voltage, long amperage) {
        Preconditions.checkArgument(this.voltage + voltage >= 0, "Resulting voltage must be >= 0");
        Preconditions.checkArgument(this.amperage + amperage >= 1, "Resulting amperage must be >= 1");
        return new EnergyStack(this.voltage + voltage, this.amperage + amperage);
    }

    public EnergyStack addVoltage(long voltage) {
        Preconditions.checkArgument(this.voltage + voltage >= 0, "Resulting voltage must be >= 0");
        return withVoltage(this.voltage + voltage);
    }

    public EnergyStack multiplyVoltage(long multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Multiplier must be >= 0");
        return withVoltage(this.voltage * multiplier);
    }

    public EnergyStack multiplyVoltage(double multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Multiplier must be >= 0");
        return withVoltage((long) (this.voltage * multiplier));
    }

    public EnergyStack addAmperage(long amperage) {
        Preconditions.checkArgument(this.amperage + amperage >= 1, "Resulting amperage must be >= 1");
        return withAmperage(this.amperage + amperage);
    }

    public EnergyStack multiplyAmperage(long multiplier) {
        Preconditions.checkArgument(multiplier > 0, "Multiplier must be > 0");
        return withAmperage(this.amperage * multiplier);
    }

    public static EnergyStack sum(EnergyStack a, EnergyStack b) {
        return a.add(b.voltage, b.amperage);
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
