package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class GeneratorBuilder extends SimpleMachineBuilder {
    public GeneratorBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public MachineDefinition createObject() {
        return GTMachines.registerSimpleGenerator(name, recipeType, tier -> tankScalingFunction.apply(tier), tiers)[0];
    }
}
