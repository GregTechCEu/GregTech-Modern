package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Lifecycle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialRegistry extends DefaultedMappedRegistry<Material> {

    @Getter
    private final Set<String> usedNamespaces = new HashSet<>();

    private Phase registrationPhase = Phase.PRE;

    public MaterialRegistry() {
        super("gtceu:null", GTRegistries.Keys.MATERIAL, Lifecycle.stable(), true);
    }

    @Override
    public Holder.@NotNull Reference<Material> register(ResourceKey<Material> key, Material value,
                                                        Lifecycle lifecycle) {
        if (registrationPhase == Phase.CLOSED || registrationPhase == Phase.FROZEN) {
            GTCEu.LOGGER.error(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the MaterialEvent. Skipping material {}...",
                    key);
            return null;
        }
        usedNamespaces.add(key.location().getNamespace());
        return super.register(key, value, lifecycle);
    }

    /**
     * Accessible when in phases:
     * <ul>
     * <li>{@link Phase#CLOSED}</li>
     * <li>{@link Phase#FROZEN}</li>
     * </ul>
     *
     * @return all registered materials.
     */
    @NotNull
    public @UnmodifiableView Set<Material> values() {
        if (registrationPhase == Phase.PRE || registrationPhase == Phase.OPEN)
            throw new IllegalStateException("Cannot retrieve all materials before registration");
        return this.stream().collect(Collectors.toSet());
    }

    public void closeRegistry() {
        registrationPhase = Phase.CLOSED;
    }

    @Override
    public Registry<Material> freeze() {
        super.freeze();
        GTCEu.LOGGER.debug("Freezing material registry");
        registrationPhase = Phase.FROZEN;
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void unfreeze() {
        super.unfreeze();
        GTCEu.LOGGER.debug("Unfreezing material registry");
        registrationPhase = Phase.OPEN;
    }

    @NotNull
    public Phase getPhase() {
        return registrationPhase;
    }

    public boolean canModifyMaterials() {
        return getPhase() != Phase.FROZEN && getPhase() != Phase.PRE;
    }

    public enum Phase {
        /** Material Registration and Modification is not started */
        PRE,
        /** Material Registration and Modification is available */
        OPEN,
        /** Material Registration is unavailable and only Modification is available */
        CLOSED,
        /** Material Registration and Modification is unavailable */
        FROZEN
    }
}
