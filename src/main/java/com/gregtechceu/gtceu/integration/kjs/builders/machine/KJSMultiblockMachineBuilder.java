package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
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
public class KJSMultiblockMachineBuilder<P, M extends MultiblockControllerMachine> extends MachineBuilderWrapper<MultiblockMachineDefinition, MultiblockMachineBuilder<P, M>> {

    public KJSMultiblockMachineBuilder(MultiblockMachineBuilder<P, M> builder) {
        super(builder);
    }

    public KJSMultiblockMachineBuilder<P, M> generator(boolean generator) {
        builder.generator(generator);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        builder.pattern(pattern);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> allowFlip(boolean allowFlip) {
        builder.allowFlip(allowFlip);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> partSorter(Comparator<MultiblockPartMachine> partSorter) {
        builder.partSorter(partSorter);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> partAppearance(@Nullable TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        builder.partAppearance(partAppearance);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> additionalDisplay(BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay) {
        builder.additionalDisplay(additionalDisplay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recoveryItems(Supplier<ItemLike[]> items) {
        builder.recoveryItems(items);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recoveryStacks(Supplier<ItemStack[]> stacks) {
        builder.recoveryStacks(stacks);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> definition(Function<ResourceLocation, MultiblockMachineDefinition> definition) {
        builder.definition(definition);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> instanceFactory(MachineInstanceFactory<M> machine) {
        builder.instanceFactory(machine);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> model(@Nullable MachineBuilder.ModelInitializer model) {
        builder.model(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        builder.blockModel(blockModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> shape(VoxelShape shape) {
        builder.shape(shape);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                 boolean multiBlockXEIPreview) {
        builder.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> rotationState(RotationState rotationState) {
        builder.rotationState(rotationState);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> hasBER(boolean hasBER) {
        builder.hasBER(hasBER);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        builder.blockProp(blockProp);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        builder.itemProp(itemProp);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeTypes(GTRecipeType... recipeTypes) {
        builder.recipeTypes(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeType(GTRecipeType recipeTypes) {
        builder.recipeType(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> tier(int tier) {
        builder.tier(tier);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        builder.recipeOutputLimits(map);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> addOutputLimit(RecipeCapability<?> capability, int limit) {
        builder.addOutputLimit(capability, limit);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        builder.itemColor(itemColor);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> simpleModel(ResourceLocation model) {
        builder.simpleModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> defaultModel() {
        builder.defaultModel();
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> tieredHullModel(ResourceLocation model) {
        builder.tieredHullModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> overlayTieredHullModel(ResourceLocation overlayModel) {
        builder.overlayTieredHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> colorOverlayTieredHullModel(ResourceLocation overlay) {
        builder.colorOverlayTieredHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                   @Nullable ResourceLocation pipeOverlay,
                                                                   @Nullable ResourceLocation emissiveOverlay) {
        builder.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> workableTieredHullModel(ResourceLocation workableModel) {
        builder.workableTieredHullModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> simpleGeneratorModel(ResourceLocation workableModel) {
        builder.simpleGeneratorModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> workableCasingModel(ResourceLocation baseCasing,
                                                           ResourceLocation overlayModel) {
        builder.workableCasingModel(baseCasing, overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                               ResourceLocation workableModel) {
        builder.sidedOverlayCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                                ResourceLocation workableModel) {
        builder.sidedWorkableCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> overlaySteamHullModel(ResourceLocation overlayModel) {
        builder.overlaySteamHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                  @Nullable ResourceLocation pipeOverlay,
                                                                  @Nullable ResourceLocation emissiveOverlay) {
        builder.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> colorOverlaySteamHullModel(ResourceLocation overlay) {
        builder.colorOverlaySteamHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> workableSteamHullModel(boolean isHighPressure,
                                                              ResourceLocation workableModel) {
        builder.workableSteamHullModel(isHighPressure, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        builder.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> appearance(@Nullable Supplier<BlockState> state) {
        builder.appearance(state);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> appearanceBlock(Supplier<? extends Block> block) {
        builder.appearanceBlock(block);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> langValue(@Nullable String langValue) {
        builder.langValue(langValue);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> tooltips(Component... components) {
        builder.tooltips(components);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        builder.conditionalTooltip(component, condition.get());
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> conditionalTooltip(Component component, boolean condition) {
        builder.conditionalTooltip(component, condition);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> abilities(PartAbility... abilities) {
        builder.abilities(abilities);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> paintingColor(int paintingColor) {
        builder.paintingColor(paintingColor);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeModifier(RecipeModifier recipeModifier) {
        builder.recipeModifier(recipeModifier);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeModifier(RecipeModifier recipeModifier,
                                                      boolean alwaysTryModifyRecipe) {
        builder.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeModifiers(RecipeModifier... recipeModifiers) {
        builder.recipeModifiers(recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> recipeModifiers(boolean alwaysTryModifyRecipe,
                                                       RecipeModifier... recipeModifiers) {
        builder.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> noRecipeModifier() {
        builder.noRecipeModifier();
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        builder.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        builder.beforeWorking(beforeWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        builder.onWorking(onWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        builder.onWaiting(onWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        builder.afterWorking(afterWorking);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> regressWhenWaiting(boolean regressWhenWaiting) {
        builder.regressWhenWaiting(regressWhenWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> onBlockEntityRegister(NonNullConsumer<BlockEntityType<MetaMachine>> onBlockEntityRegister) {
        builder.onBlockEntityRegister(onBlockEntityRegister);
        return this;
    }

    public KJSMultiblockMachineBuilder<P, M> allowExtendedFacing(boolean allowExtendedFacing) {
        builder.allowExtendedFacing(allowExtendedFacing);
        return this;
    }

    public static KJSMultiblockMachineBuilder<?, ?> createKJSMulti(ResourceLocation id) {
        var baseBuilder = GTRegistrate.create(id.getNamespace(), false)
                .multiblock(id.getPath(), WorkableElectricMultiblockMachine::new);
        return new KJSMultiblockMachineBuilder<>(baseBuilder);
    }

    public static KJSMultiblockMachineBuilder<?, ?> createKJSMulti(ResourceLocation id,
                                                             KJSTieredMachineBuilder.CreationFunction<? extends MultiblockControllerMachine> machine) {
        var baseBuilder = GTRegistrate.create(id.getNamespace(), false)
                .multiblock(id.getPath(), machine::create);
        return new KJSMultiblockMachineBuilder<>(baseBuilder);
    }
}
