package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.SimpleWorldGenLayer;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.builder.*;
import com.gregtechceu.gtceu.api.registry.registrate.builder.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.holder.HolderRegistryEntry;
import com.gregtechceu.gtceu.api.registry.registrate.holder.NoConfigHolderBuilder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
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
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRegistrate extends AbstractRegistrate<GTRegistrate> {

    private static final Map<String, GTRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected GTRegistrate(String modId) {
        super(modId);
    }

    public ResourceLocation makeResourceLocation(String path) {
        return new ResourceLocation(this.getModid(), path);
    }

    @Override
    public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryType) {
        return super.isRegistered(registryType);
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
            Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId)
                    .filter(FMLModContainer.class::isInstance)
                    .map(FMLModContainer.class::cast)
                    .map(FMLModContainer::getEventBus);
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
                registrate.registerEventListeners(modEventBus.orElse(FMLJavaModLoadingContext.get().getModEventBus()));
            }
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public GTRegistrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
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
        }
        return this;
    }

    // spotless:off
    /* === Builder helpers === */

    // Generic
    @Override
    public <R, T extends R> HolderRegistryEntry<T> simple(ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(currentName(), registryType, factory);
    }

    @Override
    public <R, T extends R> HolderRegistryEntry<T> simple(String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(this, name, registryType, factory);
    }

    @Override
    public <R, T extends R, P> HolderRegistryEntry<T> simple(P parent, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return simple(parent, currentName(), registryType, factory);
    }

    @Override
    public <R, T extends R, P> HolderRegistryEntry<T> simple(P parent, String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return generic(parent, name, registryType, factory).register();
    }

    @Override
    public <R, T extends R> NoConfigHolderBuilder<R, T, GTRegistrate> generic(ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return generic(self(), registryType, factory);
    }

    @Override
    public <R, T extends R> NoConfigHolderBuilder<R, T, GTRegistrate> generic(String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return generic(self(), name, registryType, factory);
    }

    @Override
    public <R, T extends R, P> NoConfigHolderBuilder<R, T, P> generic(P parent, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return generic(parent, currentName(), registryType, factory);
    }

    @Override
    public <R, T extends R, P> NoConfigHolderBuilder<R, T, P> generic(P parent, String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return (NoConfigHolderBuilder<R, T, P>) entry(name, callback -> new NoConfigHolderBuilder<>(this, parent, name, callback, registryType, factory));
    }

    // Machines

    public <M extends MetaMachine> SimpleMachineBuilder<GTRegistrate, M> machine(String name,
                                                                                 Function<ResourceLocation, MachineDefinition> definitionFactory,
                                                                                 BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                                                                 BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                 MachineInstanceFactory<M> instanceFactory) {
        return machine(this, name, definitionFactory, blockFactory, itemFactory, instanceFactory);
    }

    public <P, M extends MetaMachine> SimpleMachineBuilder<P, M> machine(P parent, String name,
                                                                         Function<ResourceLocation, MachineDefinition> definitionFactory,
                                                                         BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                                                         BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                         MachineInstanceFactory<M> instanceFactory) {
        return entry(name, callback -> new SimpleMachineBuilder<>(this, parent, name, callback, definitionFactory, blockFactory, itemFactory, instanceFactory));
    }

    public <M extends MetaMachine> SimpleMachineBuilder<GTRegistrate, M> machine(String name,
                                                                                 MachineInstanceFactory<M> instanceFactory) {
        return machine(this, name, instanceFactory);
    }

    public <P, M extends MetaMachine> SimpleMachineBuilder<P, M> machine(P parent, String name,
                                                                         MachineInstanceFactory<M> instanceFactory) {
        return entry(name, callback -> new SimpleMachineBuilder<>(this, parent, name, callback, MachineDefinition::new, MetaMachineBlock::new, MetaMachineItem::new, instanceFactory));
    }

    // Multiblock machines

    public <M extends MultiblockControllerMachine> MultiblockMachineBuilder<GTRegistrate, M> multiblock(String name,
                                                                                                        BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                                                        BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                                        MachineInstanceFactory<M> instanceFactory) {
        return multiblock(this, name, blockFactory, itemFactory, instanceFactory);
    }

    public <P, M extends MultiblockControllerMachine> MultiblockMachineBuilder<P, M> multiblock(P parent, String name,
                                                                                                BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                                                BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                                MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new MultiblockMachineBuilder<>(this, parent, name, callback, blockFactory, itemFactory, blockEntityFactory));
    }

    public <M extends MultiblockControllerMachine> MultiblockMachineBuilder<GTRegistrate, M> multiblock(String name, MachineInstanceFactory<M> instanceFactory) {
        return multiblock(this, name, instanceFactory);
    }

    public <P, M extends MultiblockControllerMachine> MultiblockMachineBuilder<P, M> multiblock(P parent, String name,
                                                                                                MachineInstanceFactory<M> blockEntityFactory) {
        return entry(name, callback -> new MultiblockMachineBuilder<>(this, parent, name, callback, MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory));
    }

    // Recipe types

    // TODO make a builder for this
    public HolderRegistryEntry<GTRecipeType> recipeType(String name, String group, RecipeType<?>... proxyRecipes) {
        return this.simple(name, GTRegistries.Keys.RECIPE_TYPE, () -> new GTRecipeType(makeResourceLocation(name), group, proxyRecipes));
    }

    // Recipe categories

    // TODO make a builder for this
    public HolderRegistryEntry<GTRecipeCategory> recipeCategory(String categoryName, GTRecipeType recipeType) {
        return this.simple(categoryName, GTRegistries.Keys.RECIPE_CATEGORY, () -> new GTRecipeCategory(categoryName, recipeType));
    }

    // Tag prefixes

    public TagPrefixBuilder<GTRegistrate> tagPrefix(String name) {
        return tagPrefix(this, name);
    }

    public <P> TagPrefixBuilder<P> tagPrefix(P parent, String name) {
        return entry(name, callback -> new TagPrefixBuilder<>(this, parent, name, callback));
    }

    public TagPrefixBuilder<GTRegistrate> oreTagPrefix(String name, TagKey<Block> miningToolTag) {
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

    public Material.Builder<GTRegistrate> material(String name) {
        return material(this, name);
    }

    public <P> Material.Builder<P> material(P parent, String name) {
        return entry(name, callback -> new Material.Builder<>(this, parent, name, callback));
    }

    // Elements

    public HolderRegistryEntry<Element> element(String name, long protons, long neutrons, long halfLifeSeconds,
                                                @Nullable String decayTo, String symbol, boolean isIsotope) {
        // TODO require names to be lowercase
        return this.simple(name.toLowerCase(Locale.ROOT), GTRegistries.Keys.ELEMENT, () -> new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope));
    }

    // Material icon sets

    public HolderRegistryEntry<MaterialIconSet> materialIconSet(String id) {
        return materialIconSet(id, MaterialIconSet.DULL);
    }

    public HolderRegistryEntry<MaterialIconSet> materialIconSet(String id, @Nullable Holder<MaterialIconSet> parent) {
        return this.simple(id, GTRegistries.Keys.MATERIAL_ICON_SET, () -> new MaterialIconSet(makeResourceLocation(id), parent != null ? parent.get() : null, parent == null));
    }

    // Medical conditions

    public HolderRegistryEntry<MedicalCondition> medicalCondition(String name, int color,
                                                                  int maxProgression, MedicalCondition.IdleProgressionType progressionType, float progressionRate,
                                                                  boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        return this.simple(name, GTRegistries.Keys.MEDICAL_CONDITION, () -> new MedicalCondition(makeResourceLocation(name),
                color, maxProgression, progressionType, progressionRate, canBePermanent, symptoms));
    }

    // Sounds

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(makeResourceLocation(name));
    }

    // World gen layers

    public HolderRegistryEntry<SimpleWorldGenLayer> simpleWorldGenLayer(String id, IWorldGenLayer.RuleTestSupplier target,
                                                                        Set<ResourceKey<Level>> levels) {
        return this.simple(id, GTRegistries.Keys.WORLD_GEN_LAYER, () -> new SimpleWorldGenLayer(makeResourceLocation(id), target, levels));
    }

    // Blocks
    @Override
    public <T extends Block> GTBlockBuilder<T, GTRegistrate> block(NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, factory);
    }

    @Override
    public <T extends Block> GTBlockBuilder<T, GTRegistrate> block(String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    @Override
    public <T extends Block, P> GTBlockBuilder<T, P> block(P parent, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(parent, currentName(), factory);
    }

    @Override
    public <T extends Block, P> GTBlockBuilder<T, P> block(P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (GTBlockBuilder<T, P>) entry(name, callback -> GTBlockBuilder.create(this, parent, name, callback, factory));
    }

    // Fluids
    public IGTFluidBuilder createFluid(String name, String langKey, Material material, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return entry(name, callback -> new GTFluidBuilder<>(this, this, material, name, langKey, callback, stillTexture, flowingTexture, GTFluidBuilder::defaultFluidType)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .defaultSource());
    }

    // Creative mode tabs

    private @Nullable RegistryEntry<CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?>, @Nullable RegistryEntry<CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    public @Nullable RegistryEntry<CreativeModeTab> creativeModeTab() {
        return this.currentTab;
    }

    public void creativeModeTab(Supplier<@Nullable RegistryEntry<CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void creativeModeTab(RegistryEntry<CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?> entry, RegistryEntry<CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?> entry, @Nullable RegistryEntry<CreativeModeTab> tab) {
        TAB_LOOKUP.put(entry, tab);
    }

    protected <R, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type,
                                                       Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
                                                       NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst()
                            .map(ItemEntry::cast)
                            .map(ItemEntry::asStack)
                            .orElse(new ItemStack(Items.AIR)));
            config.accept(builder);
            return builder.build();
        });
    }
    // spotless:on
}
