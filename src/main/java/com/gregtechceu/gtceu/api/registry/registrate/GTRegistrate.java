package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;

public class GTRegistrate extends AbstractRegistrate<GTRegistrate> {

    private static final Map<String, GTRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected GTRegistrate(String modId) {
        super(modId);
    }

    public ResourceLocation makeResourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(this.getModid(), path);
    }

    /**
     * Get or create a new {@link GTRegistrate} and register event listeners for registration and data generation.
     * A new {@code GTRegistrate} instance is only made if one doesn't already exist in the cache.
     *
     * @param modId The mod ID for which objects will be registered
     * @return The {@link GTRegistrate} instance
     */
    public static GTRegistrate create(String modId) {
        return create(modId, true);
    }

    /**
     * Get or create a new {@link GTRegistrate} and conditionally register event listeners.
     * A new {@code GTRegistrate} instance is only made if one doesn't already exist in the cache.
     * <br>
     * Note that if you do not allow event listeners to be registered automatically, you <strong>must</strong>
     * call {@link #registerEventListeners(IEventBus)} yourself with your {@link IEventBus mod event bus}.
     *
     * @param modId          The mod ID for which objects will be registered
     * @param registerEvents Whether to register required event listeners.
     * @return The {@link GTRegistrate} instance
     */
    public static GTRegistrate create(String modId, boolean registerEvents) {
        return innerCreate(modId, registerEvents, registerEvents);
    }

    /**
     * Get or create a new {@link GTRegistrate} and register event listeners for registration and data generation.
     * A new {@code GTRegistrate} instance is only made if one doesn't already exist in the cache.
     * <br>
     * Completely skips all mod id validity messages and defaults to GT's bus instead. <b>ADDON DEVS DO NOT USE.</b>
     *
     * @param modId The mod ID for which objects will be registered
     * @return The {@link GTRegistrate} instance
     */
    @ApiStatus.Internal
    public static GTRegistrate createIgnoringListenerErrors(String modId) {
        return innerCreate(modId, true, false);
    }

    private static GTRegistrate innerCreate(String modId, boolean registerEvents, boolean requireValidEventBus) {
        var existing = EXISTING_REGISTRATES.get(modId);
        if (existing != null) return existing;
        var registrate = new GTRegistrate(modId);

        if (registerEvents) {
            Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
            if (requireValidEventBus) {
                modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                    String message = "# [GTRegistrate] Failed to register eventListeners for mod " + modId +
                            ", This should be reported to this mod's dev #";
                    String hashtags = "#".repeat(message.length());
                    GTCEu.LOGGER.fatal(hashtags);
                    GTCEu.LOGGER.fatal(message);
                    GTCEu.LOGGER.fatal(hashtags);
                });
            } else {
                registrate.registerEventListeners(Objects.requireNonNull(
                        modEventBus.orElse(ModLoadingContext.get().getActiveContainer().getEventBus())));
            }
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public GTRegistrate registerEventListeners(IEventBus bus) {
        if (registered.getAndSet(true)) {
            // early exit if event listeners are already registered
            return this;
        }
        if (this.getModEventBus() == null) {
            this.setModEventBus(bus);
        }
        // recreate the super method so we can register the event listener with LOW priority.
        Consumer<RegisterEvent> onRegister = this::onRegister;
        Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
        bus.addListener(EventPriority.LOW, onRegister);
        bus.addListener(EventPriority.LOWEST, onRegisterLate);

        // Fired multiple times when ever tabs need contents rebuilt (changing op tab perms for example)
        bus.addListener(this::onBuildCreativeModeTabContents);
        // Register events fire multiple times, so clean them up on common setup
        OneTimeEventReceiver.addModListener(this, FMLCommonSetupEvent.class, $ -> {
            OneTimeEventReceiver.unregister(this, onRegister, RegisterEvent.class);
            OneTimeEventReceiver.unregister(this, onRegisterLate, RegisterEvent.class);
        });
        if (((AbstractRegistrateAccessor) this).getDoDatagen().get()) {
            OneTimeEventReceiver.addModListener(this, GatherDataEvent.class, this::onData);
        }
        return this;
    }

    protected <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(P parent, String name,
                                                                                             Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast)
                            .map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)));
            config.accept(builder);
            return builder.build();
        });
    }

    // Machines

    public <M extends MetaMachine> SingleblockMachineBuilder<GTRegistrate, M> machine(String name,
                                                                                      Function<ResourceLocation, MachineDefinition> definitionFactory,
                                                                                      BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                                                                      BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                      MachineInstanceFactory<M> blockEntityFactory) {
        return machine(this, name, definitionFactory, blockFactory, itemFactory, blockEntityFactory);
    }

    public <P, M extends MetaMachine> SingleblockMachineBuilder<P, M> machine(P parent, String name,
                                                                              Function<ResourceLocation, MachineDefinition> definitionFactory,
                                                                              BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                                                              BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                              MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new SingleblockMachineBuilder<>(this, parent, name, callback, definitionFactory, blockFactory, itemFactory, blockEntityFactory));
    }

    public <M extends MetaMachine> SingleblockMachineBuilder<GTRegistrate, M> machine(String name, MachineInstanceFactory<M> blockEntityFactory) {
        return machine(this, name, blockEntityFactory);
    }

    public <P, M extends MetaMachine> SingleblockMachineBuilder<P, M> machine(P parent, String name,
                                                                              MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new SingleblockMachineBuilder<>(this, parent, name, callback, MachineDefinition::new, MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory));
    }

    // Multiblock machines

    public <M extends MultiblockControllerMachine> MultiblockMachineBuilder<GTRegistrate, M> multiblock(String name,
                                                                                                        BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                                                        BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                                        MachineInstanceFactory<M> blockEntityFactory) {
        return multiblock(this, name, blockFactory, itemFactory, blockEntityFactory);
    }

    public <P, M extends MultiblockControllerMachine> MultiblockMachineBuilder<P, M> multiblock(P parent, String name,
                                                                                                BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                                                BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                                MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new MultiblockMachineBuilder<>(this, parent, name, callback, blockFactory, itemFactory, blockEntityFactory));
    }

    public <M extends MultiblockControllerMachine> MultiblockMachineBuilder<GTRegistrate, M> multiblock(String name,
                                                                                                        MachineInstanceFactory<M> blockEntityFactory) {
        return multiblock(this, name, blockEntityFactory);
    }

    public <P, M extends MultiblockControllerMachine> MultiblockMachineBuilder<P, M> multiblock(P parent, String name,
                                                                                                MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new MultiblockMachineBuilder<>(this, parent, name, callback, MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory));
    }

    // Recipe types

    public GTRecipeType recipeType(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTRecipeType(makeResourceLocation(name), group, proxyRecipes);
        this.generic(name, Registries.RECIPE_TYPE, () -> recipeType).build();
        recipeType.setSerializer(this.generic(name, Registries.RECIPE_SERIALIZER, GTRecipeSerializer::new).register());

        return recipeType;
    }

    // Recipe categories

    public GTRecipeCategory recipeCategory(String categoryName, GTRecipeType recipeType) {
        var category = new GTRecipeCategory(makeResourceLocation(categoryName), recipeType);
        this.generic(categoryName, GTRegistries.Keys.RECIPE_CATEGORY, () -> category).build();
        return category;
    }

    // Tag prefixes

    public TagPrefix tagPrefix(String name) {
        return tagPrefix(name, false);
    }

    public TagPrefix tagPrefix(String name, boolean invertedName) {
        var tagPrefix = new TagPrefix(makeResourceLocation(name), invertedName);
        this.generic(name, GTRegistries.Keys.TAG_PREFIX, () -> tagPrefix).register();
        return tagPrefix;
    }

    public TagPrefix oreTagPrefix(String name, TagKey<Block> miningToolTag) {
        return tagPrefix(name)
                .defaultTagPath("ores/%s")
                .prefixOnlyTagPath("ores_in_ground/%s")
                .unformattedTagPath("ores")
                .materialIconType(MaterialIconType.ore)
                .miningToolTag(miningToolTag)
                .unificationEnabled(true)
                .blockConstructor(OreBlock::new)
                .generationCondition(hasOreProperty);
    }

    // Materials

    public Material.Builder material(String name) {
        return new Material.Builder(this, makeResourceLocation(name));
    }

    // Elements

    public Element element(String name, long protons, long neutrons, long halfLifeSeconds, @Nullable String decayTo,
                           String symbol, boolean isIsotope) {
        var element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        this.generic(name.toLowerCase(), GTRegistries.Keys.ELEMENT, () -> element).register();
        return element;
    }

    public Element element(long protons, long neutrons, long halfLifeSeconds, String decayTo, String name,
                           String symbol,
                           boolean isIsotope) {
        return element(name, protons, neutrons, halfLifeSeconds, decayTo, symbol, isIsotope);
    }

    // Material icon sets

    public MaterialIconSet materialIconSet(String id) {
        return materialIconSet(id, MaterialIconSet.DULL);
    }

    public MaterialIconSet materialIconSet(String id, MaterialIconSet parent) {
        return materialIconSet(id, parent, false);
    }

    public MaterialIconSet materialIconSet(String id, @Nullable MaterialIconSet parent, boolean isRoot) {
        var iconSet = new MaterialIconSet(makeResourceLocation(id), parent, isRoot);
        this.generic(id, GTRegistries.Keys.MATERIAL_ICON_SET, () -> iconSet).build();
        return iconSet;
    }

    // Medical conditions

    public MedicalCondition medicalCondition(String name, int color,
                                             int maxProgression, MedicalCondition.IdleProgressionType progressionType,
                                             float progressionRate,
                                             boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        var medicalCondition = new MedicalCondition(makeResourceLocation(name), color, maxProgression, progressionType,
                progressionRate, canBePermanent, symptoms);
        this.generic(name, GTRegistries.Keys.MEDICAL_CONDITION, () -> medicalCondition).register();
        return medicalCondition;
    }

    // Sounds

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(makeResourceLocation(name));
    }

    // World gen layers

    public SimpleWorldGenLayer simpleWorldGenLayer(String id, IWorldGenLayer.RuleTestSupplier target,
                                                   Set<ResourceKey<Level>> levels) {
        var worldGenLayer = new SimpleWorldGenLayer(makeResourceLocation(id), target, levels);
        this.generic(id, GTRegistries.Keys.WORLD_GEN_LAYER, () -> worldGenLayer).build();
        return worldGenLayer;
    }

    // Blocks
    @Override
    public <T extends Block> GTBlockBuilder<T, GTRegistrate> block(NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, factory);
    }

    @Override
    public <T extends Block> GTBlockBuilder<T, GTRegistrate> block(String name,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    @Override
    public <T extends Block, P> GTBlockBuilder<T, P> block(P parent,
                                                           NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(parent, currentName(), factory);
    }

    @Override
    public <T extends Block, P> GTBlockBuilder<T, P> block(P parent, String name,
                                                           NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (GTBlockBuilder<T, P>) entry(name,
                callback -> GTBlockBuilder.create(this, parent, name, callback, factory));
    }

    private @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?, ?>, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    public @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> creativeModeTab() {
        return this.currentTab;
    }

    public void creativeModeTab(Supplier<@Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void resetCreativeModeTab() {
        this.currentTab = null;
    }

    public void creativeModeTab(RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?, ?> entry,
                                   RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?, ?> entry,
                               @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        TAB_LOOKUP.put(entry, tab);
    }

    protected <R, T extends R> RegistryEntry<R, T> accept(String name, ResourceKey<? extends Registry<R>> type,
                                                          Builder<R, T, ?, ?> builder,
                                                          NonNullSupplier<? extends T> creator,
                                                          NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, String name,
                                                                                       Consumer<CreativeModeTab.Builder> config) {
        return createCreativeModeTab(parent, name, config);
    }
}
