package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.MultiblockShapeInfo;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder<DEFINITION extends MultiblockMachineDefinition,
        TYPE extends MultiblockMachineBuilder<DEFINITION, TYPE>> extends MachineBuilder<DEFINITION, TYPE> {

    private boolean generator;
    private Function<MultiblockMachineDefinition, BlockPattern> pattern;
    private final List<Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>>> shapeInfos = new ArrayList<>();
    /**
     * Set this to false only if your multiblock is set up such that it could have a wall-shared controller.
     */
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    private Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter = (c) -> (a, b) -> 0;
    private @Nullable TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay = (m, l) -> {};

    public MultiblockMachineBuilder(GTRegistrate registrate, String name,
                                    Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                    BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                    BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                    TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, (loc -> (DEFINITION) new MultiblockMachineDefinition(loc)), metaMachine::apply,
                blockFactory,
                itemFactory, blockEntityFactory);
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        // always add the formed property to multi controllers
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
    }

    public TYPE generator(boolean generator) {
        this.generator = generator;
        return getThis();
    }

    public TYPE pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
        this.pattern = pattern;
        return getThis();
    }

    public TYPE allowFlip(boolean allowFlip) {
        this.allowFlip = allowFlip;
        return getThis();
    }

    public TYPE partSorter(Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter) {
        this.partSorter = partSorter;
        return getThis();
    }

    public TYPE partAppearance(@Nullable TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance) {
        this.partAppearance = partAppearance;
        return getThis();
    }

    public TYPE additionalDisplay(BiConsumer<IMultiController, List<Component>> additionalDisplay) {
        this.additionalDisplay = additionalDisplay;
        return getThis();
    }

    public TYPE shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        this.shapeInfos.add(d -> List.of(shape.apply(d)));
        return getThis();
    }

    public TYPE shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        this.shapeInfos.add(shapes);
        return getThis();
    }

    public TYPE recoveryItems(Supplier<ItemLike[]> items) {
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new));
        return getThis();
    }

    public TYPE recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return getThis();
    }

    @Tolerate
    public TYPE partSorter(Comparator<IMultiPart> sorter) {
        this.partSorter = $ -> sorter;
        return getThis();
    }

    @Override
    @HideFromJS
    public DEFINITION register() {
        var definition = super.register();
        definition.setGenerator(generator);
        // noinspection ConstantValue it can be null by mistake.
        if (pattern == null) {
            GTCEu.LOGGER.error(
                    "missing pattern while creating multiblock {}, something's likely gone very wrong! Check the full log.",
                    name);
        }
        definition.setPatternFactory(GTMemoizer.memoize(() -> pattern.apply(definition)));
        definition.setShapes(() -> shapeInfos.stream().map(factory -> factory.apply(definition))
                .flatMap(Collection::stream).toList());
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
