package com.gregtechceu.gtceu.api.registry.registrate;

import com.google.common.base.Suppliers;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MachineBuilder
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder<MACHINE extends MultiblockControllerMachine> extends MachineBuilder<MultiblockMachineDefinition, MACHINE> {
    @Setter
    private Function<MultiblockMachineDefinition, BlockPattern> pattern;
    private final List<Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>>> shapeInfos = new ArrayList<>();
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    @Setter
    private Comparator<IMultiPart> partSorter = (a, b) -> 0;
    @Setter
    private TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay = (m, l) -> {};

    protected MultiblockMachineBuilder(Registrate registrate, String name, Function<IMachineBlockEntity, MACHINE> metaMachine,
                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, MultiblockMachineDefinition::createDefinition, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public static <MACHINE extends MultiblockControllerMachine> MultiblockMachineBuilder<MACHINE> createMulti(Registrate registrate, String name, Function<IMachineBlockEntity, MACHINE> metaMachine,
                                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MultiblockMachineBuilder<>(registrate, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public MultiblockMachineBuilder<MACHINE> shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        this.shapeInfos.add(d -> List.of(shape.apply(d)));
        return this;
    }

    public MultiblockMachineBuilder<MACHINE> shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        this.shapeInfos.add(shapes);
        return this;
    }

    public MultiblockMachineBuilder<MACHINE> recoveryItems(Supplier<ItemLike[]> items) {
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
        return this;
    }

    public MultiblockMachineBuilder<MACHINE> recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return this;
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> renderer(@Nullable Supplier<IRenderer> renderer) {
        return (MultiblockMachineBuilder<MACHINE>) super.renderer(renderer);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> shape(VoxelShape shape) {
        return (MultiblockMachineBuilder<MACHINE>) super.shape(shape);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> rotationState(RotationState rotationState) {
        return (MultiblockMachineBuilder<MACHINE>) super.rotationState(rotationState);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> hasTESR(boolean hasTESR) {
        return (MultiblockMachineBuilder<MACHINE>) super.hasTESR(hasTESR);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        return (MultiblockMachineBuilder<MACHINE>) super.blockProp(blockProp);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        return (MultiblockMachineBuilder<MACHINE>) super.itemProp(itemProp);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        return (MultiblockMachineBuilder<MACHINE>) super.blockBuilder(blockBuilder);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        return (MultiblockMachineBuilder<MACHINE>) super.itemBuilder(itemBuilder);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> recipeType(GTRecipeType recipeType) {
        return (MultiblockMachineBuilder<MACHINE>) super.recipeType(recipeType);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> tier(int tier) {
        return (MultiblockMachineBuilder<MACHINE>) super.tier(tier);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        return (MultiblockMachineBuilder<MACHINE>) super.itemColor(itemColor);
    }

    @Override
    @Deprecated
    public MultiblockMachineBuilder<MACHINE> overclockingLogic(OverclockingLogic overclockingLogic) {
        return (MultiblockMachineBuilder<MACHINE>) super.overclockingLogic(overclockingLogic);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> modelRenderer(Supplier<ResourceLocation> model) {
        return (MultiblockMachineBuilder<MACHINE>) super.modelRenderer(model);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> defaultModelRenderer() {
        return (MultiblockMachineBuilder<MACHINE>) super.defaultModelRenderer();
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> overlayTieredHullRenderer(String name) {
        return (MultiblockMachineBuilder<MACHINE>) super.overlayTieredHullRenderer(name);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> workableTieredHullRenderer(ResourceLocation workableModel) {
        return (MultiblockMachineBuilder<MACHINE>) super.workableTieredHullRenderer(workableModel);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder<MACHINE>) super.workableCasingRenderer(baseCasing, overlayModel);
    }

    public MultiblockMachineBuilder<MACHINE> workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel, boolean tint) {
        return (MultiblockMachineBuilder<MACHINE>) super.workableCasingRenderer(baseCasing, overlayModel, tint);
    }

    public MultiblockMachineBuilder<MACHINE> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel, boolean tint) {
        return (MultiblockMachineBuilder<MACHINE>) super.sidedWorkableCasingRenderer(basePath, overlayModel, tint);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        return (MultiblockMachineBuilder<MACHINE>) super.tooltipBuilder(tooltipBuilder);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> appearance(Supplier<BlockState> state) {
        return (MultiblockMachineBuilder<MACHINE>) super.appearance(state);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> appearanceBlock(Supplier<? extends Block> block) {
        return (MultiblockMachineBuilder<MACHINE>) super.appearanceBlock(block);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> langValue(String langValue) {
        return (MultiblockMachineBuilder<MACHINE>) super.langValue(langValue);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> overlaySteamHullRenderer(String name) {
        return (MultiblockMachineBuilder<MACHINE>) super.overlaySteamHullRenderer(name);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
        return (MultiblockMachineBuilder<MACHINE>) super.workableSteamHullRenderer(isHighPressure, workableModel);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> tooltips(Component... components) {
        return (MultiblockMachineBuilder<MACHINE>) super.tooltips(components);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> abilities(PartAbility... abilities) {
        return (MultiblockMachineBuilder<MACHINE>) super.abilities(abilities);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> paintingColor(int paintingColor) {
        return (MultiblockMachineBuilder<MACHINE>) super.paintingColor(paintingColor);
    }

    @Override
    public MultiblockMachineBuilder<MACHINE> onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        return (MultiblockMachineBuilder<MACHINE>) super.onBlockEntityRegister(onBlockEntityRegister);
    }

    @Override
    public MultiblockMachineDefinition register() {
        var definition = (MultiblockMachineDefinition) super.register();
        if (pattern == null) {
            throw new IllegalStateException("missing pattern while creating multiblock " + name);
        }
        definition.setPatternFactory(Suppliers.memoize(() -> pattern.apply(definition)));
        definition.setShapes(() -> shapeInfos.stream().map(factory -> factory.apply(definition)).flatMap(Collection::stream).toList());
        if (!recoveryItems.isEmpty()) {
            definition.setRecoveryItems(() -> recoveryItems.stream().map(Supplier::get).flatMap(Arrays::stream).toArray(ItemStack[]::new));
        }
        definition.setPartSorter(partSorter);
        if (partAppearance == null) {
            partAppearance = (controller, part, side) -> definition.getAppearance().get();
        }
        definition.setPartAppearance(partAppearance);
        definition.setAdditionalDisplay(additionalDisplay);
        return definition;
    }
}
