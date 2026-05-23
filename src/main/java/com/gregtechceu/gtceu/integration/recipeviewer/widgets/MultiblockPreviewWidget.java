package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import brachy.modularui.widgets.menu.Menu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternAisle;
import com.gregtechceu.gtceu.api.multiblock.predicates.*;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.schema.ArraySchema;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SchemaWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                                                        .overlay(new ItemDrawable(innerPred.getCandidates().get(0)));
                                            }));
                            menuRow.child(menu);
                            text.add(predicate.getCandidates().get(0).get(0).getHoverName());
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
            var aisles = blockPattern.getAisles();
            // todo use real relative directions from the definition
            for (int aisleIdx = 0; aisleIdx < dimensions[0]; aisleIdx++) {
                PatternAisle aisle = aisles[aisleIdx];
                for (int strIdx = 0; strIdx < dimensions[1]; strIdx++) {
                    for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                        PatternPredicate predicate = mapping.get(aisle.charAt(strIdx, charIdx));
                        if (predicate.equals(PatternPredicate.ANY) || predicate.equals(PatternPredicate.AIR)) {
                            blocks[aisleIdx][strIdx][charIdx] = Blocks.AIR.defaultBlockState();
                        } else {
                            blocks[aisleIdx][strIdx][charIdx] = getCandidates(predicate.predicateList.get(0))[0];
                        }
                    }
                }
            }

            ArraySchema array = new ArraySchema(blocks);
            child(new SchemaWidget.LayerButton(array, 0, dimensions[0]));

            multiSchema = new SchemaWidget(new SchemaRenderer(array));
        }

        // BoxSchema boxSchema = new BoxSchema();

        // multiSchema = new SchemaWidget()
    }

    private BlockState[] getCandidates(BasePredicate predicate) {
        if (predicate instanceof PredicateStates states) {
            return states.states;
        } else if (predicate instanceof PredicateBlocks blocks) {
            return Arrays.stream(blocks.blocks).map(Block::defaultBlockState).toArray(BlockState[]::new);
        } else if (predicate instanceof PredicateFluids fluids) {
            return Arrays.stream(fluids.fluids).map(f -> f.defaultFluidState().createLegacyBlock())
                    .toArray(BlockState[]::new);
        } else if (predicate instanceof PredicateFluidTag fluidTag) {
            // return Arrays.stream(fluidTag.tag).map(f ->
            // f.defaultFluidState().createLegacyBlock()).toArray(BlockState[]::new);
        }
        return Arrays.stream(predicate.candidates.apply(new CompoundTag())).map(BlockInfo::getBlockState)
                .toArray(BlockState[]::new);
    }
}
