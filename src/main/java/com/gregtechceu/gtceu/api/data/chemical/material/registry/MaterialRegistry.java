package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

public class MaterialRegistry extends GTRegistry.RL<Material> {

    @Getter
    private final Set<java.lang.String> usedNamespaces = new ObjectOpenHashSet<>();

    private Phase registrationPhase = Phase.PRE;

    public MaterialRegistry() {
        super(GTCEu.id("material"));
    }

    public Material get(java.lang.String name) {
        ResourceLocation location = ResourceLocation.tryParse(GTCEu.appendIdString(name));
        if (location != null) return get(location);
        return GTMaterials.NULL;
    }

    @Override
    public <T extends Material> T register(@NotNull ResourceLocation key, @NotNull T value) {
        if (registrationPhase == Phase.CLOSED || registrationPhase == Phase.FROZEN) {
            GTCEu.LOGGER.error(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the MaterialEvent. Skipping material {}...",
                    key);
            return null;
        }
        super.register(key, value);
        usedNamespaces.add(key.getNamespace());
        return value;
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
        return super.values();
    }

    public void closeRegistry() {
        registrationPhase = Phase.CLOSED;
    }

    @Override
    public void freeze() {
        super.freeze();
        registrationPhase = Phase.FROZEN;
    }

    @Override
    public void unfreeze() {
        super.unfreeze();
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
