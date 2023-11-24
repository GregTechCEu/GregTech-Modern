package com.gregtechceu.gtceu.api.misc.fabric;

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.item.component.fabric.ThermalFluidStatsImpl;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.fabric.FluidHelperImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Phoupraw
 * @date 2023/8/9
 * @see ThermalFluidStatsImpl#onAttached
 */
public class FluidCellStorage extends SingleVariantItemStorage<FluidVariant> {

    private final long capacity;
    private final boolean allowPartialFill;
    private final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean acidProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;

    public FluidCellStorage(ContainerItemContext context, long capacity, boolean allowPartialFill, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        super(context);
        this.capacity = capacity;
        this.allowPartialFill = allowPartialFill;
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    @Override
    @ApiStatus.Internal
    public FluidVariant getBlankResource() {
        return FluidVariant.blank();
    }

    @Override
    @ApiStatus.Internal
    public FluidVariant getResource(ItemVariant currentVariant) {
        return FluidHelperImpl.toFluidVariant(getFluidStack(currentVariant));
    }

    @Override
    @ApiStatus.Internal
    public long getAmount(ItemVariant currentVariant) {
        return getFluidStack(currentVariant).getAmount();
    }

    @Override
    @ApiStatus.Internal
    public long getCapacity(FluidVariant variant) {
        return capacity;
    }

    @Override
    @ApiStatus.Internal
    public ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
        CompoundTag newNbt = currentVariant.copyOrCreateNbt();
        if (newResource.isBlank() || newAmount == 0) {
            newNbt.remove(FluidHandlerItemStack.FLUID_NBT_KEY);
            if (newNbt.isEmpty()) {
                newNbt = null;
            }
        } else {
            CompoundTag nbtFluid = FluidStack.create(newResource.getFluid(), newAmount, newResource.getNbt()).saveToTag(new CompoundTag());
            newNbt.put(FluidHandlerItemStack.FLUID_NBT_KEY, nbtFluid);
        }
        return ItemVariant.of(currentVariant.getItem(), newNbt);
    }

    /**
     * @see FilteringStorage#canInsert
     * @see IThermalFluidHandlerItemStack#canFillFluidType
     */
    @ApiStatus.Internal
    public boolean canInsert(FluidVariant resource) {
        int temperature = FluidVariantAttributes.getTemperature(resource);
        return temperature <= getMaxFluidTemperature() && !(temperature < 120 && !isCryoProof()) && (!FluidVariantAttributes.isLighterThanAir(resource) || isGasProof());
        //TODO acid, plasma
    }

    /**
     * @see FilteringStorage#canExtract
     */
    @ApiStatus.Internal
    public boolean canExtract(FluidVariant resource) {
        return true;
    }

    @Override
    public long insert(FluidVariant insertedResource, long maxAmount, TransactionContext transaction) {
        if (!canInsert(insertedResource)) return 0;
        if (!isAllowPartialFill()) {
            maxAmount = maxAmount < getCapacity(insertedResource) ? 0 : getCapacity(insertedResource);
        }
        return super.insert(insertedResource, maxAmount, transaction);
    }

    @Override
    public long extract(FluidVariant extractedResource, long maxAmount, TransactionContext transaction) {
        if (!canExtract(extractedResource)) return 0;
        if (!isAllowPartialFill()) {
            maxAmount = maxAmount < getAmount() ? 0 : getAmount();
        }
        return super.extract(extractedResource, maxAmount, transaction);
    }

    public FluidStack getFluidStack(ItemVariant currentVariant) {
        CompoundTag root = currentVariant.getNbt();
        if (root == null) return FluidStack.empty();
        return FluidStack.loadFromTag(root.getCompound(FluidHandlerItemStack.FLUID_NBT_KEY));
    }

    public boolean isAllowPartialFill() {
        return allowPartialFill;
    }

    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public boolean isAcidProof() {
        return acidProof;
    }

    public boolean isCryoProof() {
        return cryoProof;
    }

    public boolean isPlasmaProof() {
        return plasmaProof;
    }

}
