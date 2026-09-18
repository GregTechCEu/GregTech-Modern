package brachy.modularui.utils;

import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of ItemStacks when
 * considering equality.
 */
public interface ItemStackHashStrategy extends Hash.Strategy<ItemStack> {

    /**
     * @return a builder object for producing a custom ItemStackHashStrategy.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static ItemStackHashStrategy comparingAll() {
        return Builder.ALL;
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks except the number
     * of items in the stack.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static ItemStackHashStrategy comparingAllButCount() {
        return Builder.ITEM_AND_TAG;
    }

    static ItemStackHashStrategy comparingItem() {
        return Builder.ITEM;
    }

    /**
     * Builder pattern class for generating customized ItemStackHashStrategy
     */
    public static class Builder {

        private static final ItemStackHashStrategy ALL = builder().compareItem(true)
                .compareCount(true)
                .compareTag(true)
                .build();
        private static final ItemStackHashStrategy ITEM_AND_TAG = builder().compareItem(true)
                .compareTag(true)
                .build();
        private static final ItemStackHashStrategy ITEM = builder().compareItem(true).build();

        private boolean item, count, components;

        /**
         * Defines whether the Item type should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareItem(boolean choice) {
            item = choice;
            return this;
        }

        /**
         * Defines whether stack size should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareCount(boolean choice) {
            count = choice;
            return this;
        }

        /**
         * Defines whether NBT Tags should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareTag(boolean choice) {
            components = choice;
            return this;
        }

        /**
         * @return the ItemStackHashStrategy as configured by "compare" methods.
         */
        public ItemStackHashStrategy build() {
            return new ItemStackHashStrategy() {

                @Override
                public int hashCode(@Nullable ItemStack o) {
                    return o == null || o.isEmpty() ? 0 : Objects.hash(
                            item ? o.getItem() : null,
                            count ? o.getCount() : null,
                            components ? o.getComponentsPatch() : null);
                }

                @Override
                public boolean equals(@Nullable ItemStack a, @Nullable ItemStack b) {
                    if (a == null || a.isEmpty()) return b == null || b.isEmpty();
                    if (b == null || b.isEmpty()) return false;

                    return (!item || a.getItem() == b.getItem()) &&
                            (!count || a.getCount() == b.getCount()) &&
                            (!components || a.getComponentsPatch().equals(b.getComponentsPatch()));
                }
            };
        }
    }
}
