package com.gregtechceu.gtceu.api.data.chemical.material.event;

import com.gregtechceu.gtceu.api.data.chemical.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Event to add a material registry in.
 *
 * @see IMaterialRegistryManager#createRegistry(String)
 */
public class MaterialRegistryEvent extends GenericEvent<MaterialRegistry> {

    public MaterialRegistryEvent() {
        super(MaterialRegistry.class);
    }
}