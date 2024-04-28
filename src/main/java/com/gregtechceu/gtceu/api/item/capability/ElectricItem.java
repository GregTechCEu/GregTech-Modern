package com.gregtechceu.gtceu.api.item.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

@AllArgsConstructor
public class ElectricItem implements IElectricItem {
    public static final Codec<ElectricItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("max_charge").forGetter(ElectricItem::getMaxCharge),
        Codec.LONG.optionalFieldOf("charge", 0L).forGetter(ElectricItem::getCharge),
        Codec.INT.fieldOf("tier").forGetter(ElectricItem::getTier),
        Codec.BOOL.fieldOf("chargeable").forGetter(ElectricItem::chargeable),
        Codec.BOOL.fieldOf("can_provide_energy_externally").forGetter(ElectricItem::canProvideChargeExternally),
        Codec.BOOL.optionalFieldOf("infinite", false).forGetter(ElectricItem::isInfinite),
        Codec.BOOL.optionalFieldOf("discharge_mode", false).forGetter(ElectricItem::isDischargeMode)
    ).apply(instance, ElectricItem::new));
    public static final StreamCodec<ByteBuf, ElectricItem> STREAM_CODEC = NeoForgeStreamCodecs.composite(
        ByteBufCodecs.VAR_LONG, item -> item.maxCharge,
        ByteBufCodecs.VAR_LONG, item -> item.charge,
        ByteBufCodecs.VAR_INT, item -> item.tier,
        ByteBufCodecs.BOOL, ElectricItem::isChargeable,
        ByteBufCodecs.BOOL, ElectricItem::isCanProvideEnergyExternally,
        ByteBufCodecs.BOOL, ElectricItem::isInfinite,
        ByteBufCodecs.BOOL, ElectricItem::isDischargeMode,
        ElectricItem::new
    );

    @Getter
    protected final long maxCharge;
    @Getter
    protected final long charge;
    @Getter
    protected final int tier;

    @Getter
    protected final boolean chargeable;
    @Getter
    protected final boolean canProvideEnergyExternally;
    @Getter
    protected final boolean infinite;
    @Getter
    protected final boolean dischargeMode;

    public ElectricItem(long maxCharge, int tier, boolean chargeable, boolean canProvideEnergyExternally) {
        this(maxCharge, 0, tier, chargeable, canProvideEnergyExternally, false, false);
    }

    public ElectricItem setCharge(long charge) {
        return new ElectricItem(maxCharge, charge, tier, chargeable, canProvideEnergyExternally, infinite, dischargeMode);
    }

    public ElectricItem setMaxChargeOverride(long maxCharge) {
        return new ElectricItem(maxCharge, charge, tier, chargeable, canProvideEnergyExternally, infinite, dischargeMode);
    }

    public ElectricItem setDischargeMode(boolean dischargeMode) {
        return new ElectricItem(maxCharge, charge, tier, chargeable, canProvideEnergyExternally, infinite, dischargeMode);
    }

    @Override
    public long getTransferLimit() {
        return GTValues.V[getTier()];
    }

    public ElectricItem setInfiniteCharge(boolean infinite) {
        return new ElectricItem(maxCharge, charge, tier, chargeable, canProvideEnergyExternally, infinite, dischargeMode);
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
    public long charge(ItemStack stack, long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (stack.getCount() != 1) {
            return 0L;
        }
        if ((chargeable || amount == Long.MAX_VALUE) && (chargerTier >= tier) && amount > 0L) {
            long canReceive = getMaxCharge() - getCharge();
            if (!ignoreTransferLimit) {
                amount = Math.min(amount, getTransferLimit());
            }
            long charged = Math.min(amount, canReceive);
            if (!simulate) {
                stack.set(GTDataComponents.ELECTRIC_ITEM, setCharge(charge + charged));
            }
            return charged;
        }
        return 0;
    }

    @Override
    public long discharge(ItemStack stack, long amount, int chargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        if (stack.getCount() != 1) {
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
                stack.set(GTDataComponents.ELECTRIC_ITEM, setCharge(charge - discharged));
            }
            return discharged;
        }
        return 0;
    }

}
