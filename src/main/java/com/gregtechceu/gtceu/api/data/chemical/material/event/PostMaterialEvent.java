package com.gregtechceu.gtceu.api.data.chemical.material.event;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Event to modify and perform post-processing on materials
 */
public class PostMaterialEvent extends GenericEvent<Material> {

    public PostMaterialEvent() {
        super(Material.class);
    }
}