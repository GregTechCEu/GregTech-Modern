package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.schema.ArraySchema;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;

import java.util.ArrayList;
import java.util.List;

public class MultiblockPreviewWidget extends ParentWidget<MultiblockPreviewWidget> {

    private final MultiblockMachineDefinition multiblockDefinition;

    private SchemaWidget multiSchema;

    public MultiblockPreviewWidget(MultiblockMachineDefinition definition) {
        multiblockDefinition = definition;
        coverChildren();
        child(new ListWidget<>()
                .coverChildren()
                .children(multiblockDefinition.getStructurePatterns().entrySet(), (e) -> {
                    List<Component> text = new ArrayList<>();
                    IBlockPattern pattern = e.getValue().get();

                    Flow menuRow = Flow.row();
                    if (pattern instanceof BlockPattern blockPattern) {
                        for (var predicate : blockPattern.getPredicates().values()) {
                            if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                                continue;
                            }
                            var menu = new ContextMenuButton<>(predicate.predicateList.get(0).getPredicateName())
                                    .size(18)
                                    .menuList(l -> l
                                            .children(predicate.predicateList, innerPred -> {
                                                return new ButtonWidget<>()
                                                        .size(16)
                                                        .overlay(new ItemDrawable(
                                                                innerPred.getCandidateStacks().get(0)));
                                            }));
                            menuRow.child(menu);
                            text.add(predicate.getCandidates().get(0).get(0).getItemStackForm().getHoverName());
                        }
                    }
                    return menuRow;
                }));

        setupSchema();
        if (multiSchema != null) {
            child(multiSchema.size(200, 160).pos(200, 200));
        }
    }

    private void setupSchema() {
        IBlockPattern mainPattern = multiblockDefinition.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        if (mainPattern instanceof BlockPattern blockPattern) {
            var dimensions = blockPattern.getDimensions();
            BlockState[][][] blocks = new BlockState[dimensions[0]][dimensions[1]][dimensions[2]];
            var mapping = blockPattern.getPredicates();
            var slices = blockPattern.getSlices();
            // todo use real relative directions from the definition
            for (int sliceIdx = 0; sliceIdx < dimensions[0]; sliceIdx++) {
                PatternSlice slice = slices[sliceIdx];
                for (int strIdx = 0; strIdx < dimensions[1]; strIdx++) {
                    for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                        PatternPredicate predicate = mapping.get(slice.charAt(strIdx, charIdx));
                        if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                            blocks[sliceIdx][strIdx][charIdx] = Blocks.AIR.defaultBlockState();
                            continue;
                        }

                        blocks[sliceIdx][strIdx][charIdx] = predicate.predicateList.get(0).candidates.get(0)
                                .getBlockState();

                    }
                }
            }

            ArraySchema array = new ArraySchema(blocks);
            // TODO: Fix this
            // child(new SchemaWidget.LayerButton(array, 0, dimensions[0]));

            multiSchema = new SchemaWidget(new SchemaRenderer(array));
        }

        // BoxSchema boxSchema = new BoxSchema();

        // multiSchema = new SchemaWidget()
    }
}
