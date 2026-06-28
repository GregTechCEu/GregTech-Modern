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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = "gtceu")
public final class GTRegistries {

    private static final HashMap<ResourceLocation, Registry<?>> REGISTRIES = new HashMap<>();

    private GTRegistries() {}

    // spotless:off
    public static final class Keys {

        private Keys() {}

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

        // Datapack registries

        public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID = makeRegistryKey(GTCEu.id("bedrock_fluid"));
        public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE = makeRegistryKey(GTCEu.id("bedrock_ore"));
        public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN = makeRegistryKey(GTCEu.id("ore_vein"));

        // Worldgen related registries

        public static final ResourceKey<Registry<IWorldGenLayer>> WORLD_GEN_LAYER = makeRegistryKey(GTCEu.id("world_gen_layer"));

        // Other registries

        public static final ResourceKey<Registry<CoverDefinition>> COVER = makeRegistryKey(GTCEu.id("cover"));
        public static final ResourceKey<Registry<MachineDefinition>> MACHINE = makeRegistryKey(GTCEu.id("machine"));

        public static final ResourceKey<Registry<SoundEntry>> SOUND = makeRegistryKey(GTCEu.id("sound"));

        public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER = makeRegistryKey(GTCEu.id("dimension_marker"));
        public static final ResourceKey<Registry<MedicalCondition>> MEDICAL_CONDITION = makeRegistryKey(GTCEu.id("medical_condition"));
        public static final ResourceKey<Registry<PatternError.PatternErrorType>> PATTERN_ERROR_TYPE = makeRegistryKey(GTCEu.id("pattern_error_type"));
        public static final ResourceKey<Registry<Placeholder>> PLACEHOLDER = makeRegistryKey(GTCEu.id("placeholder"));
    }

    // Material related registries

    public static final MaterialRegistry MATERIALS = makeRegistry(Keys.MATERIAL, new MaterialRegistry());
    public static final MappedRegistry<Element> ELEMENTS = makeRegistry(Keys.ELEMENT);
    public static final MappedRegistry<TagPrefix> TAG_PREFIXES = makeRegistry(Keys.TAG_PREFIX);
    public static final MappedRegistry<MaterialIconSet> MATERIAL_ICON_SETS = makeRegistry(Keys.MATERIAL_ICON_SET);

    // Recipe related registries

    public static final MappedRegistry<GTRecipeType> RECIPE_TYPE = makeRegistry(Keys.RECIPE_TYPE);
    public static final MappedRegistry<GTRecipeCategory> RECIPE_CATEGORIES = makeRegistry(Keys.RECIPE_CATEGORY);
    public static final MappedRegistry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(Keys.RECIPE_CAPABILITY);
    public static final MappedRegistry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(Keys.RECIPE_CONDITION);
    public static final MappedRegistry<ChanceLogic> CHANCE_LOGICS = makeRegistry(Keys.CHANCE_LOGIC);

    // Worldgen related registries

    public static final GTRegistry<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry<>(GTCEu.id("bedrock_ore"));
    public static final GTRegistry<GTOreDefinition> ORE_VEINS = new GTRegistry<>(GTCEu.id("ore_vein"));
    public static final MappedRegistry<IWorldGenLayer> WORLD_GEN_LAYERS = makeRegistry(Keys.WORLD_GEN_LAYER);

    // Other registries

    public static final MappedRegistry<CoverDefinition> COVERS = makeRegistry(Keys.COVER);
    public static final MappedRegistry<MachineDefinition> MACHINES = makeRegistry(Keys.MACHINE);
    public static final MappedRegistry<SoundEntry> SOUNDS = makeRegistry(Keys.SOUND);
    public static final MappedRegistry<DimensionMarker> DIMENSION_MARKERS = makeRegistry(Keys.DIMENSION_MARKER);
    public static final MappedRegistry<MedicalCondition> MEDICAL_CONDITIONS = makeRegistry(Keys.MEDICAL_CONDITION);

    public static final MappedRegistry<Placeholder> PLACEHOLDERS = makeRegistry(Keys.PLACEHOLDER);
    public static final MappedRegistry<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = makeRegistry(Keys.PATTERN_ERROR_TYPE);
    // spotless:on

    private static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
        return ResourceKey.createRegistryKey(registryId);
    }

    private static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
    }

    @SuppressWarnings("unchecked")
    private static <T, R extends WritableRegistry<T>> R makeRegistry(ResourceKey<Registry<T>> key, R registry) {
        BuiltInRegistriesAccessor.gtceu$getWritableRegistry()
                .register((ResourceKey<WritableRegistry<?>>) (ResourceKey<?>) key, registry, Lifecycle.stable());
        REGISTRIES.put(key.location(), registry);
        return registry;
    }

    @UnmodifiableView
    public static Collection<Registry<?>> getRegistries() {
        return REGISTRIES.values();
    }

    private static RegistryAccess FROZEN = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

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
