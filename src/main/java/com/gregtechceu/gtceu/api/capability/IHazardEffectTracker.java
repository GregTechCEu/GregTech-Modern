package com.gregtechceu.gtceu.api.capability;


import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Map;
import java.util.Set;

public interface IHazardEffectTracker {

    /**
     * @return a map of the hazard types to their effects.
     */
    Map<HazardProperty.HazardType, Set<HazardProperty.HazardEffect>> getTypesToEffects();

    /**
     * @return a map of hazard effect to how long it's been applied for.
     */
    Object2IntMap<HazardProperty.HazardEffect> getCurrentHazardEffects();

    /**
     * @return the maximum air supply for the entity this is attached to. -1 for default (300).
     */
    // default maxAirSupply for players is 300.
    int getMaxAirSupply();

    void removeHazardItem(UnificationEntry entry);

    void addHazardItem(UnificationEntry entry);

    void tick();
}
