package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ElectricItem implements IElectricItem {

    protected final ItemStack itemStack;

    protected final long maxCharge;
    protected final int tier;

    protected final boolean chargeable;
    protected final boolean canProvideEnergyExternally;

    public ElectricItem(ItemStack itemStack, long maxCharge, int tier, boolean chargeable,
                        boolean canProvideEnergyExternally) {
        this.itemStack = itemStack;
        this.maxCharge = maxCharge;
        this.tier = tier;
        this.chargeable = chargeable;
        this.canProvideEnergyExternally = canProvideEnergyExternally;
    }

    public void setCharge(long change) {
        itemStack.getOrCreateTag().putLong("Charge", change);
    }

    public void setMaxChargeOverride(long maxCharge) {
        itemStack.getOrCreateTag().putLong("MaxCharge", maxCharge);
    }

    @Override
    public long getTransferLimit() {
        return GTValues.V[getTier()];
    }

    @Override
    public long getMaxCharge() {
        var tagCompound = itemStack.getTag();
        if (tagCompound == null)
            return maxCharge;
        if (tagCompound.contains("MaxCharge", Tag.TAG_LONG))
            return tagCompound.getLong("MaxCharge");
        return maxCharge;
    }

    public long getCharge() {
        var tagCompound = itemStack.getTag();
        if (tagCompound == null)
            return 0;
        if (tagCompound.getBoolean("Infinite"))
            return getMaxCharge();
        return Math.min(tagCompound.getLong("Charge"), getMaxCharge());
    }

    public void setInfiniteCharge(boolean infiniteCharge) {
        itemStack.getOrCreateTag().putBoolean("Infinite", infiniteCharge);
    }

    @Override
    public boolean canProvideChargeExternally() {
        return this.canProvideEnergyExternally;
    }

    @Override
    public boolean chargeable() {
        return chargeable;
    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (itemStack.getCount() != 1) {
            return 0L;
        }
        if ((chargeable || amount == Long.MAX_VALUE) && (chargerTier >= tier) && amount > 0L) {
            long canReceive = getMaxCharge() - getCharge();
            if (!ignoreTransferLimit) {
                amount = Math.min(amount, getTransferLimit());
            }
            long charged = Math.min(amount, canReceive);
            if (!simulate) {
                setCharge(getCharge() + charged);
            }
            return charged;
        }
        return 0;
    }

    @Override
    public long discharge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean externally,
                          boolean simulate) {
        if (itemStack.getCount() != 1) {
            return 0L;
        }
        if ((canProvideEnergyExternally || !externally || amount == Long.MAX_VALUE) && (chargerTier >= tier) &&
                amount > 0L) {
            if (!ignoreTransferLimit) {
                amount = Math.min(amount, getTransferLimit());
            }
            long charge = getCharge();
            long discharged = Math.min(amount, charge);
            if (!simulate) {
                setCharge(charge - discharged);
            }
            return discharged;
        }
        return 0;
    }

    @Override
    public int getTier() {
        return tier;
    }
}
