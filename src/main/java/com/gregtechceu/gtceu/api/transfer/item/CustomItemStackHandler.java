package com.gregtechceu.gtceu.api.transfer.item;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CustomItemStackHandler extends ItemStackHandler
                                    implements IContentChangeAware, INBTSerializable<CompoundTag> {

    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};
    @Getter
    @Setter
    protected Predicate<ItemStack> filter = stack -> true;

    public CustomItemStackHandler() {
        super();
    }

    public CustomItemStackHandler(int size) {
        super(size);
    }

    public CustomItemStackHandler(ItemStack itemStack) {
        this(NonNullList.of(ItemStack.EMPTY, itemStack));
    }

    public CustomItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return filter.test(stack);
    }

    @Override
    public void onContentsChanged(int slot) {
        onContentsChanged.run();
    }

    /**
     * Don't use unless necessary.<br>
     * (A good use case is loading/saving this container's items from/to a {@link ItemContainerContents} component)
     * 
     * @return the internal list of items in this handler
     */
    @ApiStatus.Internal
    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public void clear() {
        stacks.clear();
        onContentsChanged.run();
    }
}
