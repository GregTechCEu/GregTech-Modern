package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MultiblockInfoReiCategory extends
                                       ModularUIREIDisplayCategory<MultiblockInfoReiCategory.MultiblockInfoDisplay> {

    public static final CategoryIdentifier<MultiblockInfoDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("multiblock_info"));
    private final Renderer icon;

    public MultiblockInfoReiCategory() {
        this.icon = EntryStacks.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem());
    }

    public static void registerDisplays(DisplayRegistry registry) {
        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoDisplay::new)
                .forEach(registry::add);
    }

    @Override
    public int getDisplayHeight() {
        return 160 + 8;
    }

    @Override
    public int getDisplayWidth(MultiblockInfoDisplay display) {
        return 160 + 8;
    }

    @Override
    public CategoryIdentifier<? extends MultiblockInfoDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    public static class MultiblockInfoDisplay extends ModularUIREIDisplay {

        private final MultiblockMachineDefinition definition;
        private final List<EntryIngredient> containedBlocks = new ArrayList<>();

        public MultiblockInfoDisplay(MultiblockMachineDefinition definition) {
            super(definition.getId(), () -> new MultiblockPreviewWidget(definition, null, 200, 180), CATEGORY);
            this.definition = definition;
            initializeContainedBlocks();
        }

        private void initializeContainedBlocks() {
            Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
            IBlockPattern pattern = definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();
            AbstractStructureHelper structureHelper = null;
            if (pattern instanceof BlockPattern blockPattern) {
                var sliceRepeats = new Int2IntArrayMap();
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    sliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
                structureHelper = AbstractStructureHelper.blockPattern(sliceRepeats);
            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                var userDimensions = new IntArrayList();
                expandablePattern.getBoundsConstraints().apply().stream()
                        .mapToInt(Pair::left)
                        .forEach(userDimensions::add);
                structureHelper = AbstractStructureHelper.expandable(userDimensions);
            }
            if (structureHelper != null) {
                structureHelper.populate(resultStructure, pattern, null,
                        definition.getRotationState().defaultDirection, switch (definition.getRotationState()) {
                            case Y_AXIS -> Direction.NORTH;
                            case ALL, NON_Y_AXIS, NONE -> Direction.UP;
                        }, false);

                Object2IntMap<Block> blockCount = new Object2IntOpenHashMap<>();
                resultStructure.forEach(
                        (pos, state) -> blockCount.mergeInt(state.getBlockState().getBlock(), 1, Integer::sum));

                blockCount.forEach((block, count) -> containedBlocks.add(EntryIngredients.of(block.asItem(), count)));
            }
        }

        @Override
        public List<EntryIngredient> getInputEntries() {
            return containedBlocks;
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return List.of(EntryIngredients.of(definition.getBlock()));
        }
    }
}
