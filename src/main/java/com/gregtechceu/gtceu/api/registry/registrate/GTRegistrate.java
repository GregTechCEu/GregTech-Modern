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
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
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
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTRegistrate
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRegistrate extends Registrate {

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected GTRegistrate(String modId) {
        super(modId);
    }

    public static IGTFluidBuilder fluid(GTRegistrate parent, Material material, String name, String langKey,
                                        ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return parent.entry(name,
                callback -> new GTFluidBuilder<>(parent, parent, material, name, langKey, callback, stillTexture,
                        flowingTexture, GTFluidBuilder::defaultFluidType).defaultLang().defaultSource()
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop()));
    }

    @NotNull
    public static GTRegistrate create(String modId) {
        return new GTRegistrate(modId);
    }

    public void registerRegistrate(IEventBus modBus) {
        registerEventListeners(modBus);
    }

    public Registrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return super.registerEventListeners(bus);
        }
        return this;
    }

    protected <
            P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(P parent, String name,
                                                                                          Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast)
                            .map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)));
            config.accept(builder);
            return builder.build();
        });
    }

    public IGTFluidBuilder createFluid(String name, String langKey, Material material, ResourceLocation stillTexture,
                                       ResourceLocation flowingTexture) {
        return fluid(this, material, name, langKey, stillTexture, flowingTexture);
    }

    public <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION> machine(String name,
                                                                                     Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                     BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return MachineBuilder.create(this, name, definitionFactory, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MachineBuilder<MachineDefinition> machine(String name,
                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return MachineBuilder.create(this, name, MachineDefinition::createDefinition, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public Stream<MachineBuilder<MachineDefinition>> machine(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> metaMachine,
                                                             int... tiers) {
        return Arrays.stream(tiers)
                .mapToObj(tier -> MachineBuilder.create(this, name + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT),
                        MachineDefinition::createDefinition, holder -> metaMachine.apply(holder, tier),
                        MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity));
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                               BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                               TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return MultiblockMachineBuilder.createMulti(this, name, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return MultiblockMachineBuilder.createMulti(this, name, metaMachine, MetaMachineBlock::new,
                MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(GTCEu.id(name));
    }

    public SoundEntryBuilder sound(ResourceLocation name) {
        return new SoundEntryBuilder(name);
    }

    @Override
    public <T extends Item> @NotNull ItemBuilder<T, Registrate> item(String name,
                                                                     NonNullFunction<Item.Properties, T> factory) {
        return super.item(name, factory).lang(FormattingUtil.toEnglishName(name.replaceAll("\\.", "_")));
    }

    private RegistryEntry<CreativeModeTab, CreativeModeTab> currentTab;
    // This is using Collections#synchronizedMap because of https://github.com/Creators-of-Create/Create/issues/6222
    // TLDR: Forge's parallel mod loading will break the map if multiple mods insert into it at once during loading
    private static final Map<RegistryEntry<?, ?>, RegistryEntry<CreativeModeTab, CreativeModeTab>> TAB_LOOKUP = Collections
            .synchronizedMap(new IdentityHashMap<>());

    public void creativeModeTab(Supplier<RegistryEntry<CreativeModeTab, CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void creativeModeTab(RegistryEntry<CreativeModeTab, CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?, ?> entry, RegistryEntry<CreativeModeTab, CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?, ?> entry,
                               @Nullable RegistryEntry<CreativeModeTab, CreativeModeTab> tab) {
        TAB_LOOKUP.put(entry, tab);
    }

    protected <R,
            T extends R> RegistryEntry<R, T> accept(String name, ResourceKey<? extends Registry<R>> type,
                                                    Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
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
