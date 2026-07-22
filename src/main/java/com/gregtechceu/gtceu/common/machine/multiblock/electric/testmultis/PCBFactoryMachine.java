package com.gregtechceu.gtceu.common.machine.multiblock.electric.testmultis;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_GRATE;

public class PCBFactoryMachine extends WorkableElectricMultiblockMachine {

    public PCBFactoryMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public IBlockPattern getDefaultStructurePattern() {
        return MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                .slice("CCC", "CCC")
                .slice("CCC", "CBC")
                .slice("CSC", "CCC")
                .where('C', /*
                             * Predicates.autoAbilities(true, false, false)
                             * .or(
                             */Predicates.blocks(CASING_GRATE.get()).setMinGlobalLimited(12))
                .where('S', Predicates.controller(Predicates.blocks(getDefinition().getBlock())))
                .where('B', Predicates.frames(GTMaterials.Steel))
                .build();
    }

    @Override
    public void createStructurePatterns() {
        super.createStructurePatterns();
        // patternStates.put("cooler", new PatternState());
        // structures.put("cooler",
        // MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
        // .slice("BBBBBBB", "BBBBBBB", "#######", "#######")
        // .slice("BBBBBBB", "B#####B", "#######", "#######")
        // .slice("BBBBBBB", "B#####B", "###B###", "##BBB##")
        // .slice("BBBBBBB", "B##B##B", "##BBB##", "##BCB##")
        // .slice("BBBBBBB", "B#####B", "###B###", "##BBB##")
        // .slice("BBBBBBB", "B#####B", "#######", "#######")
        // .slice("BBBBBBB", "BBBBBBB", "#######", "#######")
        // .where('#', PatternPredicate.AIR)
        // .where('B', Predicates.blocks(GTBlocks.CASING_COKE_BRICKS.get()))
        // .where('C', Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
        // .startOffset(OriginOffset.of(RelativeDirection.FRONT, 10))
        // .anchorOffset(OriginOffset.of(RelativeDirection.FRONT, 3).move(RelativeDirection.LEFT, 3))
        // .build());
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
    }

    /*
     * @Override
     * public void addDisplayText(List<Component> textList) {
     * super.addDisplayText(textList);
     * var coolerState = patternStates.get("cooler");
     * 
     * if (coolerState.isFormed()) {
     * textList.add(Component.literal("Has Substructure"));
     * } else if (coolerState.hasError()) {
     * textList.add(Component.literal("Has no Substructure"));
     * var c = coolerState.getError().getErrorInfo();
     * textList.addAll(c);
     * }
     * }
     */
}
