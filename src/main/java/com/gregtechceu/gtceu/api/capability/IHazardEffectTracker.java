package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface IHazardEffectTracker {

    /**
     * @return a map of hazard effect to how long it's been applied for.
     */
    Object2IntMap<HazardProperty.HazardEffect> getCurrentHazardEffects();

    /**
     * @return the maximum air supply for the entity this is attached to. -1 for default (300).
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();

    void removeHazardItem(HazardProperty property);

    void addHazardItem(HazardProperty property);

    void tick();
}
