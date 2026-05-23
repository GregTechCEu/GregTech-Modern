package com.gregtechceu.gtceu.common.machine.mui;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternAisle;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.schema.ArraySchema;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;

import java.util.*;

public class TestMuiMachine2 extends MetaMachine implements IMuiMachine {

    private final MultiblockMachineDefinition multiblockDefinition;

    private SchemaWidget multiSchema;
    private Map<PatternPredicate, BlockInfo> predicateSetting = new HashMap<>();
    // todo store

    public TestMuiMachine2(BlockEntityCreationInfo info) {
        super(info);
        multiblockDefinition = GTMultiMachines.DISTILLATION_TOWER;

        for (var pattern : multiblockDefinition.getStructurePatterns().values()) {
            if (pattern instanceof BlockPattern blockPattern) {
                for (var predicate : blockPattern.getPredicates().values()) {
                    if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                        continue;
                    }
                    predicateSetting.put(predicate, predicate.predicateList.get(0).getCandidates().get(0));
                }
            }
        }
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel<?> panel = new ModularPanel<>("test_tile2")
                // .size(200, 200)
                .coverChildrenWidth();
        panel
                .background(GuiTextures.MC_BACKGROUND);

        Flow col = Flow.col().coverChildren();
        if (!predicateSetting.isEmpty()) {
            setupSchema(col);
        }
        col.child(new ListWidget<>()
                .name("structurePatterns")
                .coverChildren()
                .children(multiblockDefinition.getStructurePatterns().entrySet(), (e) -> {
                    List<Component> text = new ArrayList<>();
                    IBlockPattern pattern = e.getValue().get();

                    Flow predicatesRow = Flow.row()
                            .name("predicates")
                            .height(20)
                            .coverChildrenWidth();
                    if (pattern instanceof BlockPattern blockPattern) {
                        for (var predicate : blockPattern.getPredicates().values()) {
                            // todo figure out sliders needed for predicate min/max depending on base predicates in the
                            // main predicate
                            if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                                continue;
                            }
                            predicateSetting.put(predicate, predicate.predicateList.get(0).getCandidates().get(0));
                            for (var innerPred : predicate.predicateList) {

                            }
                            var menu = new ContextMenuButton<>(predicate.predicateList.get(0).getPredicateName())
                                    .size(20)
                                    .requiresClick()
                                    .menuList(l -> l
                                            .maxSize(80)
                                            .coverChildrenWidth()
                                            .childSeparator(Icon.EMPTY_2PX)
                                            .children(predicate.predicateList, innerPred -> {
                                                List<BlockInfo> candidates = innerPred.candidates;
                                                if (candidates == null || candidates.isEmpty())
                                                    return new EmptyWidget();
                                                if (candidates.size() > 1) {
                                                    return new ContextMenuButton<>(innerPred.getPredicateName())
                                                            .size(16)
                                                            .tooltip(r -> r.add(innerPred.getPredicateName()))
                                                            .overlay(new ItemDrawable(
                                                                    candidates.get(0).getItemStackForm()))
                                                            .requiresClick()
                                                            .openRightDown()
                                                            .menuList(l1 -> l1
                                                                    .maxSize(80)
                                                                    .coverChildrenWidth()
                                                                    .childSeparator(Icon.EMPTY_2PX)
                                                                    .children(candidates, blockInfo -> {
                                                                        Component stackName = blockInfo
                                                                                .getItemStackForm().getHoverName();
                                                                        return new ToggleButton()
                                                                                .value(new BoolValue.Dynamic(
                                                                                        () -> false,
                                                                                        (b) -> predicateSetting.put(
                                                                                                predicate, blockInfo)))
                                                                                .size(16)
                                                                                .tooltip(r -> r.add(stackName))
                                                                                .overlay(new ItemDrawable(
                                                                                        blockInfo.getItemStackForm()));
                                                                    }));

                                                } else {
                                                    return new ToggleButton()
                                                            .value(new BoolValue.Dynamic(() -> false,
                                                                    (b) -> predicateSetting.put(predicate,
                                                                            candidates.get(0))))
                                                            .size(16)
                                                            .tooltip(r -> r.add(innerPred.getPredicateName()))
                                                            .overlay(new ItemDrawable(
                                                                    candidates.get(0).getItemStackForm()));
                                                }
                                            }));
                            predicatesRow.child(menu);
                            text.add(predicate.getCandidates().get(0).get(0).getHoverName());
                        }
                    }
                    return predicatesRow;
                }));

        panel.child(col);

        return panel;
    }

    private void setupSchema(Flow col) {
        IBlockPattern mainPattern = multiblockDefinition.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        if (mainPattern instanceof BlockPattern blockPattern) {
            var dimensions = blockPattern.getDimensions();
            // todo move to long2blockpos map for boxschema so that centering is correct

            BlockState[][][] blocks = new BlockState[dimensions[0]][dimensions[1]][dimensions[2]];
            // Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();
            var mapping = blockPattern.getPredicates();
            var aisles = blockPattern.getAisles();
            // todo use real relative directions from the definition
            RelativeDirection[] directions = blockPattern.getDirections();
            Direction absoluteAisle = directions[0].getRelativeFacing(Direction.NORTH, Direction.UP, false);
            Direction absoluteString = directions[1].getRelativeFacing(Direction.NORTH, Direction.UP, false);
            Direction absoluteChar = directions[2].getRelativeFacing(Direction.NORTH, Direction.UP, false);

            for (int aisleIdx = 0; aisleIdx < dimensions[0]; aisleIdx++) {
                PatternAisle aisle = aisles[aisleIdx];
                for (int strIdx = 0; strIdx < dimensions[1]; strIdx++) {
                    for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                        PatternPredicate predicate = mapping.get(aisle.charAt(strIdx, charIdx));
                        if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                            blocks[aisleIdx][strIdx][charIdx] = Blocks.AIR.defaultBlockState();
                            continue;
                        }
                        blocks[aisleIdx][strIdx][charIdx] = predicateSetting.get(predicate).getBlockState();
                    }
                }
            }

            ArraySchema array = new ArraySchema(blocks);
            if (getLevel().isClientSide()) {
                multiSchema = new SchemaWidget(array);
                col.child(multiSchema.size(100, 100));
                col.child(new SchemaWidget.LayerButton(array, 0, dimensions[0]));
            }
        }
    }
}
