package com.gregtechceu.gtceu.api.item.component.forge;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote ElectricStatsImpl
 */
public class ElectricStatsImpl extends ElectricStats implements IComponentCapability {
    public ElectricStatsImpl(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        super(maxCharge, tier, chargeable, dischargeable);
    }

    public static ElectricStats create(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        return new ElectricStatsImpl(maxCharge, tier, chargeable, dischargeable);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> capability) {
        if (capability == GTCapability.CAPABILITY_ELECTRIC_ITEM) {
            return GTCapability.CAPABILITY_ELECTRIC_ITEM.orEmpty(capability, LazyOptional.of(() -> new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable)));
        }
        return LazyOptional.empty();
    }
}
