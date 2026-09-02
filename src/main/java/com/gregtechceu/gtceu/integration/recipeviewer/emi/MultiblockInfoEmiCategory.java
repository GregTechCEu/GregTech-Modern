package com.gregtechceu.gtceu.integration.recipeviewer.emi;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MultiblockInfoEmiCategory extends EmiRecipeCategory {

    public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

    private MultiblockInfoEmiCategory() {
        super(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoEmiWrapper::new)
                .forEach(registry::addRecipe);
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    public static class MultiblockInfoEmiWrapper extends ModularUIEmiRecipe {

        private final MultiblockMachineDefinition definition;
        private final List<EmiIngredient> containedBlocks = new ArrayList<>();

        public MultiblockInfoEmiWrapper(MultiblockMachineDefinition definition) {
            super(definition.getId(), () -> new MultiblockPreviewWidget(definition, null, 200, 180));
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
                blockCount.forEach((block, count) -> containedBlocks.add(EmiStack.of(block.asItem(), count)));
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            super.addWidgets(widgets);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return definition.getId().withPrefix("/multi_info/");
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return containedBlocks;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(EmiStack.of(definition.getBlock()));
        }
    }
}
