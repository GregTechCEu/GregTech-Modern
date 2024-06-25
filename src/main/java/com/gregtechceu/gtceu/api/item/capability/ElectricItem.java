package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class ElectricItem implements IElectricItem {

    protected final Supplier<DataComponentType<SimpleEnergyContent>> componentType;
    protected ItemStack container;

    protected final long maxCharge;
    protected final int tier;

    protected final boolean chargeable;
    protected final boolean canProvideEnergyExternally;

    public ElectricItem(ItemStack container, long maxCharge, int tier, boolean chargeable,
                        boolean canProvideEnergyExternally) {
        componentType = GTDataComponents.ENERGY_CONTENT;
        this.container = container;
        if (container.has(componentType)) {
            this.maxCharge = getMaxCharge();
        } else {
            this.maxCharge = maxCharge;
            // do this here to force the max charge to be set on the stats
            setMaxChargeOverride(maxCharge);
        }
        this.tier = tier;
        this.chargeable = chargeable;
        this.canProvideEnergyExternally = canProvideEnergyExternally;
    }

    public void setCharge(long change) {
        container.update(componentType, new SimpleEnergyContent(maxCharge, 0),
                content -> content.withCharge(change));
    }

    public void setMaxChargeOverride(long maxCharge) {
        container.update(componentType, new SimpleEnergyContent(maxCharge, 0),
                content -> content.withMaxCharge(maxCharge));
    }

    @Override
    public long getTransferLimit() {
        return GTValues.V[getTier()];
    }

    @Override
    public long getMaxCharge() {
        if (!container.has(componentType)) {
            return maxCharge;
        }
        return container.get(componentType).maxCharge();
    }

    public long getCharge() {
        if (!container.has(componentType)) {
            return 0;
        }
        return container.get(componentType).charge();
    }

    public void setInfiniteCharge(boolean infiniteCharge) {
        container.update(componentType, new SimpleEnergyContent(maxCharge, 0),
                content -> content.withInfinite(infiniteCharge));
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
    public boolean isDischargeMode() {
        if (!container.has(componentType)) {
            return false;
        }
        return container.get(componentType).dischargeMode();
    }

    @Override
    public void setDischargeMode(boolean dischargeMode) {
        container.update(componentType, new SimpleEnergyContent(maxCharge, 0),
                content -> content.withDischargeMode(dischargeMode));
    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (container.getCount() != 1) {
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
        if (container.getCount() != 1) {
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
