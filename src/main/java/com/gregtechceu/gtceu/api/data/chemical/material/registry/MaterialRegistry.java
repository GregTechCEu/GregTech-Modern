package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialRegistry extends MappedRegistry<Material> {

    @Getter
    private final Set<String> usedNamespaces = new ObjectOpenHashSet<>();

    private Phase registrationPhase = Phase.PRE;

    public MaterialRegistry() {
        super(GTRegistries.Keys.MATERIAL, Lifecycle.stable());
    }

    /**
     * Use {@link #get(ResourceLocation)} instead
     */
    @Deprecated(since = "8.0.0")
    public Material get(String name) {
        ResourceLocation location = ResourceLocation.tryParse(GTCEu.appendIdString(name));
        if (location != null) return Objects
                .requireNonNullElse(get(ResourceKey.create(GTRegistries.Keys.MATERIAL, location)), GTMaterials.NULL);
        return GTMaterials.NULL;
    }

    public Material getOrThrow(ResourceLocation id) {
        return getOrThrow(ResourceKey.create(GTRegistries.Keys.MATERIAL, id));
    }

    @Override
    public Holder.Reference<Material> registerMapping(int id, ResourceKey<Material> key, Material value,
                                                      Lifecycle lifecycle) {
        if (registrationPhase == Phase.CLOSED || registrationPhase == Phase.FROZEN) {
            throw new IllegalStateException(
                    "Failed to register material %s. Materials cannot be registered in the PostMaterialEvent (or after)!"
                            .formatted(key));
        }
        usedNamespaces.add(key.location().getNamespace());
        return super.registerMapping(id, key, value, lifecycle);
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
    public @UnmodifiableView Set<Material> values() {
        if (registrationPhase == Phase.PRE || registrationPhase == Phase.OPEN)
            throw new IllegalStateException("Cannot retrieve all materials before registration");
        return super.stream().collect(Collectors.toSet());
    }

    public void closeRegistry() {
        registrationPhase = Phase.CLOSED;
    }

    @Override
    public Registry<Material> freeze() {
        registrationPhase = Phase.FROZEN;
        return super.freeze();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void unfreeze() {
        super.unfreeze();
        registrationPhase = Phase.OPEN;
    }

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
