package com.lowdragmc.gtceu.api.registry.registrate;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.GTValues;
import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.MetaMachine;
import com.lowdragmc.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.tterrag.registrate.Registrate;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;

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

}
