package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class GeneratorBuilder extends SimpleMachineBuilder {
    public GeneratorBuilder(ResourceLocation i, Object... args) {
        super(i, args);
    }

    @Override
    public MachineDefinition register() {
        value = GTMachines.registerSimpleGenerator(name, recipeType, tier -> tankScalingFunction.apply(tier), tiers)[0];
        return value;
    }
}
