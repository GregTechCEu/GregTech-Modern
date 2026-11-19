package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

@SuppressWarnings("unused")
public class MultiblockMachineBuilderJS extends MachineBuilderJS<MultiblockMachineBuilderJS> {

    public MultiblockMachineBuilderJS(ResourceLocation id,
                                      MultiblockMachineBuilder<?> internal) {
        super(id, internal);
    }

    private MultiblockMachineBuilder<?> getInternal() {
        return (MultiblockMachineBuilder<?>) internal;
    }

    public MultiblockMachineBuilderJS instanceFactory(MachineInstanceFactory machine) {
        getInternal().instanceFactory(machine);
        return getThis();
    }

    public MultiblockMachineBuilderJS generator(boolean generator) {
        getInternal().generator(generator);
        return getThis();
    }

    public MultiblockMachineBuilderJS pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        getInternal().pattern(pattern);
        return getThis();
    }

    public MultiblockMachineBuilderJS allowFlip(boolean allowFlip) {
        getInternal().allowFlip(allowFlip);
        return getThis();
    }

    public MultiblockMachineBuilderJS partAppearance(TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        getInternal().partAppearance(partAppearance);
        return getThis();
    }

    public MultiblockMachineBuilderJS partSorter(Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter) {
        getInternal().partSorter(partSorter);
        return getThis();
    }

    public MultiblockMachineBuilderJS additionalDisplay(BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>> additionalDisplay) {
        getInternal().additionalDisplay(additionalDisplay);
        return getThis();
    }

    public MultiblockMachineBuilderJS recoveryItems(Supplier<ItemLike[]> items) {
        getInternal().recoveryItems(items);
        return getThis();
    }

    public MultiblockMachineBuilderJS recoveryStacks(Supplier<ItemStack[]> stacks) {
        getInternal().recoveryStacks(stacks);
        return getThis();
    }

    public MultiblockMachineBuilderJS renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        getInternal().renderMultiblockWorldPreview(renderMultiblockWorldPreview);
        return getThis();
    }

    public MultiblockMachineBuilderJS renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        getInternal().renderMultiblockXEIPreview(renderMultiblockXEIPreview);
        return getThis();
    }

    public MultiblockMachineBuilderJS multiblockPreviewRenderer(boolean multiblockWorldPreview,
                                                                boolean multiblockXEIPreview) {
        renderMultiblockWorldPreview(multiblockWorldPreview);
        renderMultiblockXEIPreview(multiblockXEIPreview);
        return getThis();
    }

    public static MultiblockMachineBuilderJS createKJSMulti(ResourceLocation id) {
        var baseBuilder = GTRegistrate.createIgnoringListenerErrors(id.getNamespace())
                .multiblock(id.getPath(), WorkableElectricMultiblockMachine::new);
        return new MultiblockMachineBuilderJS(id, baseBuilder);
    }

    public static MultiblockMachineBuilderJS createKJSMulti(ResourceLocation id,
                                                            MachineInstanceFactory<? extends MultiblockControllerMachine> machine) {
        var baseBuilder = GTRegistrate.createIgnoringListenerErrors(id.getNamespace())
                .multiblock(id.getPath(), machine);
        return new MultiblockMachineBuilderJS(id, baseBuilder);
    }
}
