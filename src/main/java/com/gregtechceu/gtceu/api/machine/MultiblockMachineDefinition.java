package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote MultiblockMachineDefinition
 */
public class MultiblockMachineDefinition extends MachineDefinition {

    @Getter
    @Setter
    private boolean generator;
    @Setter
    @Getter
    @NonNull
    private Supplier<BlockPattern> patternFactory;
    @Setter
    @Getter
    private Supplier<List<MultiblockShapeInfo>> shapes;
    /** Set this to false only if your multiblock is set up such that it could have a wall-shared controller. */
    @Getter
    @Setter
    private boolean allowFlip;
    @Getter
    @Setter
    private boolean renderXEIPreview;
    @Setter
    @Getter
    @Nullable
    private Supplier<ItemStack[]> recoveryItems;
    @Setter
    @Getter
    private Comparator<IMultiPart> partSorter;
    @Getter
    @Setter
    private TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay;

    protected MultiblockMachineDefinition(ResourceLocation id) {
        super(id);
    }

    public static MultiblockMachineDefinition createDefinition(ResourceLocation id) {
        return new MultiblockMachineDefinition(id);
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        var designs = shapes.get();
        if (!designs.isEmpty()) return designs;
        var structurePattern = patternFactory.get();
        int[][] aisleRepetitions = structurePattern.aisleRepetitions;
        return repetitionDFS(structurePattern, new ArrayList<>(), aisleRepetitions, new Stack<>());
    }

    private List<MultiblockShapeInfo> repetitionDFS(BlockPattern pattern, List<MultiblockShapeInfo> pages,
                                                    int[][] aisleRepetitions, Stack<Integer> repetitionStack) {
        if (repetitionStack.size() == aisleRepetitions.length) {
            int[] repetition = new int[repetitionStack.size()];
            for (int i = 0; i < repetitionStack.size(); i++) {
                repetition[i] = repetitionStack.get(i);
            }
            pages.add(new MultiblockShapeInfo(pattern.getPreview(repetition)));
        } else {
            for (int i = aisleRepetitions[repetitionStack.size()][0]; i <=
                    aisleRepetitions[repetitionStack.size()][1]; i++) {
                repetitionStack.push(i);
                repetitionDFS(pattern, pages, aisleRepetitions, repetitionStack);
                repetitionStack.pop();
            }
        }
        return pages;
    }
}
