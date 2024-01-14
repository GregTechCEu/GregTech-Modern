package com.gregtechceu.gtceu.api.data.chemical.material.event;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Event to register and modify materials in
 */
public class MaterialEvent extends GenericEvent<Material> {

    public MaterialEvent() {
        super(Material.class);
    }
}