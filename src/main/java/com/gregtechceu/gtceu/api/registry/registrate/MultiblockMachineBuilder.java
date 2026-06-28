package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder<M extends MultiblockControllerMachine> extends MachineBuilder<MultiblockMachineDefinition, M, MultiblockMachineBuilder<M>> {

    private boolean generator;
    private final Map<String, Function<MultiblockMachineDefinition, IBlockPattern>> patterns;
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    private Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter = (c) -> (a, b) -> 0;
    private @Nullable TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance;
    @Getter
    private BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay = (m, l) -> {};

    public MultiblockMachineBuilder(GTRegistrate registrate, String name,
                                    BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                    BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                    MachineInstanceFactory<M> blockEntityFactory) {
        super(registrate, name, MultiblockMachineDefinition::new, blockFactory, itemFactory, blockEntityFactory);
        patterns = new Object2ReferenceOpenHashMap<>();
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        // always add the formed property to multi controllers
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
    }

    public MultiblockMachineBuilder<M> generator(boolean generator) {
        this.generator = generator;
        return getThis();
    }

    public MultiblockMachineBuilder<M> pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        this.patterns.put(MultiblockControllerMachine.DEFAULT_STRUCTURE, pattern);
        return getThis();
    }

    public MultiblockMachineBuilder<M> pattern(String structureName, Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        this.patterns.put(structureName, pattern);
        return getThis();
    }

    public MultiblockMachineBuilder<M> allowFlip(boolean allowFlip) {
        this.allowFlip = allowFlip;
        return getThis();
    }

    public MultiblockMachineBuilder<M> partSorter(Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter) {
        this.partSorter = partSorter;
        return getThis();
    }

    public MultiblockMachineBuilder<M> partAppearance(@Nullable TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        this.partAppearance = partAppearance;
        return getThis();
    }

    public MultiblockMachineBuilder<M> additionalDisplay(BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay) {
        this.additionalDisplay = additionalDisplay;
        return getThis();
    }

    public MultiblockMachineBuilder<M> recoveryItems(Supplier<ItemLike[]> items) {
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new));
        return getThis();
    }

    public MultiblockMachineBuilder<M> recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return getThis();
    }

    @Tolerate
    public MultiblockMachineBuilder<M> partSorter(Comparator<MultiblockPartMachine> sorter) {
        this.partSorter = $ -> sorter;
        return getThis();
    }

    @Override
    @HideFromJS
    public MultiblockMachineDefinition register() {
        var definition = super.register();
        definition.setGenerator(generator);
        if (patterns.isEmpty()) {
            throw new IllegalStateException("Missing default structure pattern for " + name);
        }
        for (Map.Entry<String, Function<MultiblockMachineDefinition, IBlockPattern>> entry : patterns.entrySet()) {
            definition.setPattern(entry.getKey(), GTMemoizer.memoize(() -> entry.getValue().apply(definition)));
        }

        definition.setAllowFlip(allowFlip);
        if (!recoveryItems.isEmpty()) {
            definition.setRecoveryItems(
                    () -> recoveryItems.stream().map(Supplier::get).flatMap(Arrays::stream).toArray(ItemStack[]::new));
        }
        definition.setPartSorter(GTMemoizer.memoizeFunctionWeakIdent(partSorter));
        if (partAppearance == null) {
            partAppearance = (controller, part, side) -> definition.getAppearance().get();
        }
        definition.setPartAppearance(partAppearance);
        definition.setAdditionalDisplay(additionalDisplay);
        return definition;
    }
}
