package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTCreateMachines;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import dev.latvian.mods.kubejs.event.EventJS;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.Pair;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote MachineEventJS
 */
public class MachineEventJS extends EventJS {

    public Pair<MachineDefinition, MachineDefinition> simpleSteamMachines(String name, GTRecipeType recipeType) {
        return GTMachines.registerSimpleSteamMachines(name, recipeType);
    }

    public static MachineDefinition[] simpleGenerator(String name, GTRecipeType recipeType, Function<Object, Double> tankScalingFunction, int... tiers) {
        return GTMachines.registerSimpleGenerator(name, recipeType, tier -> tankScalingFunction.apply(tier).longValue(), tiers);
    }

    public static MachineDefinition[] simpleMachines(String name, GTRecipeType recipeType, Function<Object, Double> tankScalingFunction, int... tiers) {
        return GTMachines.registerSimpleMachines(name, recipeType, tier -> tankScalingFunction.apply(tier).longValue(), tiers);
    }

    public static MachineDefinition[] simpleKineticMachines(String name, GTRecipeType recipeType, int... tiers) {
        return GTCreateMachines.registerSimpleKineticElectricMachine(name, recipeType, tiers);
    }

    public static MultiblockMachineBuilder simpleMultiblock(String name) {
        return GTRegistries.REGISTRATE.multiblock(name, WorkableElectricMultiblockMachine::new);
    }

    public void post() {
        GTCEuStartupEvents.MACHINES.post(this);
    }
}
