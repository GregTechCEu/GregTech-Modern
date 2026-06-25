package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.core.mixins.BuiltInRegistriesAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;

import java.security.Key;

@Mod.EventBusSubscriber(modid = "gtceu")
public final class GTRegistries {

    private GTRegistries() {}

    public static final class Keys {

        // Material related registries

        public static final ResourceKey<Registry<Material>> MATERIAL = makeRegistryKey(GTCEu.id("material"));
        public static final ResourceKey<Registry<Element>> ELEMENT = makeRegistryKey(GTCEu.id("element"));
        public static final ResourceKey<Registry<TagPrefix>> TAG_PREFIX = makeRegistryKey(GTCEu.id("tag_prefix"));
        public static final ResourceKey<Registry<MaterialIconSet>> MATERIAL_ICON_SET = makeRegistryKey(GTCEu.id("material_icon_set"));

        // Recipe related registries

        public static final ResourceKey<Registry<GTRecipeType>> RECIPE_TYPE = makeRegistryKey(GTCEu.id("recipe_type"));
        public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY = makeRegistryKey(GTCEu.id("recipe_category"));
        public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY = makeRegistryKey(GTCEu.id("recipe_capability"));
        public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION = makeRegistryKey(GTCEu.id("recipe_condition"));
        public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC = makeRegistryKey(GTCEu.id("chance_logic"));

        // Worldgen related registries

        public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID_DEFINITION = makeRegistryKey(GTCEu.id("bedrock_fluid"));
        public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE_DEFINITION = makeRegistryKey(GTCEu.id("bedrock_ore"));
        public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN = makeRegistryKey(GTCEu.id("ore_vein"));
        public static final ResourceKey<Registry<IWorldGenLayer>> WORLD_GEN_LAYER = makeRegistryKey(GTCEu.id("world_gen_layer"));

        // Other registries

        public static final ResourceKey<Registry<CoverDefinition>> COVER = makeRegistryKey(GTCEu.id("cover"));
        public static final ResourceKey<Registry<MachineDefinition>> MACHINE = makeRegistryKey(GTCEu.id("machine"));

        public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER = makeRegistryKey(GTCEu.id("dimension_marker"));
        public static final ResourceKey<Registry<MedicalCondition>> MEDICAL_CONDITION = makeRegistryKey(GTCEu.id("medical_condition"));
        public static final ResourceKey<Registry<PatternError.PatternErrorType>> PATTERN_ERROR_TYPE = makeRegistryKey(
                GTCEu.id("pattern_error_type"));
        public static final ResourceKey<Registry<Placeholder>> PLACEHOLDER = makeRegistryKey(GTCEu.id("placeholder"));

    }

    // spotless:off

    // Material related registries

    public static final MaterialRegistry MATERIALS = new MaterialRegistry();
    public static final GTRegistry<Element> ELEMENTS = new GTRegistry<>(GTCEu.id("element"));
    public static final GTRegistry<TagPrefix> TAG_PREFIXES = new GTRegistry<>(GTCEu.id("tag_prefix"));
    public static final GTRegistry<MaterialIconSet> MATERIAL_ICON_SETS = new GTRegistry<>(GTCEu.id("material_icon_set"));

    // Recipe related registries

    public static final GTRegistry<GTRecipeType> RECIPE_TYPES = new GTRegistry<>(GTCEu.id("recipe_type"));
    public static final GTRegistry<GTRecipeCategory> RECIPE_CATEGORIES = new GTRegistry<>(GTCEu.id("recipe_category"));
    public static final MappedRegistry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(Keys.RECIPE_CAPABILITY);
    public static final MappedRegistry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(Keys.RECIPE_CONDITION);
    public static final MappedRegistry<ChanceLogic> CHANCE_LOGICS = makeRegistry(Keys.CHANCE_LOGIC);

    // Worldgen related registries

    public static final GTRegistry<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry<>(GTCEu.id("bedrock_ore"));
    public static final GTRegistry<GTOreDefinition> ORE_VEINS = new GTRegistry<>(GTCEu.id("ore_vein"));
    public static final GTRegistry<IWorldGenLayer> WORLD_GEN_LAYERS = new GTRegistry<>(GTCEu.id("world_gen_layer"));

    // Other registries

    public static final MappedRegistry<CoverDefinition> COVERS = makeRegistry(Keys.COVER);
    public static final GTRegistry<MachineDefinition> MACHINES = new GTRegistry<>(GTCEu.id("machine"));
    public static final GTRegistry<SoundEntry> SOUNDS = new GTRegistry<>(GTCEu.id("sound"));
    public static final MappedRegistry<DimensionMarker> DIMENSION_MARKERS = makeRegistry(Keys.DIMENSION_MARKER);
    public static final MappedRegistry<MedicalCondition> MEDICAL_CONDITIONS = makeRegistry(Keys.MEDICAL_CONDITION);

    public static final MappedRegistry<Placeholder> PLACEHOLDERS = makeRegistry(Keys.PLACEHOLDER);
    public static final MappedRegistry<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = makeRegistry(Keys.PATTERN_ERROR_TYPE);

    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIES = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GTCEu.MOD_ID);

    // spotless:on

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

    private static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    private static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
    }

    @SuppressWarnings("unchecked")
    private static <T, R extends WritableRegistry<T>> R makeRegistry(ResourceKey<Registry<T>> key, R registry) {
        BuiltInRegistriesAccessor.gtceu$getWRITABLE_REGISTRY().register(
                (ResourceKey<WritableRegistry<?>>) (Object) key, registry, Lifecycle.stable());
        return registry;
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

    public static void init() {}
}
