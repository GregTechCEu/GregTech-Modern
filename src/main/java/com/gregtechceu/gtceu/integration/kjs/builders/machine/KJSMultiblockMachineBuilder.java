package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

@SuppressWarnings("unused")
public class KJSMultiblockMachineBuilder<P> extends MachineBuilderWrapper<MultiblockMachineDefinition, MultiblockMachineBuilder<P>> {

    public KJSMultiblockMachineBuilder(MultiblockMachineBuilder<P> builder) {
        super(builder);
    }

    public KJSMultiblockMachineBuilder<P> generator(boolean generator) {
        builder.generator(generator);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        builder.pattern(pattern);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> allowFlip(boolean allowFlip) {
        builder.allowFlip(allowFlip);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> partSorter(Comparator<IMultiPart> partSorter) {
        builder.partSorter(partSorter);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> partAppearance(@Nullable TriFunction<MultiblockControllerMachine, IMultiPart, Direction, BlockState> partAppearance) {
        builder.partAppearance(partAppearance);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> additionalDisplay(BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay) {
        builder.additionalDisplay(additionalDisplay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recoveryItems(Supplier<ItemLike[]> items) {
        builder.recoveryItems(items);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recoveryStacks(Supplier<ItemStack[]> stacks) {
        builder.recoveryStacks(stacks);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> definition(Function<ResourceLocation, MultiblockMachineDefinition> definition) {
        builder.definition(definition);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> blockEntityFactory(Function<BlockEntityCreationInfo, MetaMachine> machine) {
        builder.blockEntityFactory(machine);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> model(@Nullable MachineBuilder.ModelInitializer model) {
        builder.model(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        builder.blockModel(blockModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> shape(VoxelShape shape) {
        builder.shape(shape);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                 boolean multiBlockXEIPreview) {
        builder.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> rotationState(RotationState rotationState) {
        builder.rotationState(rotationState);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> hasBER(boolean hasBER) {
        builder.hasBER(hasBER);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        builder.blockProp(blockProp);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        builder.itemProp(itemProp);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeTypes(GTRecipeType... recipeTypes) {
        builder.recipeTypes(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeType(GTRecipeType recipeTypes) {
        builder.recipeType(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> tier(int tier) {
        builder.tier(tier);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        builder.recipeOutputLimits(map);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> addOutputLimit(RecipeCapability<?> capability, int limit) {
        builder.addOutputLimit(capability, limit);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        builder.itemColor(itemColor);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> simpleModel(ResourceLocation model) {
        builder.simpleModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> defaultModel() {
        builder.defaultModel();
        return this;
    }

    public KJSMultiblockMachineBuilder<P> tieredHullModel(ResourceLocation model) {
        builder.tieredHullModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> overlayTieredHullModel(ResourceLocation overlayModel) {
        builder.overlayTieredHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> colorOverlayTieredHullModel(ResourceLocation overlay) {
        builder.colorOverlayTieredHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                   @Nullable ResourceLocation pipeOverlay,
                                                                   @Nullable ResourceLocation emissiveOverlay) {
        builder.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> workableTieredHullModel(ResourceLocation workableModel) {
        builder.workableTieredHullModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> simpleGeneratorModel(ResourceLocation workableModel) {
        builder.simpleGeneratorModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> workableCasingModel(ResourceLocation baseCasing,
                                                           ResourceLocation overlayModel) {
        builder.workableCasingModel(baseCasing, overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                               ResourceLocation workableModel) {
        builder.sidedOverlayCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                                ResourceLocation workableModel) {
        builder.sidedWorkableCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> overlaySteamHullModel(ResourceLocation overlayModel) {
        builder.overlaySteamHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                  @Nullable ResourceLocation pipeOverlay,
                                                                  @Nullable ResourceLocation emissiveOverlay) {
        builder.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> colorOverlaySteamHullModel(ResourceLocation overlay) {
        builder.colorOverlaySteamHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> workableSteamHullModel(boolean isHighPressure,
                                                              ResourceLocation workableModel) {
        builder.workableSteamHullModel(isHighPressure, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        builder.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> appearance(@Nullable Supplier<BlockState> state) {
        builder.appearance(state);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> appearanceBlock(Supplier<? extends Block> block) {
        builder.appearanceBlock(block);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> langValue(@Nullable String langValue) {
        builder.langValue(langValue);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> tooltips(Component... components) {
        builder.tooltips(components);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        builder.conditionalTooltip(component, condition.get());
        return this;
    }

    public KJSMultiblockMachineBuilder<P> conditionalTooltip(Component component, boolean condition) {
        builder.conditionalTooltip(component, condition);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> abilities(PartAbility... abilities) {
        builder.abilities(abilities);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> paintingColor(int paintingColor) {
        builder.paintingColor(paintingColor);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeModifier(RecipeModifier recipeModifier) {
        builder.recipeModifier(recipeModifier);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeModifier(RecipeModifier recipeModifier,
                                                      boolean alwaysTryModifyRecipe) {
        builder.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeModifiers(RecipeModifier... recipeModifiers) {
        builder.recipeModifiers(recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> recipeModifiers(boolean alwaysTryModifyRecipe,
                                                       RecipeModifier... recipeModifiers) {
        builder.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> noRecipeModifier() {
        builder.noRecipeModifier();
        return this;
    }

    public KJSMultiblockMachineBuilder<P> alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        builder.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        builder.beforeWorking(beforeWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        builder.onWorking(onWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        builder.onWaiting(onWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        builder.afterWorking(afterWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> regressWhenWaiting(boolean regressWhenWaiting) {
        builder.regressWhenWaiting(regressWhenWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> onBlockEntityRegister(NonNullConsumer<BlockEntityType<MetaMachine>> onBlockEntityRegister) {
        builder.onBlockEntityRegister(onBlockEntityRegister);
        return this;
    }

    public KJSMultiblockMachineBuilder<P> allowExtendedFacing(boolean allowExtendedFacing) {
        builder.allowExtendedFacing(allowExtendedFacing);
        return this;
    }

    public static KJSMultiblockMachineBuilder<?> createKJSMulti(ResourceLocation id) {
        var baseBuilder = GTRegistrate.create(id.getNamespace(), false)
                .multiblock(id.getPath(), WorkableElectricMultiblockMachine::new);
        return new KJSMultiblockMachineBuilder<>(baseBuilder);
    }

    public static KJSMultiblockMachineBuilder<?> createKJSMulti(ResourceLocation id,
                                                             KJSTieredMachineBuilder.CreationFunction<? extends MultiblockControllerMachine> machine) {
        var baseBuilder = GTRegistrate.create(id.getNamespace(), false)
                .multiblock(id.getPath(), machine::create);
        return new KJSMultiblockMachineBuilder<>(baseBuilder);
    }
}
