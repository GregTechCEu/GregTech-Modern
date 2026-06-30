package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.blocks.GTDevBlocks.CTM_TEST;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTDevMachines {

    public static final MultiblockMachineDefinition CONNECTED_TEXTURE_TEST = REGISTRATE
            .multiblock("connected_texture_test", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
            .appearanceBlock(CTM_TEST)
            .pattern(definition -> MultiblockPatternBuilder.start()
                    .slice("S        ", "         ", "XXXXXXXXX", "XXXX  XX ", "XXXXXXXXX")
                    .slice("         ", "         ", "         ", "         ", "         ")
                    .slice("         ", "         ", "         ", "         ", "         ")
                    .slice("         ", "         ", "         ", "         ", "         ")
                    .slice("         ", "         ", "      XXX", "      XXX", "      XXX")
                    .slice("         ", "         ", "      XXX", "      XXX", "      XXX")
                    .slice("         ", "         ", "     XXXX", "     XXXX", "     XXXX")
                    .slice("         ", "         ", "         ", "         ", "         ")
                    .slice("         ", "         ", "         ", "         ", "         ")
                    .slice("         ", "         ", "       XX", "       XX", "       XX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CTM_TEST)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, true)))
                    .where(' ', air())
                    .build())
            .workableCasingModel(GTCEu.id("block/ctm_test"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .register();

    public static void init() {}
}
