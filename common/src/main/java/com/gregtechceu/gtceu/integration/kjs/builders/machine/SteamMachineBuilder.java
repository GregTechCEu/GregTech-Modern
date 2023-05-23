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
    private MachineDefinition value;

    public SteamMachineBuilder(ResourceLocation i, Object... args) {
        super(i);
    }

    @Override
    public MachineDefinition register() {
        var pair = GTMachines.registerSimpleSteamMachines(name, recipeType);
        value = pair.first();
        return value;
    }

    @Override
    public MachineDefinition get() {
        return value;
    }
}
