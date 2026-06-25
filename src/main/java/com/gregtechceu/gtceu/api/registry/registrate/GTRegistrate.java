package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
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
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    // Machines

    public <DEFINITION extends MachineDefinition,
            MACHINE extends MetaMachine> MachineBuilder<DEFINITION, MACHINE, ?> machine(String name,
                                                                                        Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                        BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                                                                                        BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                        MachineInstanceFactory<MACHINE> blockEntityFactory) {
        return new MachineBuilder<>(this, name, definitionFactory,
                blockFactory, itemFactory, blockEntityFactory);
    }

    public <MACHINE extends MetaMachine> MachineBuilder<MachineDefinition, MACHINE, ?> machine(String name,
                                                                                               MachineInstanceFactory<MACHINE> blockEntityFactory) {
        return new MachineBuilder<>(this, name, MachineDefinition::new,
                MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory);
    }

    // Multiblock machines

    public <MACHINE extends MultiblockControllerMachine> MultiblockMachineBuilder<MultiblockMachineDefinition, MACHINE, ?> multiblock(String name,
                                                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                               BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                               MachineInstanceFactory<MACHINE> blockEntityFactory) {
        return new MultiblockMachineBuilder<>(this, name,
                blockFactory, itemFactory, blockEntityFactory);
    }

    public <MACHINE extends MultiblockControllerMachine> MultiblockMachineBuilder<MultiblockMachineDefinition, MACHINE, ?> multiblock(String name,
                                                                                                                                      MachineInstanceFactory<MACHINE> blockEntityFactory) {
        return new MultiblockMachineBuilder<>(this, name, MetaMachineBlock::new, MetaMachineItem::new,
                blockEntityFactory);
    }

    // Tag prefixes

    public TagPrefix tagPrefix(String name) {
        return tagPrefix(name, false);
    }

    public TagPrefix tagPrefix(String name, boolean invertedName) {
        var tagPrefix = new TagPrefix(makeResourceLocation(name), invertedName);
        this.generic(name.toLowerCase(), GTRegistries.Keys.TAG_PREFIX, () -> tagPrefix).register();
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

    // Elements

    public Element element(String name, long neutrons, long halfLifeSeconds, @Nullable String decayTo, long protons,
                           String symbol, boolean isIsotope) {
        var element = new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
        this.generic(name.toLowerCase(), GTRegistries.Keys.ELEMENT, () -> element).register();
        return element;
    }

    // Medical conditions

    public MedicalCondition medicalCondition(String name, int color,
                                             int maxProgression, MedicalCondition.IdleProgressionType progressionType, float progressionRate,
                                             boolean canBePermanent, Symptom.ConfiguredSymptom... symptoms) {
        var medicalCondition = new MedicalCondition(makeResourceLocation(name), color, maxProgression, progressionType, progressionRate, canBePermanent, symptoms);
        this.generic(name, GTRegistries.Keys.MEDICAL_CONDITION, () -> medicalCondition).register();
        return medicalCondition;
    }

    // Sounds

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(new ResourceLocation(getModid(), name));
    }

    // World gen layers

    public SimpleWorldGenLayer simpleWorldGenLayer(String id, IWorldGenLayer.RuleTestSupplier target,
                                                   Set<ResourceKey<Level>> levels) {
        var worldGenLayer = new SimpleWorldGenLayer(makeResourceLocation(id), target, levels);
        this.generic(id, GTRegistries.Keys.WORLD_GEN_LAYER, () -> worldGenLayer).build();
        return worldGenLayer;
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

    // Fluids
    public IGTFluidBuilder createFluid(String name, String langKey, Material material, ResourceLocation stillTexture,
                                       ResourceLocation flowingTexture) {
        return entry(name,
                callback -> new GTFluidBuilder<>(this, this, material, name, langKey, callback, stillTexture,
                        flowingTexture, GTFluidBuilder::defaultFluidType).defaultLang().defaultSource()
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop()));
    }

    // Creative mode tabs

    private @Nullable RegistryEntry<CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?>, RegistryEntry<CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

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

    protected <R,
            T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type,
                                                 Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
                                                 NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
        RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, String name,
                                                                                       Consumer<CreativeModeTab.Builder> config) {
        return createCreativeModeTab(parent, name, config);
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
}
