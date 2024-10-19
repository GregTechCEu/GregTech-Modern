package com.gregtechceu.gtceu.utils;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of FluidStacks when
 * considering equality.
 */
public interface FluidStackHashStrategy extends Hash.Strategy<FluidStack> {

    /**
     * @return a builder object for producing a custom FluidStackHashStrategy.
     */
    static FluidStackHashStrategy.FluidStackHashStrategyBuilder builder() {
        return new FluidStackHashStrategy.FluidStackHashStrategyBuilder();
    }

    /**
     * Generates an FluidStackHash configured to compare every aspect of FluidStacks.
     *
     * @return the FluidStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAll() {
        return builder().compareFluid(true)
                .compareCount(true)
                .compareTag(true)
                .build();
    }

    /**
     * Generates an FluidStackHash configured to compare every aspect of FluidStacks except the number
     * of fluids in the stack.
     *
     * @return the FluidStackHashStrategy as described above.
     */
    static FluidStackHashStrategy comparingAllButAmount() {
        return builder().compareFluid(true)
                .compareTag(true)
                .build();
    }

    /**
     * Builder pattern class for generating customized FluidStackHashStrategy
     */
    class FluidStackHashStrategyBuilder {

        private boolean fluid, amount, damage, tag;

        /**
         * Defines whether the Fluid type should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategy.FluidStackHashStrategyBuilder compareFluid(boolean choice) {
            fluid = choice;
            return this;
        }

        /**
         * Defines whether stack size should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategy.FluidStackHashStrategyBuilder compareCount(boolean choice) {
            amount = choice;
            return this;
        }

        /**
         * Defines whether NBT Tags should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public FluidStackHashStrategy.FluidStackHashStrategyBuilder compareTag(boolean choice) {
            tag = choice;
            return this;
        }

        /**
         * @return the FluidStackHashStrategy as configured by "compare" methods.
         */
        public FluidStackHashStrategy build() {
            return new FluidStackHashStrategy() {

                @Override
                public int hashCode(@Nullable FluidStack o) {
                    return o == null || o.isEmpty() ? 0 : Objects.hash(
                            fluid ? o.getFluid() : null,
                            amount ? o.getAmount() : null,
                            tag ? o.getTag() : null);
                }

                @Override
                public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
                    if (a == null || a.isEmpty()) return b == null || b.isEmpty();
                    if (b == null || b.isEmpty()) return false;

                    return (!fluid || a.getFluid() == b.getFluid()) &&
                            (!amount || a.getAmount() == b.getAmount()) &&
                            (!tag || Objects.equals(a.getTag(), b.getTag()));
                }
            };
        }
    }
}
