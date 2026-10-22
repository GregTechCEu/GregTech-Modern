package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiblockMachineDefinition extends MachineDefinition {

    // spotless:off
    public static final Codec<MultiblockMachineDefinition> CODEC = GTRegistries.MACHINES.codec().comapFlatMap(def -> {
        if (def instanceof MultiblockMachineDefinition mDef) return DataResult.success(mDef);
        else return DataResult.error(() -> "%s is not a multiblock machine definition".formatted(def.getId()));
    }, v -> v);
    //spotless:on

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
    private Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter;
    @Getter
    @Setter
    private TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>> additionalDisplay;

    public MultiblockMachineDefinition(ResourceLocation id) {
        super(id);
    }

    public void setPattern(String structureName, Supplier<IBlockPattern> pattern) {
        structurePatterns.put(structureName, pattern);
    }
}
