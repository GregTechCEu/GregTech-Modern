package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.AdditionalDisplayWidgetProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAppearanceProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.PartSorter;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    private PartSorter partSorter;
    @Getter
    @Setter
    private PartAppearanceProvider partAppearance;
    @Getter
    @Setter
    private AdditionalDisplayWidgetProvider additionalDisplay;

    public MultiblockMachineDefinition(ResourceLocation id) {
        super(id);
    }

    public void setPattern(String structureName, Supplier<IBlockPattern> pattern) {
        structurePatterns.put(structureName, pattern);
    }
}
