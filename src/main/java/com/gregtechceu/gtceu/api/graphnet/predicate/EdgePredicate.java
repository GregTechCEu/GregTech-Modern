package com.gregtechceu.gtceu.api.graphnet.predicate;

import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Note - all extenders of this class are suggested to be final, in order to avoid unexpected
 * {@link #union(EdgePredicate)} behavior.
 */
public abstract class EdgePredicate<T extends EdgePredicate<T, N>, N extends Tag>
                                   implements INBTSerializable<N> {

    public abstract @NotNull NetPredicateType<T> getType();

    public void deserializeNBTNaive(Tag nbt) {
        deserializeNBT((N) nbt);
    }

    /**
     * Whether this predicate should behave in "and" fashion with other predicates. <br>
     * <br>
     * For example, if a predicate handler has 2 and-y predicates and 3 or-y predicates,
     * the effective result of evaluation will be: <br>
     * (andy1) && (andy2) && (ory1 || ory2 || ory3)
     */
    public abstract boolean andy();

    public abstract boolean test(IPredicateTestObject object);

    /**
     * Returns null if the operation is not supported.
     */
    @Nullable
    public T union(EdgePredicate<?, ?> other) {
        return null;
    }

    /**
     * {@link Object#equals(Object)} should always have a good implementation for the sake of sameness checks.
     */
    @Override
    public abstract boolean equals(Object obj);
}
