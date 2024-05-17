package com.gregtechceu.gtceu.api.item.component.forge;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote IComponentCapability
 */
public interface IComponentCapability {

    void attachCapabilites(RegisterCapabilitiesEvent event, Item item);
}
