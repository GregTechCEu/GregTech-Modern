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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IdMappingEvent;
import net.minecraftforge.registries.RegisterEvent;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

import static net.minecraft.resources.ResourceKey.createRegistryKey;

@SuppressWarnings("unused")
public final class GTRegistries {

    private static final LinkedList<ResourceLocation> LOAD_ORDER = new LinkedList<>();
    private static final LinkedHashMap<ResourceKey<Registry<?>>, Registry<?>> REGISTRIES = new LinkedHashMap<>();

    private GTRegistries() {}

    // spotless:off
    public static final class Keys {
        private Keys() {}

        // Material related registries
        public static final ResourceKey<Registry<Material>> MATERIAL = createRegistryKey(GTCEu.id("material"));
        public static final ResourceKey<Registry<Element>> ELEMENT = createRegistryKey(GTCEu.id("element"));
        public static final ResourceKey<Registry<TagPrefix>> TAG_PREFIX = createRegistryKey(GTCEu.id("tag_prefix"));
        public static final ResourceKey<Registry<MaterialIconSet>> MATERIAL_ICON_SET = createRegistryKey(GTCEu.id("material_icon_set"));

        // Recipe related registries

        public static final ResourceKey<Registry<GTRecipeType>> RECIPE_TYPE = createRegistryKey(GTCEu.id("recipe_type"));
        public static final ResourceKey<Registry<GTRecipeCategory>> RECIPE_CATEGORY = createRegistryKey(GTCEu.id("recipe_category"));
        public static final ResourceKey<Registry<RecipeCapability<?>>> RECIPE_CAPABILITY = createRegistryKey(GTCEu.id("recipe_capability"));
        public static final ResourceKey<Registry<RecipeConditionType<?>>> RECIPE_CONDITION = createRegistryKey(GTCEu.id("recipe_condition"));
        public static final ResourceKey<Registry<ChanceLogic>> CHANCE_LOGIC = createRegistryKey(GTCEu.id("chance_logic"));

        // Datapack registries

        public static final ResourceKey<Registry<BedrockFluidDefinition>> BEDROCK_FLUID = createRegistryKey(GTCEu.id("bedrock_fluid"));
        public static final ResourceKey<Registry<BedrockOreDefinition>> BEDROCK_ORE = createRegistryKey(GTCEu.id("bedrock_ore"));
        public static final ResourceKey<Registry<GTOreDefinition>> ORE_VEIN = createRegistryKey(GTCEu.id("ore_vein"));

        // Other registries

        public static final ResourceKey<Registry<CoverDefinition>> COVER = createRegistryKey(GTCEu.id("cover"));
        public static final ResourceKey<Registry<MachineDefinition>> MACHINE = createRegistryKey(GTCEu.id("machine"));

        public static final ResourceKey<Registry<SoundEntry>> SOUND = createRegistryKey(GTCEu.id("sound"));

        public static final ResourceKey<Registry<DimensionMarker>> DIMENSION_MARKER = createRegistryKey(GTCEu.id("dimension_marker"));
        public static final ResourceKey<Registry<MedicalCondition>> MEDICAL_CONDITION = createRegistryKey(GTCEu.id("medical_condition"));
        public static final ResourceKey<Registry<IWorldGenLayer>> WORLD_GEN_LAYER = createRegistryKey(GTCEu.id("world_gen_layer"));
        public static final ResourceKey<Registry<PatternError.PatternErrorType>> PATTERN_ERROR_TYPE = createRegistryKey(GTCEu.id("pattern_error_type"));
        public static final ResourceKey<Registry<Placeholder>> PLACEHOLDER = createRegistryKey(GTCEu.id("placeholder"));
    }

    // Be careful when changing the order of these static fields, as changing the order of them also changes the order of registry load.

    public static final MappedRegistry<Element> ELEMENTS = makeRegistry(Keys.ELEMENT);
    public static final MappedRegistry<MaterialIconSet> MATERIAL_ICON_SETS = makeRegistry(Keys.MATERIAL_ICON_SET);
    public static final MappedRegistry<TagPrefix> TAG_PREFIXES = makeRegistry(Keys.TAG_PREFIX);
    public static final MappedRegistry<MedicalCondition> MEDICAL_CONDITIONS = makeRegistry(Keys.MEDICAL_CONDITION);
    public static final MaterialRegistry MATERIALS = makeRegistry(Keys.MATERIAL, new MaterialRegistry());
    public static final MappedRegistry<SoundEntry> SOUNDS = makeRegistry(Keys.SOUND);

    public static final MappedRegistry<ChanceLogic> CHANCE_LOGICS = makeRegistry(Keys.CHANCE_LOGIC);
    public static final MappedRegistry<RecipeCapability<?>> RECIPE_CAPABILITIES = makeRegistry(Keys.RECIPE_CAPABILITY);
    public static final MappedRegistry<DimensionMarker> DIMENSION_MARKERS = makeRegistry(Keys.DIMENSION_MARKER);
    public static final MappedRegistry<RecipeConditionType<?>> RECIPE_CONDITIONS = makeRegistry(Keys.RECIPE_CONDITION);
    public static final MappedRegistry<GTRecipeCategory> RECIPE_CATEGORIES = makeRegistry(Keys.RECIPE_CATEGORY);
    public static final MappedRegistry<GTRecipeType> RECIPE_TYPES = makeRegistry(Keys.RECIPE_TYPE);

    public static final MappedRegistry<CoverDefinition> COVERS = makeRegistry(Keys.COVER);
    public static final MappedRegistry<MachineDefinition> MACHINES = makeRegistry(Keys.MACHINE);
    public static final MappedRegistry<Placeholder> PLACEHOLDERS = makeRegistry(Keys.PLACEHOLDER);
    public static final MappedRegistry<IWorldGenLayer> WORLD_GEN_LAYERS = makeRegistry(Keys.WORLD_GEN_LAYER);
    public static final MappedRegistry<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = makeRegistry(Keys.PATTERN_ERROR_TYPE);

    public static final GTRegistry.RL<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry.RL<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_ore"));
    public static final GTRegistry.RL<GTOreDefinition> ORE_VEINS = new GTRegistry.RL<>(GTCEu.id("ore_vein"));

    // spotless:on

    private static <T> MappedRegistry<T> makeRegistry(ResourceKey<Registry<T>> key) {
        return makeRegistry(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
    }

    @SuppressWarnings("unchecked")
    private static <T, R extends WritableRegistry<T>> R makeRegistry(ResourceKey<Registry<T>> key, R registry) {
        BuiltInRegistriesAccessor.gtceu$getWritableRegistry().register((ResourceKey<WritableRegistry<?>>) (Object) key,
                registry, Lifecycle.stable());
        LOAD_ORDER.add(key.location());
        REGISTRIES.put((ResourceKey<Registry<?>>) (Object) key, registry);
        return registry;
    }

    @UnmodifiableView
    public static LinkedList<ResourceLocation> getRegistryOrder() {
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
    @SuppressWarnings({ "unchecked" })
    private static void actuallyRegister(RegisterEvent event) {
        if (!TO_REGISTER.containsRow(event.getVanillaRegistry())) return;

        for (var entry : TO_REGISTER.row(event.getVanillaRegistry()).entrySet()) {
            event.register((ResourceKey<? extends Registry<Object>>) event.getRegistryKey(), entry.getKey(),
                    entry::getValue);
        }
        TO_REGISTER.row(event.getVanillaRegistry()).clear();
    }

    private static void onUnfreeze(RegisterEvent event) {
        isFrozen = false;
    }

    private static void onFreeze(IdMappingEvent event) {
        isFrozen = event.isFrozen();
    }

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, GTRegistries::onUnfreeze);
        eventBus.addListener(GTRegistries::actuallyRegister);
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
