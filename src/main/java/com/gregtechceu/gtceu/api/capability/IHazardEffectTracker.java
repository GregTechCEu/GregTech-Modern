package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.material.material.Material;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Set;

public interface IHazardEffectTracker extends INBTSerializable<CompoundTag> {

    /**
     * @return a set of hazard effect to how long it's been applied for.
     */
    Set<Material> getExtraHazards();

    /**
     * @return a map of material to how long its effects been applied for.
     */
    Object2IntMap<Material> getCurrentHazards();

    /**
     * @return the maximum air supply for the entity this is attached to. -1 for default (300).
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();

    void startTick();

    void tick(Material material);

    void endTick();
}
