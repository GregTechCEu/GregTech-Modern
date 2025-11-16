package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MultiblockMachineBuilder extends MachineBuilder<MultiblockMachineDefinition> {

    @Setter
    private boolean generator;
    @Setter
    private Function<MultiblockMachineDefinition, BlockPattern> pattern;
    private final List<Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>>> shapeInfos = new ArrayList<>();
    /**
     * Set this to false only if your multiblock is set up such that it could have a wall-shared controller.
     */
    @Setter
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    @Setter
    private Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter = (c) -> (a, b) -> 0;
    @Setter
    private TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay = (m, l) -> {};

    public MultiblockMachineBuilder(GTRegistrate registrate, String name,
                                    Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                    BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                    BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                    TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, MultiblockMachineDefinition::new, metaMachine::apply, blockFactory,
                itemFactory, blockEntityFactory);
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        // always add the formed property to multi controllers
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
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
        this.recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new));
        return this;
    }

    public MultiblockMachineBuilder recoveryStacks(Supplier<ItemStack[]> stacks) {
        this.recoveryItems.add(stacks);
        return this;
    }

    @Override
    public MultiblockMachineBuilder machine(Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return (MultiblockMachineBuilder) super.machine(metaMachine);
    }

    @Override
    public MultiblockMachineBuilder model(@Nullable MachineBuilder.ModelInitializer model) {
        return (MultiblockMachineBuilder) super.model(model);
    }

    @Override
    public MultiblockMachineBuilder blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        return (MultiblockMachineBuilder) super.blockModel(blockModel);
    }

    @Override
    public MultiblockMachineBuilder shape(VoxelShape shape) {
        return (MultiblockMachineBuilder) super.shape(shape);
    }

    @Override
    public MultiblockMachineBuilder multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                              boolean multiBlockXEIPreview) {
        return (MultiblockMachineBuilder) super.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
    }

    @Override
    public MultiblockMachineBuilder rotationState(RotationState rotationState) {
        return (MultiblockMachineBuilder) super.rotationState(rotationState);
    }

    @Override
    public MultiblockMachineBuilder hasBER(boolean hasBER) {
        return (MultiblockMachineBuilder) super.hasBER(hasBER);
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

    public MultiblockMachineBuilder recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
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
    public MultiblockMachineBuilder simpleModel(ResourceLocation model) {
        return (MultiblockMachineBuilder) super.simpleModel(model);
    }

    @Override
    public MultiblockMachineBuilder defaultModel() {
        return (MultiblockMachineBuilder) super.defaultModel();
    }

    @Override
    public MultiblockMachineBuilder tieredHullModel(ResourceLocation model) {
        return (MultiblockMachineBuilder) super.tieredHullModel(model);
    }

    @Override
    public MultiblockMachineBuilder overlayTieredHullModel(String name) {
        return (MultiblockMachineBuilder) super.overlayTieredHullModel(name);
    }

    @Override
    public MultiblockMachineBuilder overlayTieredHullModel(ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder) super.overlayTieredHullModel(overlayModel);
    }

    @Override
    public MultiblockMachineBuilder colorOverlayTieredHullModel(String overlay) {
        return (MultiblockMachineBuilder) super.colorOverlayTieredHullModel(overlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlayTieredHullModel(String overlay,
                                                                @Nullable String pipeOverlay,
                                                                @Nullable String emissiveOverlay) {
        return (MultiblockMachineBuilder) super.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlayTieredHullModel(ResourceLocation overlay) {
        return (MultiblockMachineBuilder) super.colorOverlayTieredHullModel(overlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                @Nullable ResourceLocation pipeOverlay,
                                                                @Nullable ResourceLocation emissiveOverlay) {
        return (MultiblockMachineBuilder) super.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    @Override
    public MultiblockMachineBuilder workableTieredHullModel(ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.workableTieredHullModel(workableModel);
    }

    @Override
    public MultiblockMachineBuilder simpleGeneratorModel(ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.simpleGeneratorModel(workableModel);
    }

    @Override
    public MultiblockMachineBuilder workableCasingModel(ResourceLocation baseCasing, ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder) super.workableCasingModel(baseCasing, overlayModel);
    }

    @Override
    public MultiblockMachineBuilder sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                            ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.sidedOverlayCasingModel(baseCasing, workableModel);
    }

    @Override
    public MultiblockMachineBuilder sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.sidedWorkableCasingModel(baseCasing, workableModel);
    }

    @Override
    public MultiblockMachineBuilder tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
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
    public MultiblockMachineBuilder langValue(@Nullable String langValue) {
        return (MultiblockMachineBuilder) super.langValue(langValue);
    }

    @Override
    public MultiblockMachineBuilder overlaySteamHullModel(String name) {
        return (MultiblockMachineBuilder) super.overlaySteamHullModel(name);
    }

    @Override
    public MultiblockMachineBuilder overlaySteamHullModel(ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder) super.overlaySteamHullModel(overlayModel);
    }

    @Override
    public MultiblockMachineBuilder colorOverlaySteamHullModel(String overlay) {
        return (MultiblockMachineBuilder) super.colorOverlaySteamHullModel(overlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlaySteamHullModel(String overlay,
                                                               @Nullable ResourceLocation pipeOverlay,
                                                               @Nullable String emissiveOverlay) {
        return (MultiblockMachineBuilder) super.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlaySteamHullModel(ResourceLocation overlay,
                                                               @Nullable ResourceLocation pipeOverlay,
                                                               @Nullable ResourceLocation emissiveOverlay) {
        return (MultiblockMachineBuilder) super.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    @Override
    public MultiblockMachineBuilder colorOverlaySteamHullModel(ResourceLocation overlay) {
        return (MultiblockMachineBuilder) super.colorOverlaySteamHullModel(overlay);
    }

    @Override
    public MultiblockMachineBuilder workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        return (MultiblockMachineBuilder) super.workableSteamHullModel(isHighPressure, workableModel);
    }

    @Override
    public MultiblockMachineBuilder tooltips(@Nullable Component... components) {
        return (MultiblockMachineBuilder) super.tooltips(components);
    }

    @Override
    public MultiblockMachineBuilder tooltips(List<? extends @Nullable Component> components) {
        return (MultiblockMachineBuilder) super.tooltips(components);
    }

    @Override
    public MultiblockMachineBuilder conditionalTooltip(Component component, BooleanSupplier condition) {
        return (MultiblockMachineBuilder) super.conditionalTooltip(component, condition);
    }

    @Override
    public MultiblockMachineBuilder conditionalTooltip(Component component, boolean condition) {
        return (MultiblockMachineBuilder) super.conditionalTooltip(component, condition);
    }

    @Tolerate
    public MultiblockMachineBuilder partSorter(Comparator<IMultiPart> sorter) {
        this.partSorter = $ -> sorter;
        return this;
    }

    @Override
    public MultiblockMachineBuilder abilities(PartAbility... abilities) {
        return (MultiblockMachineBuilder) super.abilities(abilities);
    }

    @Override
    public MultiblockMachineBuilder modelProperty(Property<?> property) {
        return (MultiblockMachineBuilder) super.modelProperty(property);
    }

    @Override
    public <T extends Comparable<T>> MultiblockMachineBuilder modelProperty(Property<T> property,
                                                                            @Nullable T defaultValue) {
        return (MultiblockMachineBuilder) super.modelProperty(property, defaultValue);
    }

    @Override
    public MultiblockMachineBuilder modelProperties(Property<?>... properties) {
        return (MultiblockMachineBuilder) super.modelProperties(properties);
    }

    @Override
    public MultiblockMachineBuilder modelProperties(Collection<Property<?>> properties) {
        return (MultiblockMachineBuilder) super.modelProperties(properties);
    }

    @Override
    public MultiblockMachineBuilder modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        return (MultiblockMachineBuilder) super.modelProperties(properties);
    }

    @Override
    public MultiblockMachineBuilder removeModelProperty(Property<?> property) {
        return (MultiblockMachineBuilder) super.removeModelProperty(property);
    }

    @Override
    public MultiblockMachineBuilder clearModelProperties() {
        return (MultiblockMachineBuilder) super.clearModelProperties();
    }

    @Override
    public MultiblockMachineBuilder paintingColor(int paintingColor) {
        return (MultiblockMachineBuilder) super.paintingColor(paintingColor);
    }

    @Override
    public MultiblockMachineBuilder recipeModifier(RecipeModifier recipeModifier) {
        return (MultiblockMachineBuilder) super.recipeModifier(recipeModifier);
    }

    @Override
    public MultiblockMachineBuilder recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        return (MultiblockMachineBuilder) super.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
    }

    @Override
    public MultiblockMachineBuilder recipeModifiers(RecipeModifier... recipeModifiers) {
        return (MultiblockMachineBuilder) super.recipeModifiers(recipeModifiers);
    }

    @Override
    public MultiblockMachineBuilder recipeModifiers(boolean alwaysTryModifyRecipe, RecipeModifier... recipeModifiers) {
        return (MultiblockMachineBuilder) super.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
    }

    public MultiblockMachineBuilder noRecipeModifier() {
        return (MultiblockMachineBuilder) super.noRecipeModifier();
    }

    @Override
    public MultiblockMachineBuilder alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        return (MultiblockMachineBuilder) super.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
    }

    @Override
    public MultiblockMachineBuilder beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        return (MultiblockMachineBuilder) super.beforeWorking(beforeWorking);
    }

    @Override
    public MultiblockMachineBuilder onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        return (MultiblockMachineBuilder) super.onWorking(onWorking);
    }

    @Override
    public MultiblockMachineBuilder onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        return (MultiblockMachineBuilder) super.onWaiting(onWaiting);
    }

    @Override
    public MultiblockMachineBuilder afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        return (MultiblockMachineBuilder) super.afterWorking(afterWorking);
    }

    @Override
    public MultiblockMachineBuilder regressWhenWaiting(boolean regressWhenWaiting) {
        return (MultiblockMachineBuilder) super.regressWhenWaiting(regressWhenWaiting);
    }

    @Override
    public MultiblockMachineBuilder editableUI(@Nullable EditableMachineUI editableUI) {
        return (MultiblockMachineBuilder) super.editableUI(editableUI);
    }

    @Override
    public MultiblockMachineBuilder onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        return (MultiblockMachineBuilder) super.onBlockEntityRegister(onBlockEntityRegister);
    }

    @Override
    public MultiblockMachineBuilder allowExtendedFacing(boolean allowExtendedFacing) {
        return (MultiblockMachineBuilder) super.allowExtendedFacing(allowExtendedFacing);
    }

    @Override
    public MultiblockMachineBuilder allowCoverOnFront(boolean allowCoverOnFront) {
        return (MultiblockMachineBuilder) super.allowCoverOnFront(allowCoverOnFront);
    }

    @Override
    @HideFromJS
    public MultiblockMachineDefinition register() {
        var definition = super.register();
        definition.setGenerator(generator);
        if (pattern == null) {
            throw new IllegalStateException("missing pattern while creating multiblock " + name);
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
        return value = definition;
    }
}
