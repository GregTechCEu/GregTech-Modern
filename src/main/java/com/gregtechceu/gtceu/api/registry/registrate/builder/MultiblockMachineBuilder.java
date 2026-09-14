package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import com.tterrag.registrate.builders.BuilderCallback;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder<MACHINE extends MultiblockControllerMachine> extends
                                     MachineBuilder<MultiblockMachineDefinition, MACHINE, MultiblockMachineBuilder<MACHINE>> {

    public MultiblockMachineBuilder(GTRegistrate registrate, String name,
                                    BuilderCallback callback,
                                    MachineInstanceFactory<MACHINE> blockEntityFactory) {
        super(registrate, name, callback, blockEntityFactory);
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        // always add the formed property to multi controllers
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
    }

    public MultiblockMachineBuilder<MACHINE> generator(boolean generator) {
        getProperties().generator(generator);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        getProperties().patterns().put(MultiblockControllerMachine.DEFAULT_STRUCTURE, pattern);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> pattern(String structureName,
                                                     Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        getProperties().patterns().put(structureName, pattern);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> allowFlip(boolean allowFlip) {
        getProperties().allowFlip(allowFlip);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> partSorter(Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter) {
        getProperties().partSorter(partSorter);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> partAppearance(TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        getProperties().partAppearance(partAppearance);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> additionalDisplay(BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>> additionalDisplay) {
        getProperties().additionalDisplay(additionalDisplay);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> recoveryItems(Supplier<ItemLike[]> items) {
        getProperties().recoveryItems()
                .add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                        .toArray(ItemStack[]::new));
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> recoveryStacks(Supplier<ItemStack[]> stacks) {
        getProperties().recoveryItems().add(stacks);
        return getThis();
    }

    @Tolerate
    public MultiblockMachineBuilder<MACHINE> partSorter(Comparator<MultiblockPartMachine> sorter) {
        getProperties().partSorter($ -> sorter);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        getProperties().renderMultiblockWorldPreview(renderMultiblockWorldPreview);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        getProperties().renderMultiblockXEIPreview(renderMultiblockXEIPreview);
        return getThis();
    }

    public MultiblockMachineBuilder<MACHINE> multiblockPreviewRenderer(boolean multiblockWorldPreview,
                                                                       boolean multiblockXEIPreview) {
        renderMultiblockWorldPreview(multiblockWorldPreview);
        renderMultiblockXEIPreview(multiblockXEIPreview);
        return getThis();
    }

    @Override
    protected MachineDefinition.Properties createProperties() {
        return new MultiblockMachineDefinition.Properties();
    }

    @Override
    public MultiblockMachineDefinition.Properties getProperties() {
        return (MultiblockMachineDefinition.Properties) super.getProperties();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected @NonNull MultiblockMachineDefinition createEntry() {
        createAdditionalObjects();
        return new MultiblockMachineDefinition(getOwner().makeResourceLocation(getName()), getProperties());
    }
}
