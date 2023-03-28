package com.gregtechceu.gtceu.api.misc.fabric;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote FluidHandlerItemStack
 */
public class FluidHandlerItemStack implements SingleSlotStorage<FluidVariant> {
    public static final String FLUID_NBT_KEY = "Fluid";
    protected @NotNull ContainerItemContext container;
    protected long capacity;

    public FluidHandlerItemStack(@NotNull ContainerItemContext container, long capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    public @NotNull FluidStack getFluid() {
        CompoundTag tagCompound = this.container.getItemVariant().getNbt();
        return tagCompound != null && tagCompound.contains(FLUID_NBT_KEY) ? FluidStack.loadFromTag(tagCompound.getCompound(FLUID_NBT_KEY)) : FluidStack.empty();
    }

    protected boolean setFluid(FluidStack fluid, TransactionContext tx) {
        ItemStack newStack = this.container.getItemVariant().toStack();

        CompoundTag fluidTag = new CompoundTag();
        fluid.saveToTag(fluidTag);
        newStack.getOrCreateTag().put(FLUID_NBT_KEY, fluidTag);
        return this.container.exchange(ItemVariant.of(newStack), 1L, tx) == 1L;
    }

    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.container.getAmount() == 1L && !(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt())).isEmpty() && this.canFillFluidType(resource, maxAmount)) {
            FluidStack contained = this.getFluid();
            long fillAmount;
            if (contained.isEmpty()) {
                fillAmount = Math.min(this.capacity, maxAmount);
                FluidStack filled = FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt());
                filled.setAmount(fillAmount);
                if (this.setFluid(filled, transaction)) {
                    return fillAmount;
                }
            } else if (contained.isFluidEqual(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt()))) {
                fillAmount = Math.min(this.capacity - contained.getAmount(), maxAmount);
                if (fillAmount > 0L) {
                    contained.grow(fillAmount);
                    if (this.setFluid(contained, transaction)) {
                        return fillAmount;
                    }
                }
            }

            return 0L;
        } else {
            return 0L;
        }
    }

    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.container.getAmount() == 1L && !(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt())).isEmpty() && this.getFluid().isFluidEqual(FluidStack.create(resource.getFluid(), maxAmount, resource.getNbt())) && maxAmount > 0L) {
            FluidStack contained = this.getFluid();
            if (!contained.isEmpty() && this.canDrainFluidType(FluidVariant.of(contained.getFluid(), contained.getTag()), contained.getAmount())) {
                long drainAmount = Math.min(contained.getAmount(), maxAmount);
                contained.shrink(drainAmount);
                if (contained.isEmpty()) {
                    if (this.setContainerToEmpty(transaction)) {
                        return drainAmount;
                    }
                } else if (this.setFluid(contained, transaction)) {
                    return drainAmount;
                }

                return 0L;
            } else {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    public FluidVariant getResource() {
        return FluidVariant.of(this.getFluid().getFluid(), this.getFluid().getTag());
    }

    public long getAmount() {
        return this.getFluid().getAmount();
    }

    public long getCapacity() {
        return this.capacity;
    }

    public boolean canFillFluidType(FluidVariant variant, long amount) {
        return true;
    }

    public boolean canDrainFluidType(FluidVariant variant, long amount) {
        return true;
    }

    protected boolean setContainerToEmpty(TransactionContext tx) {
        ItemStack newStack = this.container.getItemVariant().toStack();
        newStack.removeTagKey(FLUID_NBT_KEY);
        return this.container.exchange(ItemVariant.of(newStack), 1L, tx) == 1L;
    }

    public static class SwapEmpty extends FluidHandlerItemStack {
        protected final ItemStack emptyContainer;

        public SwapEmpty(ContainerItemContext container, ItemStack emptyContainer, int capacity) {
            super(container, (long)capacity);
            this.emptyContainer = emptyContainer;
        }

        protected boolean setContainerToEmpty(TransactionContext tx) {
            boolean result = super.setContainerToEmpty(tx);
            Transaction nested = tx.openNested();

            boolean var4;
            label40: {
                try {
                    if (this.container.exchange(ItemVariant.of(this.emptyContainer), this.emptyContainer.getCount(), nested) == (long)this.emptyContainer.getCount()) {
                        nested.commit();
                        var4 = true;
                        break label40;
                    }
                } catch (Throwable var7) {
                    if (nested != null) {
                        try {
                            nested.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (nested != null) {
                    nested.close();
                }

                return result;
            }

            if (nested != null) {
                nested.close();
            }

            return var4;
        }
    }

    public static class Consumable extends FluidHandlerItemStack {
        public Consumable(ContainerItemContext container, int capacity) {
            super(container, capacity);
        }

        protected boolean setContainerToEmpty(TransactionContext tx) {
            boolean result = super.setContainerToEmpty(tx);
            Transaction nested = tx.openNested();

            boolean var4;
            label40: {
                try {
                    if (this.container.extract(this.container.getItemVariant(), 1L, nested) == 1L) {
                        nested.commit();
                        var4 = true;
                        break label40;
                    }
                } catch (Throwable var7) {
                    if (nested != null) {
                        try {
                            nested.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (nested != null) {
                    nested.close();
                }

                return result;
            }

            if (nested != null) {
                nested.close();
            }

            return var4;
        }
    }
}
