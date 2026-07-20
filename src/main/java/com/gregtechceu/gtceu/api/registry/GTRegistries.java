package com.gregtechceu.gtceu.api.registry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
import com.mojang.serialization.Lifecycle;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraftforge.registries.IdMappingEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.checkerframework.checker.units.qual.K;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.security.Key;
import java.util.*;

@SuppressWarnings("unused")
public final class GTRegistries {

    private static final LinkedHashSet<ResourceLocation> LOAD_ORDER = new LinkedHashSet<>();
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
        public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE = makeRegistryKey(GTCEu.id("recipe_type"));
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
        public static final ResourceKey<Registry<IWorldGenLayer>> WORLD_GEN_LAYER = makeRegistryKey(GTCEu.id("world_gen_layer"));
        public static final ResourceKey<Registry<PatternError.PatternErrorType>> PATTERN_ERROR_TYPE = makeRegistryKey(GTCEu.id("pattern_error_type"));
        public static final ResourceKey<Registry<Placeholder>> PLACEHOLDER = makeRegistryKey(GTCEu.id("placeholder"));
    }

    // spotless:off

    // Material related registries

    // spotless:off

    public static final MaterialRegistry MATERIALS = new MaterialRegistry();
    public static final GTRegistry.RL<Element> ELEMENTS = new GTRegistry.RL<>(GTCEu.id("element"));
    public static final GTRegistry.RL<TagPrefix> TAG_PREFIXES = new GTRegistry.RL<>(GTCEu.id("tag_prefix"));
    public static final GTRegistry.RL<MaterialIconSet> MATERIAL_ICON_SETS = new GTRegistry.RL<>(GTCEu.id("material_icon_set"));

    // Recipe related registries

    public static final GTRegistry.RL<GTRecipeType> RECIPE_TYPES = new GTRegistry.RL<>(GTCEu.id("recipe_type"));
    public static final GTRegistry.RL<GTRecipeCategory> RECIPE_CATEGORIES = new GTRegistry.RL<>(GTCEu.id("recipe_category"));
    public static final GTRegistry.RL<RecipeCapability<?>> RECIPE_CAPABILITIES = new GTRegistry.RL<>(GTCEu.id("recipe_capability"));
    public static final GTRegistry.RL<RecipeConditionType<?>> RECIPE_CONDITIONS = new GTRegistry.RL<>(GTCEu.id("recipe_condition"));
    public static final GTRegistry.RL<ChanceLogic> CHANCE_LOGICS = new GTRegistry.RL<>(GTCEu.id("chance_logic"));

    // Worldgen related registries

    public static final GTRegistry.RL<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry.RL<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_ore"));
    public static final GTRegistry.RL<GTOreDefinition> ORE_VEINS = new GTRegistry.RL<>(GTCEu.id("ore_vein"));
    public static final GTRegistry.RL<IWorldGenLayer> WORLD_GEN_LAYERS = new GTRegistry.RL<>(GTCEu.id("world_gen_layer"));

    // Other registries

    public static final GTRegistry.RL<CoverDefinition> COVERS = new GTRegistry.RL<>(GTCEu.id("cover"));
    public static final GTRegistry.RL<MachineDefinition> MACHINES = new GTRegistry.RL<>(GTCEu.id("machine"));
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
        BuiltInRegistriesAccessor.gtceu$getWRITABLE_REGISTRY().register((ResourceKey<WritableRegistry<?>>) (Object) key,
                registry, Lifecycle.stable());
        LOAD_ORDER.add(key.location());
        REGISTRIES.put((ResourceKey<Registry<?>>)(Object)key, registry);
        return registry;
    }

    @UnmodifiableView
    public static LinkedHashSet<ResourceLocation> getRegistryOrder() {
        return LOAD_ORDER;
    }

    @UnmodifiableView
    public static Collection<Registry<?>> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

    private static final Table<Registry<?>, ResourceLocation, Object> TO_REGISTER = HashBasedTable.create();
    private static boolean isFrozen = true;

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        ResourceKey<?> registryKey = registry.key();

        if (registryKey == Registries.RECIPE_TYPE) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registryKey == Registries.RECIPE_SERIALIZER) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else {
            if (!isFrozen) {
                Registry.register(registry, name, value);
            } else {
                TO_REGISTER.put(registry, name, value);
            }
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
        MinecraftForge.EVENT_BUS.addListener(GTRegistries::onFreeze);
    }


    public static void init() {}

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
