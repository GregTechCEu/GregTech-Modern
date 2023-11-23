package com.gregtechceu.gtceu.api.registry.registrate;

import com.google.common.base.Suppliers;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
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
import com.gregtechceu.gtceu.common.data.GTCompassNodes;
import com.gregtechceu.gtceu.common.data.GTCompassSections;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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
public class MultiblockMachineBuilder extends MachineBuilder<MultiblockMachineDefinition> {
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

    protected MultiblockMachineBuilder(Registrate registrate, String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, MultiblockMachineDefinition::createDefinition, metaMachine::apply, blockFactory, itemFactory, blockEntityFactory);
        this.compassSections(GTCompassSections.MULTIBLOCK);
    }

    public static MultiblockMachineBuilder createMulti(Registrate registrate, String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MultiblockMachineBuilder(registrate, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public MultiblockMachineBuilder shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        this.shapeInfos.add(d -> List.of(shape.apply(d)));
        return this;
    }

    public MultiblockMachineBuilder shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        this.shapeInfos.add(shapes);
        return this;
    }

    public MultiblockMachineBuilder recoveryItems(Supplier<ItemLike[]> items) {
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
        return this;
    }

    public MultiblockMachineBuilder recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return this;
    }

    @Override
    public MultiblockMachineBuilder renderer(@Nullable Supplier<IRenderer> renderer) {
        return (MultiblockMachineBuilder) super.renderer(renderer);
    }

    @Override
    public MultiblockMachineBuilder shape(VoxelShape shape) {
        return (MultiblockMachineBuilder) super.shape(shape);
    }

    @Override
    public MultiblockMachineBuilder rotationState(RotationState rotationState) {
        return (MultiblockMachineBuilder) super.rotationState(rotationState);
    }

    @Override
    public MultiblockMachineBuilder hasTESR(boolean hasTESR) {
        return (MultiblockMachineBuilder) super.hasTESR(hasTESR);
    }

    @Override
    public MultiblockMachineBuilder blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        return (MultiblockMachineBuilder) super.blockProp(blockProp);
    }

    @Override
    public MultiblockMachineBuilder itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        return (MultiblockMachineBuilder) super.itemProp(itemProp);
    }

    @Override
    public MultiblockMachineBuilder blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        return (MultiblockMachineBuilder) super.blockBuilder(blockBuilder);
    }

    @Override
    public MultiblockMachineBuilder itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        return (MultiblockMachineBuilder) super.itemBuilder(itemBuilder);
    }

    @Override
    public MultiblockMachineBuilder recipeTypes(GTRecipeType... recipeTypes) {
        return (MultiblockMachineBuilder) super.recipeTypes(recipeTypes);
    }

    @Override
    public MultiblockMachineBuilder recipeType(GTRecipeType recipeTypes) {
        return (MultiblockMachineBuilder) super.recipeType(recipeTypes);
    }

    @Override
    public MultiblockMachineBuilder tier(int tier) {
        return (MultiblockMachineBuilder) super.tier(tier);
    }

    public MultiblockMachineBuilder recipeOutputLimits(Object2IntMap<RecipeCapability<?>> map) {
        return (MultiblockMachineBuilder) super.recipeOutputLimits(map);
    }

    @Override
    public MultiblockMachineBuilder addOutputLimit(RecipeCapability<?> capability, int limit) {
        return (MultiblockMachineBuilder) super.addOutputLimit(capability, limit);
    }

    @Override
    public MultiblockMachineBuilder itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        return (MultiblockMachineBuilder) super.itemColor(itemColor);
    }

    @Override
    public MultiblockMachineBuilder modelRenderer(Supplier<ResourceLocation> model) {
        return (MultiblockMachineBuilder) super.modelRenderer(model);
    }

    @Override
    public MultiblockMachineBuilder defaultModelRenderer() {
        return (MultiblockMachineBuilder) super.defaultModelRenderer();
    }

    @Override
    public MultiblockMachineBuilder overlayTieredHullRenderer(String name) {
        return (MultiblockMachineBuilder) super.overlayTieredHullRenderer(name);
    }

    @Override
    public MultiblockMachineBuilder workableTieredHullRenderer(ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.workableTieredHullRenderer(workableModel);
    }

    @Override
    public MultiblockMachineBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder) super.workableCasingRenderer(baseCasing, overlayModel);
    }

    public MultiblockMachineBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel, boolean tint) {
        return (MultiblockMachineBuilder) super.workableCasingRenderer(baseCasing, overlayModel, tint);
    }

    public MultiblockMachineBuilder sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel, boolean tint) {
        return (MultiblockMachineBuilder) super.sidedWorkableCasingRenderer(basePath, overlayModel, tint);
    }

    @Override
    public MultiblockMachineBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        return (MultiblockMachineBuilder) super.tooltipBuilder(tooltipBuilder);
    }

    @Override
    public MultiblockMachineBuilder appearance(Supplier<BlockState> state) {
        return (MultiblockMachineBuilder) super.appearance(state);
    }

    @Override
    public MultiblockMachineBuilder appearanceBlock(Supplier<? extends Block> block) {
        return (MultiblockMachineBuilder) super.appearanceBlock(block);
    }

    @Override
    public MultiblockMachineBuilder langValue(String langValue) {
        return (MultiblockMachineBuilder) super.langValue(langValue);
    }

    @Override
    public MultiblockMachineBuilder overlaySteamHullRenderer(String name) {
        return (MultiblockMachineBuilder) super.overlaySteamHullRenderer(name);
    }

    @Override
    public MultiblockMachineBuilder workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.workableSteamHullRenderer(isHighPressure, workableModel);
    }

    @Override
    public MultiblockMachineBuilder tooltips(Component... components) {
        return (MultiblockMachineBuilder) super.tooltips(components);
    }

    @Override
    public MultiblockMachineBuilder abilities(PartAbility... abilities) {
        return (MultiblockMachineBuilder) super.abilities(abilities);
    }

    @Override
    public MultiblockMachineBuilder paintingColor(int paintingColor) {
        return (MultiblockMachineBuilder) super.paintingColor(paintingColor);
    }

    @Override
    public MultiblockMachineBuilder recipeModifier(BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier) {
        return (MultiblockMachineBuilder) super.recipeModifier(recipeModifier);
    }

    @Override
    public MultiblockMachineBuilder recipeModifier(BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier, boolean alwaysTryModifyRecipe) {
        return (MultiblockMachineBuilder) super.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
    }

    @Override
    public MultiblockMachineBuilder alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        return (MultiblockMachineBuilder) super.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
    }

    @Override
    public MultiblockMachineBuilder editableUI(@Nullable EditableMachineUI editableUI) {
        return (MultiblockMachineBuilder) super.editableUI(editableUI);
    }

    @Override
    public MultiblockMachineBuilder compassSections(CompassSection... sections) {
        return (MultiblockMachineBuilder) super.compassSections(sections);
    }

    @Override
    public MultiblockMachineBuilder compassNodeSelf() {
        return (MultiblockMachineBuilder) super.compassNodeSelf();
    }

    @Override
    public MultiblockMachineBuilder compassNode(String compassNode) {
        return (MultiblockMachineBuilder) super.compassNode(compassNode);
    }

    @Override
    public MultiblockMachineBuilder compassPreNodes(CompassSection section, String... compassNodes) {
        return (MultiblockMachineBuilder) super.compassPreNodes(section, compassNodes);
    }

    @Override
    public MultiblockMachineBuilder compassPreNodes(ResourceLocation... compassNodes) {
        return (MultiblockMachineBuilder) super.compassPreNodes(compassNodes);
    }

    @Override
    public MultiblockMachineBuilder compassPreNodes(CompassNode... compassNodes) {
        return (MultiblockMachineBuilder) super.compassPreNodes(compassNodes);
    }

    @Override
    public MultiblockMachineBuilder onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        return (MultiblockMachineBuilder) super.onBlockEntityRegister(onBlockEntityRegister);
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
