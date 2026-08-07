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
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.IdMappingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@SuppressWarnings("unused")
public final class GTRegistries {

    private static final SequencedSet<ResourceLocation> LOAD_ORDER = new LinkedHashSet<>();
    private static final LinkedHashMap<ResourceKey<Registry<?>>, Registry<?>> REGISTRIES = new LinkedHashMap<>();

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

        /**
         * Use {@link Registries#RECIPE_TYPE} instead of this. This only exists to simplify KubeJS registration.
         *
         * @see Registries#RECIPE_TYPE
         */
        @ApiStatus.Internal
        public static final ResourceKey<Registry<GTRecipeType>> RECIPE_TYPE = makeRegistryKey(GTCEu.id("recipe_type"));
        public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY = makeRegistryKey(GTCEu.id("recipe_category"));
        public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY = makeRegistryKey(GTCEu.id("recipe_capability"));
        public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION = makeRegistryKey(GTCEu.id("recipe_condition"));
        public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC = makeRegistryKey(GTCEu.id("chance_logic"));

        // Datapack registries

        public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID = makeRegistryKey(GTCEu.id("bedrock_fluid"));
        public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE = makeRegistryKey(GTCEu.id("bedrock_ore"));
        public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN = makeRegistryKey(GTCEu.id("ore_vein"));

        // Other registries

        public static final ResourceKey<Registry<CoverDefinition>> COVER = makeRegistryKey(GTCEu.id("cover"));
        public static final ResourceKey<Registry<MachineDefinition>> MACHINE = makeRegistryKey(GTCEu.id("machine"));

        public static final ResourceKey<Registry<SoundEntry>> SOUND = makeRegistryKey(GTCEu.id("sound"));

        public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER = makeRegistryKey(GTCEu.id("dimension_marker"));
        public static final ResourceKey<Registry<MedicalCondition>> MEDICAL_CONDITION = makeRegistryKey(GTCEu.id("medical_condition"));
        public static final ResourceKey<Registry<ToolBehaviorType<?>>> TOOL_BEHAVIOR = makeRegistryKey(GTCEu.id("tool_behavior"));
        public static final ResourceKey<Registry<IWorldGenLayer>> WORLD_GEN_LAYER = makeRegistryKey(GTCEu.id("world_gen_layer"));
        public static final ResourceKey<Registry<PatternError.PatternErrorType>> PATTERN_ERROR_TYPE = makeRegistryKey(GTCEu.id("pattern_error_type"));
        public static final ResourceKey<Registry<Placeholder>> PLACEHOLDER = makeRegistryKey(GTCEu.id("placeholder"));

        private static <T> ResourceKey<Registry<T>> makeRegistryKey(ResourceLocation registryId) {
            return ResourceKey.createRegistryKey(registryId);
        }
    }

    // GT Registries

    // Be careful when changing the order of these static fields, as changing the order of them also changes the order of registry load.

    public static final Registry<Element> ELEMENTS = makeRegistry(Keys.ELEMENT);
    public static final Registry<TagPrefix> TAG_PREFIXES = makeRegistry(Keys.TAG_PREFIX);
    public static final Registry<MaterialIconSet> MATERIAL_ICON_SETS = makeRegistry(Keys.MATERIAL_ICON_SET);
    public static final Registry<ToolBehaviorType<?>> TOOL_BEHAVIORS = makeRegistry(Keys.TOOL_BEHAVIOR);
    public static final Registry<MedicalCondition> MEDICAL_CONDITIONS = makeRegistry(Keys.MEDICAL_CONDITION);
    public static final MaterialRegistry MATERIALS = makeMaterialRegistry();

    public static final Registry<SoundEntry> SOUNDS = makeRegistry(Keys.SOUND, false);
    public static final Registry<ChanceLogic> CHANCE_LOGICS = makeRegistry(Keys.CHANCE_LOGIC);
    public static final Registry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(Keys.RECIPE_CAPABILITY);

    public static final Registry<DimensionMarker> DIMENSION_MARKERS = makeRegistry(Keys.DIMENSION_MARKER, false);
    /**
     * Use {@link BuiltInRegistries#RECIPE_TYPE} instead of this. This only exists to simplify KubeJS registration.
     *
     * @see Registries#RECIPE_TYPE
     */
    @ApiStatus.Internal
    public static final Registry<GTRecipeType> RECIPE_TYPES = makeRegistry(Keys.RECIPE_TYPE);
    public static final Registry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(Keys.RECIPE_CONDITION);
    public static final Registry<GTRecipeCategory> RECIPE_CATEGORIES = makeRegistry(Keys.RECIPE_CATEGORY);

    public static final Registry<MachineDefinition> MACHINES = makeRegistry(Keys.MACHINE);
    public static final Registry<CoverDefinition> COVERS = makeRegistry(Keys.COVER);

    public static final Registry<Placeholder> PLACEHOLDERS = makeRegistry(Keys.PLACEHOLDER);
    public static final Registry<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = makeRegistry(Keys.PATTERN_ERROR_TYPE);
    public static final Registry<IWorldGenLayer> WORLD_GEN_LAYERS = makeRegistry(Keys.WORLD_GEN_LAYER);

    // spotless:on

    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, true);
    }

    public static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key, boolean sync) {
        MappedRegistry<T> registry = (MappedRegistry<T>) new RegistryBuilder<>(key)
                .sync(sync)
                .create();
        addRegistryToLoadOrder(key, registry);
        return registry;
    }

    private static MaterialRegistry makeMaterialRegistry() {
        MaterialRegistry registry = new MaterialRegistry(Keys.MATERIAL);
        addRegistryToLoadOrder(Keys.MATERIAL, registry);
        return registry;
    }

    private static final Table<Registry<?>, ResourceLocation, Object> TO_REGISTER = HashBasedTable.create();
    private static boolean isFrozen = true;

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (!isFrozen) {
            Registry.register(registry, name, value);
        } else {
            TO_REGISTER.put(registry, name, value);
        }
        return value;
    }

    // ignore the generics and hope the registered objects are still correctly typed :3
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void actuallyRegister(RegisterEvent event) {
        for (Registry reg : TO_REGISTER.rowKeySet()) {
            event.register(reg.key(), helper -> {
                TO_REGISTER.row(reg).forEach(helper::register);
            });
        }
        TO_REGISTER.clear();
    }

    private static void onUnfreeze(RegisterEvent event) {
        isFrozen = false;
    }

    private static void onFreeze(IdMappingEvent event) {
        isFrozen = event.isFrozen();
    }

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, GTRegistries::onUnfreeze);
        eventBus.addListener(EventPriority.LOW, GTRegistries::actuallyRegister);
        NeoForge.EVENT_BUS.addListener(GTRegistries::onFreeze);
    }

    @SuppressWarnings("unchecked")
    private static void addRegistryToLoadOrder(ResourceKey<? extends Registry<?>> key, @Nullable Registry<?> registry) {
        LOAD_ORDER.add(key.location());
        if (registry != null) {
            REGISTRIES.put((ResourceKey<Registry<?>>) key, registry);
        }
    }

    @UnmodifiableView
    public static SequencedSet<ResourceLocation> getRegistryOrder() {
        return Collections.unmodifiableSequencedSet(LOAD_ORDER);
    }

    @UnmodifiableView
    public static Collection<Registry<?>> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }
}
