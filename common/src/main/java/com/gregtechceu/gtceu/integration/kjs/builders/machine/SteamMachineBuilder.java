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
public class SteamMachineBuilder extends MachineBuilder {
    public SteamMachineBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public Block createObject() {
        var machinePair = GTMachines.registerSimpleSteamMachines(name, recipeType);
        return null;
    }
}
