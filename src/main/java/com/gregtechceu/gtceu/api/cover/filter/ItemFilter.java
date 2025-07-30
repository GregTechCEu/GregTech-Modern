package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ItemFilter extends Filter<ItemStack, ItemFilter> {

    Map<ItemLike, Function<ItemStack, ItemFilter>> FILTERS = new HashMap<>();

    static ItemFilter loadFilter(ItemStack itemStack) {
        return FILTERS.get(itemStack.getItem()).apply(itemStack);
    }

    /**
     * Retrieves the configured item count for the supplied item.
     *
     * @return The amount configured for the supplied item stack.<br>
     *         If the stack is not matched by this filter, 0 is returned instead.
     */
    int testItemCount(ItemStack itemStack);

    /**
     * @return Whether this filter supports querying for exact item amounts.
     */
    default boolean supportsAmounts() {
        return !isBlackList();
    }

    /**
     * An empty item filter that allows all items.<br>
     * ONLY TO BE USED FOR ITEM MATCHING! All other functionality will throw an exception.
     */
    ItemFilter EMPTY = new ItemFilter() {

        @Override
        public int testItemCount(ItemStack itemStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return true;
        }

        @Override
        public WidgetGroup openConfigurator(int x, int y) {
            throw new NotImplementedException("Not available for empty item filter");
        }

        @Override
        public CompoundTag saveFilter() {
            throw new NotImplementedException("Not available for empty item filter");
        }

        @Override
        public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
            throw new NotImplementedException("Not available for empty item filter");
        }
    };
}
