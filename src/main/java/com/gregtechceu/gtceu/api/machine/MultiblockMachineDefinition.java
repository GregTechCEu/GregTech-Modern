package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;

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
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiblockMachineDefinition extends MachineDefinition {

    @Getter
    @Setter
    private boolean generator;
    @Getter
    @NonNull
    private Map<String, Supplier<IBlockPattern>> structurePatterns = new HashMap<>();
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
    private Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter;
    @Getter
    @Setter
    private TriFunction<MultiblockControllerMachine, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay;

    public MultiblockMachineDefinition(ResourceLocation id) {
        super(id);
    }

    public void setPattern(String structureName, Supplier<IBlockPattern> pattern) {
        structurePatterns.put(structureName, pattern);
    }
}
