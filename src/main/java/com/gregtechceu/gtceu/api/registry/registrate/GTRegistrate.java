package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class GTRegistrate extends AbstractRegistrate<GTRegistrate> {

    private static final Map<String, GTRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected GTRegistrate(String modId) {
        super(modId);
    }

    /**
     * Get or create a new {@link GTRegistrate} and register event listeners for registration and data generation.
     * A new {@code GTRegistrate} instance is only made if one doesn't already exist in the cache.
     *
     * @param modId The mod ID for which objects will be registered
     * @return The {@link GTRegistrate} instance
     */
    public static GTRegistrate create(String modId) {
        return innerCreate(modId, true);
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
        return innerCreate(modId, false);
    }

    private static GTRegistrate innerCreate(String modId, boolean strict) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new GTRegistrate(modId);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
        if (strict) {
            modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                String message = "# [GTRegistrate] Failed to register eventListeners for mod " + modId +
                        ", This should be reported to this mod's dev #";
                String hashtags = "#".repeat(message.length());
                GTCEu.LOGGER.fatal(hashtags);
                GTCEu.LOGGER.fatal(message);
                GTCEu.LOGGER.fatal(hashtags);
            });
        } else {
            registrate.registerEventListeners(modEventBus.orElse(GTCEu.gtModBus));
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public GTRegistrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return super.registerEventListeners(bus);
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

    public <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION> machine(String name,
                                                                                     Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                     BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MachineBuilder<>(this, name, definitionFactory, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MachineBuilder<MachineDefinition> machine(String name,
                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return new MachineBuilder<>(this, name, MachineDefinition::createDefinition, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public Stream<MachineBuilder<MachineDefinition>> machine(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> metaMachine,
                                                             int... tiers) {
        return Arrays.stream(tiers)
                .mapToObj(tier -> new MachineBuilder<>(this, name + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT),
                        MachineDefinition::createDefinition, holder -> metaMachine.apply(holder, tier),
                        MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity));
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                               BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                               TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MultiblockMachineBuilder(this, name, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return new MultiblockMachineBuilder(this, name, metaMachine, MetaMachineBlock::new,
                MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(GTCEu.id(name));
    }

    public SoundEntryBuilder sound(ResourceLocation name) {
        return new SoundEntryBuilder(name);
    }

    @Override
    public <T extends Item> @NotNull ItemBuilder<T, GTRegistrate> item(String name,
                                                                       NonNullFunction<Item.Properties, T> factory) {
        return super.item(name, factory).lang(FormattingUtil.toEnglishName(name.replaceAll("\\.", "_")));
    }

    @Nullable
    private RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?, ?>, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    public void creativeModeTab(@Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab) {
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
