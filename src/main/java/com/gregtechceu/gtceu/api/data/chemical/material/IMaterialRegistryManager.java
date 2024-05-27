package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface IMaterialRegistryManager {

    /**
     * Create a registry for a modid. Accessible when in phase {@link Phase#PRE}.
     *
     * @param modid the mod id for the registry
     * @return the registry for the mod
     */
    @NotNull
    MaterialRegistry createRegistry(@NotNull String modid);

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param modid the modid of the mod
     * @return the registry associated with the mod, or the GregTech registry if it does not have one
     */
    @NotNull
    MaterialRegistry getRegistry(@NotNull String modid);

    /**
     * Get a mod's registry. Accessible during all phases.
     *
     * @param networkId the network ID of the registry
     * @return the registry associated with the network ID, or the GregTech registry if it does not have one
     */
    @NotNull
    MaterialRegistry getRegistry(int networkId);

    /**
     * Accessible when in phases:
     * <ul>
     * <li>{@link Phase#OPEN}</li>
     * <li>{@link Phase#CLOSED}</li>
     * <li>{@link Phase#FROZEN}</li>
     * </ul>
     *
     * @return all the Material Registries
     */
    @NotNull
    Collection<MaterialRegistry> getRegistries();

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
    Collection<Material> getRegisteredMaterials();

    /**
     * Get a material from a String in formats:
     * <ul>
     * <li>{@code "modid:registry_name"}</li>
     * <li>{@code "registry_name"} - where modid is inferred to be {@link com.gregtechceu.gtceu.GTCEu#MOD_ID}</li>
     * </ul>
     *
     * Intended for use in reading/writing materials from/to NBT tags.
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     */
    Material getMaterial(String name);

    ResourceLocation getKey(Material material);

    /**
     * @return the current phase in the material registration process
     * @see Phase
     */
    @NotNull
    Phase getPhase();

    default boolean canModifyMaterials() {
        return this.getPhase() != Phase.FROZEN && this.getPhase() != Phase.PRE;
    }

    default Codec<Material> codec() {
        return ResourceLocation.CODEC
                .flatXmap(
                        id -> Optional.ofNullable(this.getRegistry(id.getNamespace()).get(id.getPath()))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult
                                        .error(() -> "Unknown registry key in material registry: " + id)),
                        obj -> DataResult.success(obj.getResourceLocation()));
    }

    enum Phase {
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
