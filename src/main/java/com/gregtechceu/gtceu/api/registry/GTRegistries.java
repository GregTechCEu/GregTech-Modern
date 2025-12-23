package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

public final class GTRegistries {

    // GT Registry
    public static final GTRegistry.String<Element> ELEMENTS = new GTRegistry.String<>(GTCEu.id("element"));

    public static final GTRegistry.RL<GTRecipeType> RECIPE_TYPES = new GTRegistry.RL<>(GTCEu.id("recipe_type"));
    public static final GTRegistry.RL<GTRecipeCategory> RECIPE_CATEGORIES = new GTRegistry.RL<>(
            GTCEu.id("recipe_category"));
    public static final GTRegistry.RL<CoverDefinition> COVERS = new GTRegistry.RL<>(GTCEu.id("cover"));

    public static final GTRegistry.RL<MachineDefinition> MACHINES = new GTRegistry.RL<>(GTCEu.id("machine"));
    public static final GTRegistry.String<RecipeCapability<?>> RECIPE_CAPABILITIES = new GTRegistry.String<>(
            GTCEu.id("recipe_capability"));
    public static final GTRegistry.String<RecipeConditionType<?>> RECIPE_CONDITIONS = new GTRegistry.String<>(
            GTCEu.id("recipe_condition"));
    public static final GTRegistry.String<ChanceLogic> CHANCE_LOGICS = new GTRegistry.String<>(
            GTCEu.id("chance_logic"));
    public static final GTRegistry.RL<SoundEntry> SOUNDS = new GTRegistry.RL<>(GTCEu.id("sound"));
    public static final GTRegistry.RL<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry.RL<>(
            GTCEu.id("bedrock_fluid"));
    public static final GTRegistry.RL<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry.RL<>(
            GTCEu.id("bedrock_ore"));
    public static final GTRegistry.RL<GTOreDefinition> ORE_VEINS = new GTRegistry.RL<>(GTCEu.id("ore_vein"));
    public static final GTRegistry.RL<DimensionMarker> DIMENSION_MARKERS = new GTRegistry.RL<>(
            GTCEu.id("dimension_marker"));
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = DeferredRegister
            .create(Registries.TRUNK_PLACER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER = DeferredRegister
            .create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIES = DeferredRegister
            .create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);
    public static final GTRegistry<String, Function<FriendlyByteBuf, FluidIngredient>> FLUID_SERIALIZERS = new GTRegistry.String<>(
            GTCEu.id("fluid_serializers"));

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        ResourceKey<?> registryKey = registry.key();

        if (registryKey == Registries.RECIPE_TYPE) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registryKey == Registries.RECIPE_SERIALIZER) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else if (registryKey == Registries.FEATURE) {
            ForgeRegistries.FEATURES.register(name, (Feature<?>) value);
        } else if (registryKey == Registries.FOLIAGE_PLACER_TYPE) {
            ForgeRegistries.FOLIAGE_PLACER_TYPES.register(name, (FoliagePlacerType<?>) value);
        } else if (registryKey == Registries.TRUNK_PLACER_TYPE) {
            TRUNK_PLACER_TYPE.register(name.getPath(), () -> (TrunkPlacerType<?>) value);
        } else if (registryKey == Registries.PLACEMENT_MODIFIER_TYPE) {
            PLACEMENT_MODIFIER.register(name.getPath(), () -> (PlacementModifierType<?>) value);
        } else {
            return Registry.register(registry, name, value);
        }

        return value;
    }

    public static void init(IEventBus eventBus) {
        TRUNK_PLACER_TYPE.register(eventBus);
        PLACEMENT_MODIFIER.register(eventBus);
        GLOBAL_LOOT_MODIFIES.register(eventBus);
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (GTCEu.isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return FROZEN;
    }

    private static class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            } else {
                return FROZEN;
            }
        }
    }
}
