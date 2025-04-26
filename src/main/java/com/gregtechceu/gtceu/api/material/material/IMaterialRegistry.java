package com.gregtechceu.gtceu.api.material.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.IMappedRegistryAccess;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.stream.Stream;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface IMaterialRegistry extends Iterable<Material> {

    /**
     * @return all namespaces the registered materials use
     */
    @UnmodifiableView
    @NotNull
    Collection<String> getUsedNamespaces();

    /**
     * Register a material.
     *
     * @param material the material to register
     * @return the same material.
     */
    Material register(Material material);

    /**
     * Get a material from a String in formats:
     * <ul>
     * <li>{@code "modid:registry_name"}</li>
     * <li>{@code "registry_name"} - where modid is inferred to be {@link GTCEu#MOD_ID}</li>
     * </ul>
     * Generally, you should use {@linkplain IMaterialRegistry#getMaterial(ResourceLocation)} instead.
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     * @see IMaterialRegistry#getMaterial(ResourceLocation)
     */
    default Material getMaterial(String name) {
        return getMaterial(GTCEu.id(name));
    }

    /**
     * Get a material from a ResourceLocation<br/>
     * Intended for use in reading/writing materials from/to NBT tags.
     *
     * @param name the name of the material in the above format
     * @return the material associated with the name
     * @see IMaterialRegistry#getMaterial(String)
     */
    Material getMaterial(ResourceLocation name);

    ResourceLocation getKey(Material material);

    /**
     * Set the fallback material for a namespace.
     * This is only for manual fallback usage.
     *
     * @param modId    the namespace to set the fallback for
     * @param material the fallback material
     */
    void setFallbackMaterial(@NotNull String modId, @NotNull Material material);

    /**
     * This is only for manual fallback usage.
     *
     * @param modId the namespace to get the fallback for
     * @return the fallback material, used for when another material does not exist
     */
    @NotNull
    Material getFallbackMaterial(@NotNull String modId);

    Stream<Material> stream();

    /**
     *
     * @return {@code true} if this registry is frozen, {@code false} otherwise
     * @see IMappedRegistryAccess#gtceu$isFrozen()
     */
    boolean isFrozen();
}
