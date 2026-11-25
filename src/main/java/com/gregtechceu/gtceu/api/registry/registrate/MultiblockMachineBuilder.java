package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.AdditionalDisplayWidgetProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAppearanceProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.PartSorter;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;

import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder<DEFINITION extends MultiblockMachineDefinition,
        MACHINE extends MultiblockControllerMachine,
        SELF extends MultiblockMachineBuilder<DEFINITION, MACHINE, SELF>>
                                     extends MachineBuilder<DEFINITION, MACHINE, SELF> {

    private boolean generator;
    private final Map<String, Function<MultiblockMachineDefinition, IBlockPattern>> patterns;
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    private PartSorter partSorter = PartSorter.DEFAULT;
    private PartAppearanceProvider partAppearance = PartAppearanceProvider.DEFAULT;

    @Getter
    private AdditionalDisplayWidgetProvider additionalDisplay = AdditionalDisplayWidgetProvider.DEFAULT;

    public MultiblockMachineBuilder(GTRegistrate registrate, String name,
                                    BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                                    BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                    MachineInstanceFactory<MACHINE> blockEntityFactory) {
        super(registrate, name, (loc -> (DEFINITION) new MultiblockMachineDefinition(loc)),
                blockFactory,
                itemFactory, blockEntityFactory);
        patterns = new Object2ReferenceOpenHashMap<>();
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        // always add the formed property to multi controllers
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
    }

    public SELF generator(boolean generator) {
        this.generator = generator;
        return getThis();
    }

    public SELF pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        this.patterns.put(MultiblockControllerMachine.DEFAULT_STRUCTURE, pattern);
        return getThis();
    }

    public SELF pattern(String structureName, Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        this.patterns.put(structureName, pattern);
        return getThis();
    }

    public SELF allowFlip(boolean allowFlip) {
        this.allowFlip = allowFlip;
        return getThis();
    }

    public SELF partSorter(PartSorter partSorter) {
        this.partSorter = partSorter;
        return getThis();
    }

    public SELF partAppearance(PartAppearanceProvider partAppearance) {
        this.partAppearance = partAppearance;
        return getThis();
    }

    public SELF additionalDisplay(AdditionalDisplayWidgetProvider additionalDisplay) {
        this.additionalDisplay = additionalDisplay;
        return getThis();
    }

    public SELF recoveryItems(Supplier<ItemLike[]> items) {
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new));
        return getThis();
    }

    public SELF recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return getThis();
    }

    @Tolerate
    public SELF partSorter(Comparator<MultiblockPartMachine> sorter) {
        this.partSorter = $ -> sorter;
        return getThis();
    }

    @Override
    @HideFromJS
    public DEFINITION register() {
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
        definition.setPartSorter(partSorter);
        definition.setPartAppearance(partAppearance);
        definition.setAdditionalDisplay(additionalDisplay);
        return value = definition;
    }
}
