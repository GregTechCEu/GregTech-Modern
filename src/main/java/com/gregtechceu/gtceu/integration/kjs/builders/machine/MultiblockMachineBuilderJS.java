package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.*;
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
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

@SuppressWarnings("unused")
public class MultiblockMachineBuilderJS extends BuilderBase<MultiblockMachineDefinition>
                                        implements IMachineBuilderKJS {

    private final MultiblockMachineBuilder<?> internal;

    public MultiblockMachineBuilderJS(ResourceLocation id,
                                      MultiblockMachineBuilder<?> internal) {
        super(id);
        this.internal = internal;
        this.dummyBuilder = true;
    }

    public MultiblockMachineBuilderJS generator(boolean generator) {
        internal.generator(generator);
        return this;
    }

    public MultiblockMachineBuilderJS pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        internal.pattern(pattern);
        return this;
    }

    public MultiblockMachineBuilderJS allowFlip(boolean allowFlip) {
        internal.allowFlip(allowFlip);
        return this;
    }

    public MultiblockMachineBuilderJS partSorter(Comparator<MultiblockPartMachine> partSorter) {
        internal.partSorter(partSorter);
        return this;
    }

    public MultiblockMachineBuilderJS partAppearance(@Nullable TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        internal.partAppearance(partAppearance);
        return this;
    }

    public MultiblockMachineBuilderJS recoveryItems(Supplier<ItemLike[]> items) {
        internal.recoveryItems(items);
        return this;
    }

    public MultiblockMachineBuilderJS recoveryStacks(Supplier<ItemStack[]> stacks) {
        internal.recoveryStacks(stacks);
        return this;
    }

    public MultiblockMachineBuilderJS blockEntityFactory(MachineInstanceFactory machine) {
        internal.instanceFactory(machine);
        return this;
    }

    public MultiblockMachineBuilderJS model(@Nullable MachineBuilder.ModelInitializer model) {
        internal.model(model);
        return this;
    }

    public MultiblockMachineBuilderJS blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        internal.blockModel(blockModel);
        return this;
    }

    public MultiblockMachineBuilderJS shape(VoxelShape shape) {
        internal.shape(shape);
        return this;
    }

    public MultiblockMachineBuilderJS multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                boolean multiBlockXEIPreview) {
        internal.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
        return this;
    }

    public MultiblockMachineBuilderJS rotationState(RotationState rotationState) {
        internal.rotationState(rotationState);
        return this;
    }

    public MultiblockMachineBuilderJS hasBER(boolean hasBER) {
        internal.hasBER(hasBER);
        return this;
    }

    public MultiblockMachineBuilderJS recipeTypes(GTRecipeType... recipeTypes) {
        for (var type : recipeTypes) {
            recipeType(type);
        }
        return this;
    }

    public MultiblockMachineBuilderJS recipeType(GTRecipeType recipeTypes) {
        internal.recipeType(() -> recipeTypes);
        return this;
    }

    public MultiblockMachineBuilderJS tier(int tier) {
        internal.tier(tier);
        return this;
    }

    public MultiblockMachineBuilderJS recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        internal.recipeOutputLimits(map);
        return this;
    }

    public MultiblockMachineBuilderJS addOutputLimit(RecipeCapability<?> capability, int limit) {
        internal.addOutputLimit(capability, limit);
        return this;
    }

    public MultiblockMachineBuilderJS simpleModel(ResourceLocation model) {
        internal.simpleModel(model);
        return this;
    }

    public MultiblockMachineBuilderJS defaultModel() {
        internal.defaultModel();
        return this;
    }

    public MultiblockMachineBuilderJS tieredHullModel(ResourceLocation model) {
        internal.tieredHullModel(model);
        return this;
    }

    public MultiblockMachineBuilderJS overlayTieredHullModel(ResourceLocation overlayModel) {
        internal.overlayTieredHullModel(overlayModel);
        return this;
    }

    public MultiblockMachineBuilderJS colorOverlayTieredHullModel(ResourceLocation overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return this;
    }

    public MultiblockMachineBuilderJS colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                  @Nullable ResourceLocation pipeOverlay,
                                                                  @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public MultiblockMachineBuilderJS workableTieredHullModel(ResourceLocation workableModel) {
        internal.workableTieredHullModel(workableModel);
        return this;
    }

    public MultiblockMachineBuilderJS simpleGeneratorModel(ResourceLocation workableModel) {
        internal.simpleGeneratorModel(workableModel);
        return this;
    }

    public MultiblockMachineBuilderJS workableCasingModel(ResourceLocation baseCasing,
                                                          ResourceLocation overlayModel) {
        internal.workableCasingModel(baseCasing, overlayModel);
        return this;
    }

    public MultiblockMachineBuilderJS sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                              ResourceLocation workableModel) {
        internal.sidedOverlayCasingModel(baseCasing, workableModel);
        return this;
    }

    public MultiblockMachineBuilderJS sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                               ResourceLocation workableModel) {
        internal.sidedWorkableCasingModel(baseCasing, workableModel);
        return this;
    }

    public MultiblockMachineBuilderJS overlaySteamHullModel(ResourceLocation overlayModel) {
        internal.overlaySteamHullModel(overlayModel);
        return this;
    }

    public MultiblockMachineBuilderJS colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                 @Nullable ResourceLocation pipeOverlay,
                                                                 @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public MultiblockMachineBuilderJS colorOverlaySteamHullModel(ResourceLocation overlay) {
        internal.colorOverlaySteamHullModel(overlay);
        return this;
    }

    public MultiblockMachineBuilderJS workableSteamHullModel(boolean isHighPressure,
                                                             ResourceLocation workableModel) {
        internal.workableSteamHullModel(isHighPressure, workableModel);
        return this;
    }

    public MultiblockMachineBuilderJS tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        internal.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public MultiblockMachineBuilderJS appearance(@Nullable Supplier<BlockState> state) {
        internal.appearance(state);
        return this;
    }

    public MultiblockMachineBuilderJS appearanceBlock(Supplier<? extends Block> block) {
        internal.appearanceBlock(block);
        return this;
    }

    public MultiblockMachineBuilderJS langValue(@Nullable String langValue) {
        internal.langValue(langValue);
        return this;
    }

    public MultiblockMachineBuilderJS tooltips(Component... components) {
        internal.tooltips(components);
        return this;
    }

    public MultiblockMachineBuilderJS conditionalTooltip(Component component, Supplier<Boolean> condition) {
        internal.conditionalTooltip(component, condition.get());
        return this;
    }

    public MultiblockMachineBuilderJS conditionalTooltip(Component component, boolean condition) {
        internal.conditionalTooltip(component, condition);
        return this;
    }

    public MultiblockMachineBuilderJS abilities(PartAbility... abilities) {
        internal.abilities(abilities);
        return this;
    }

    public MultiblockMachineBuilderJS paintingColor(int paintingColor) {
        internal.paintingColor(() -> paintingColor);
        return this;
    }

    public MultiblockMachineBuilderJS recipeModifier(RecipeModifier recipeModifier) {
        internal.recipeModifier(recipeModifier);
        return this;
    }

    public MultiblockMachineBuilderJS recipeModifier(RecipeModifier recipeModifier,
                                                     boolean alwaysTryModifyRecipe) {
        internal.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
        return this;
    }

    public MultiblockMachineBuilderJS recipeModifiers(RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(recipeModifiers);
        return this;
    }

    public MultiblockMachineBuilderJS recipeModifiers(boolean alwaysTryModifyRecipe,
                                                      RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
        return this;
    }

    public MultiblockMachineBuilderJS noRecipeModifier() {
        internal.noRecipeModifier();
        return this;
    }

    public MultiblockMachineBuilderJS alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        internal.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this;
    }

    public MultiblockMachineBuilderJS beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        internal.beforeWorking(beforeWorking);
        return this;
    }

    public MultiblockMachineBuilderJS onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        internal.onWorking(onWorking);
        return this;
    }

    public MultiblockMachineBuilderJS onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        internal.onWaiting(onWaiting);
        return this;
    }

    public MultiblockMachineBuilderJS afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        internal.afterWorking(afterWorking);
        return this;
    }

    public MultiblockMachineBuilderJS regressWhenWaiting(boolean regressWhenWaiting) {
        internal.regressWhenWaiting(regressWhenWaiting);
        return this;
    }

    public MultiblockMachineBuilderJS allowExtendedFacing(boolean allowExtendedFacing) {
        internal.allowExtendedFacing(allowExtendedFacing);
        return this;
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(internal, object);
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        final ResourceLocation id = this.id;
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/")));
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        if (object != null && object.getLangValue() != null) {
            lang.add(id.getNamespace(), object.getDescriptionId(), object.getLangValue());
        }
    }

    public @Nullable MultiblockMachineDefinition createObject() {
        internal.register();
        return null;
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
