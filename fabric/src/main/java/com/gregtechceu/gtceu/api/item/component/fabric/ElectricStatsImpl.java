package com.gregtechceu.gtceu.api.item.component.fabric;

import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import net.minecraft.world.item.Item;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote ElectricStatsImpl
 */
public class ElectricStatsImpl extends ElectricStats {
    public ElectricStatsImpl(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        super(maxCharge, tier, chargeable, dischargeable);
    }

    public static ElectricStats create(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        return new ElectricStatsImpl(maxCharge, tier, chargeable, dischargeable);
    }

    @Override
    public void onAttached(Item item) {
        super.onAttached(item);
        GTCapability.CAPABILITY_ELECTRIC_ITEM.registerForItems(((itemStack, context) -> new ElectricItem(itemStack, maxCharge, tier, chargeable, dischargeable)), item);
    }

}
