package com.gregtechceu.gtceu.api.registry;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote ElementRegistry
 */
public final class GTRegistries {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTCEu.MOD_ID);

    public static final GTRegistry.RL<GTRegistry<?, ?>> REGISTRIES = new GTRegistry.RL<>(GTCEu.id("root"));

    // GT Registry
    public static final GTRegistry.String<Element> ELEMENTS = new GTRegistry.String<>(GTCEu.id("element"));

    public static final GTRegistry.String<Material> MATERIALS = new GTRegistry.String<>(GTCEu.id("material"));

    public static final GTRegistry.RL<GTRecipeType> RECIPE_TYPES = new GTRegistry.RL<>(GTCEu.id("recipe_type"));
    public static final GTRegistry.RL<CoverDefinition> COVERS = new GTRegistry.RL<>(GTCEu.id("cover"));

    public static final GTRegistry.RL<MachineDefinition> MACHINES = new GTRegistry.RL<>(GTCEu.id("machine"));
    public static final GTRegistry.String<RecipeCapability<?>> RECIPE_CAPABILITIES = new GTRegistry.String<>(GTCEu.id("recipe_capability"));
    public static final GTRegistry.String<Class<? extends RecipeCondition>> RECIPE_CONDITIONS = new GTRegistry.String<>(GTCEu.id("recipe_condition"));
    public static final GTRegistry.RL<SoundEntry> SOUNDS = new GTRegistry.RL<>(GTCEu.id("sound"));
    public static final GTRegistry.RL<CompassSection> COMPASS_SECTIONS = new GTRegistry.RL<>(GTCEu.id("compass_section"));
    public static final GTRegistry.RL<CompassNode> COMPASS_NODES = new GTRegistry.RL<>(GTCEu.id("compass_node"));
    public static final GTRegistry.RL<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry.RL<GTOreFeatureEntry> ORE_VEINS = new GTRegistry.RL<>(GTCEu.id("ore_vein"));

    @ExpectPlatform
    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        throw new AssertionError();
    }

    public static RegistryAccess builtinRegistry() {
        if (Platform.getMinecraftServer() != null) {
            return Platform.getMinecraftServer().registryAccess();
        }
        throw new IllegalStateException();
    }
}
