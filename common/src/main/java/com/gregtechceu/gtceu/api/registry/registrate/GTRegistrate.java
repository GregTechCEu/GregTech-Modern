package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTRegistrate
 */
public abstract class GTRegistrate extends Registrate {

    protected GTRegistrate(String modId) {
        super(modId);
    }

    @Nonnull
    @ExpectPlatform
    public static GTRegistrate create(String modId) {
        throw new AssertionError();
    }

    public abstract void registerRegistrate();

    public IGTFluidBuilder createFluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return fluid(this, name, stillTexture, flowingTexture);
    }

    @ExpectPlatform
    public static IGTFluidBuilder fluid(GTRegistrate parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        throw new AssertionError();
    }

    public MachineBuilder machine(String name, Function<IMetaMachineBlockEntity, MetaMachine> metaMachine) {
        return MachineBuilder.create(this, name, metaMachine);
    }

    public Stream<MachineBuilder> machine(String name, BiFunction<IMetaMachineBlockEntity, Integer, MetaMachine> metaMachine, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> MachineBuilder.create(this, name + "." + GTValues.VN[tier].toLowerCase(), holder -> metaMachine.apply(holder, tier)));
    }

    public MultiblockMachineBuilder multiblock(String name, Function<IMetaMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return MultiblockMachineBuilder.createMulti(this, name, metaMachine);
    }

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(GTCEu.id(name));
    }

    @Override
    public <T extends Item> @Nonnull ItemBuilder<T, Registrate> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return super.item(name, factory).lang(FormattingUtil.toEnglishName(name.replaceAll("/.", "_")));
    }

}
