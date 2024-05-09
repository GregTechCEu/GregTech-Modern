package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.function.TriFunction;

import org.jetbrains.annotations.NotNull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

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

    @NotNull
    public static GTRegistrate create(String modId) {
        return new GTRegistrate(modId);
    }

    public void registerRegistrate() {
        registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Override
    public Registrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return super.registerEventListeners(bus);
        }
        return this;
    }

    public IGTFluidBuilder createFluid(String name, String langKey, Material material, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(this, material, name, langKey, stillTexture, flowingTexture);
    }

    public static IGTFluidBuilder fluid(GTRegistrate parent, Material material, String name, String langKey, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return parent.entry(name, callback -> new GTFluidBuilder<>(parent, parent, material, name, langKey, callback, stillTexture, flowingTexture, GTFluidBuilder::defaultFluidType).defaultLang().defaultSource().setData(ProviderType.LANG, NonNullBiConsumer.noop()));
    }

    public <T extends Block> BlockBuilder<T, Registrate> block(String name, NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        return entry(name, callback -> GTBlockBuilder.create(this, self(), name, callback, factory, initialProperties));
    }

    public <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION> machine(String name,
                                                                                     Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                     BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return MachineBuilder.create(this, name, definitionFactory, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public MachineBuilder<MachineDefinition> machine(String name, Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return MachineBuilder.create(this, name, MachineDefinition::createDefinition, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public Stream<MachineBuilder<MachineDefinition>> machine(String name, BiFunction<IMachineBlockEntity, Integer, MetaMachine> metaMachine, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> MachineBuilder.create(this, name + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT), MachineDefinition::createDefinition, holder -> metaMachine.apply(holder, tier), MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity));
    }

    public MultiblockMachineBuilder multiblock(String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                               BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                               TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return MultiblockMachineBuilder.createMulti(this, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public MultiblockMachineBuilder multiblock(String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return MultiblockMachineBuilder.createMulti(this, name, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(GTCEu.id(name));
    }

    public SoundEntryBuilder sound(ResourceLocation name) {
        return new SoundEntryBuilder(name);
    }

    @Override
    public <T extends Item> @NotNull ItemBuilder<T, Registrate> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return super.item(name, factory).lang(FormattingUtil.toEnglishName(name.replaceAll("\\.", "_")));
    }

}
