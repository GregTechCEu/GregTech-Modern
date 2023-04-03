package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class SimpleMachineBuilder extends MachineBuilder {
    public transient Int2LongFunction tankScalingFunction;
    public transient int[] tiers;

    public SimpleMachineBuilder(ResourceLocation i) {
        super(i);
        this.tankScalingFunction = GTMachines.defaultTankSizeFunction;
        this.tiers = GTMachines.ELECTRIC_TIERS;
    }

    public SimpleMachineBuilder tiers(int... tiers) {
        this.tiers = tiers;
        return this;
    }

    public SimpleMachineBuilder tankScalingFunction(Function<Object, Double> tankScalingFunction) {
        this.tankScalingFunction = tier -> tankScalingFunction.apply(tier).longValue();
        return this;
    }

    @Override
    public MachineDefinition createObject() {
        return GTMachines.registerSimpleMachines(name, recipeType, tier -> tankScalingFunction.apply(tier), tiers)[0];
    }
}
