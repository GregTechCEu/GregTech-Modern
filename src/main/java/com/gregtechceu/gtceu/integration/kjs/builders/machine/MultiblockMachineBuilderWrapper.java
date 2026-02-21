package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

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

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
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
public class MultiblockMachineBuilderWrapper extends BuilderBase<MultiblockMachineDefinition>
                                             implements IMachineBuilderKJS {

    private final MultiblockMachineBuilder<MultiblockMachineDefinition, ?> internal;

    public MultiblockMachineBuilderWrapper(ResourceLocation id,
                                           MultiblockMachineBuilder<MultiblockMachineDefinition, ?> internal) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.internal = internal;
        this.dummyBuilder = true;
    }

    public MultiblockMachineBuilderWrapper generator(boolean generator) {
        internal.generator(generator);
        return this;
    }

    public MultiblockMachineBuilderWrapper pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
        internal.pattern(pattern);
        return this;
    }

    public MultiblockMachineBuilderWrapper allowFlip(boolean allowFlip) {
        internal.allowFlip(allowFlip);
        return this;
    }

    public MultiblockMachineBuilderWrapper partSorter(Comparator<IMultiPart> partSorter) {
        internal.partSorter(partSorter);
        return this;
    }

    public MultiblockMachineBuilderWrapper partAppearance(@Nullable TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance) {
        internal.partAppearance(partAppearance);
        return this;
    }

    public MultiblockMachineBuilderWrapper additionalDisplay(BiConsumer<IMultiController, List<Component>> additionalDisplay) {
        internal.additionalDisplay(additionalDisplay);
        return this;
    }

    public MultiblockMachineBuilderWrapper shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        internal.shapeInfo(shape);
        return this;
    }

    public MultiblockMachineBuilderWrapper shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        internal.shapeInfos(shapes);
        return this;
    }

    public MultiblockMachineBuilderWrapper recoveryItems(Supplier<ItemLike[]> items) {
        internal.recoveryItems(items);
        return this;
    }

    public MultiblockMachineBuilderWrapper recoveryStacks(Supplier<ItemStack[]> stacks) {
        internal.recoveryStacks(stacks);
        return this;
    }

    public MultiblockMachineBuilderWrapper definition(Function<ResourceLocation, MultiblockMachineDefinition> definition) {
        internal.definition(definition);
        return this;
    }

    public MultiblockMachineBuilderWrapper machine(Function<IMachineBlockEntity, MetaMachine> machine) {
        internal.machine(machine);
        return this;
    }

    public MultiblockMachineBuilderWrapper model(@Nullable MachineBuilder.ModelInitializer model) {
        internal.model(model);
        return this;
    }

    public MultiblockMachineBuilderWrapper blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        internal.blockModel(blockModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper shape(VoxelShape shape) {
        internal.shape(shape);
        return this;
    }

    public MultiblockMachineBuilderWrapper multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                     boolean multiBlockXEIPreview) {
        internal.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
        return this;
    }

    public MultiblockMachineBuilderWrapper rotationState(RotationState rotationState) {
        internal.rotationState(rotationState);
        return this;
    }

    public MultiblockMachineBuilderWrapper hasBER(boolean hasBER) {
        internal.hasBER(hasBER);
        return this;
    }

    public MultiblockMachineBuilderWrapper blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        internal.blockProp(blockProp);
        return this;
    }

    public MultiblockMachineBuilderWrapper itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        internal.itemProp(itemProp);
        return this;
    }

    public MultiblockMachineBuilderWrapper blockBuilder(@Nullable Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        internal.blockBuilder(blockBuilder);
        return this;
    }

    public MultiblockMachineBuilderWrapper itemBuilder(@Nullable Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        internal.itemBuilder(itemBuilder);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeTypes(GTRecipeType... recipeTypes) {
        internal.recipeTypes(recipeTypes);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeType(GTRecipeType recipeTypes) {
        internal.recipeType(recipeTypes);
        return this;
    }

    public MultiblockMachineBuilderWrapper tier(int tier) {
        internal.tier(tier);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        internal.recipeOutputLimits(map);
        return this;
    }

    public MultiblockMachineBuilderWrapper addOutputLimit(RecipeCapability<?> capability, int limit) {
        internal.addOutputLimit(capability, limit);
        return this;
    }

    public MultiblockMachineBuilderWrapper itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        internal.itemColor(itemColor);
        return this;
    }

    public MultiblockMachineBuilderWrapper simpleModel(ResourceLocation model) {
        internal.simpleModel(model);
        return this;
    }

    public MultiblockMachineBuilderWrapper defaultModel() {
        internal.defaultModel();
        return this;
    }

    public MultiblockMachineBuilderWrapper tieredHullModel(ResourceLocation model) {
        internal.tieredHullModel(model);
        return this;
    }

    public MultiblockMachineBuilderWrapper overlayTieredHullModel(ResourceLocation overlayModel) {
        internal.overlayTieredHullModel(overlayModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper colorOverlayTieredHullModel(ResourceLocation overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return this;
    }

    public MultiblockMachineBuilderWrapper colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                       @Nullable ResourceLocation pipeOverlay,
                                                                       @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public MultiblockMachineBuilderWrapper workableTieredHullModel(ResourceLocation workableModel) {
        internal.workableTieredHullModel(workableModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper simpleGeneratorModel(ResourceLocation workableModel) {
        internal.simpleGeneratorModel(workableModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper workableCasingModel(ResourceLocation baseCasing,
                                                               ResourceLocation overlayModel) {
        internal.workableCasingModel(baseCasing, overlayModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                                   ResourceLocation workableModel) {
        internal.sidedOverlayCasingModel(baseCasing, workableModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                                    ResourceLocation workableModel) {
        internal.sidedWorkableCasingModel(baseCasing, workableModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper overlaySteamHullModel(ResourceLocation overlayModel) {
        internal.overlaySteamHullModel(overlayModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                      @Nullable ResourceLocation pipeOverlay,
                                                                      @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public MultiblockMachineBuilderWrapper colorOverlaySteamHullModel(ResourceLocation overlay) {
        internal.colorOverlaySteamHullModel(overlay);
        return this;
    }

    public MultiblockMachineBuilderWrapper workableSteamHullModel(boolean isHighPressure,
                                                                  ResourceLocation workableModel) {
        internal.workableSteamHullModel(isHighPressure, workableModel);
        return this;
    }

    public MultiblockMachineBuilderWrapper tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        internal.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public MultiblockMachineBuilderWrapper appearance(@Nullable Supplier<BlockState> state) {
        internal.appearance(state);
        return this;
    }

    public MultiblockMachineBuilderWrapper appearanceBlock(Supplier<? extends Block> block) {
        internal.appearanceBlock(block);
        return this;
    }

    public MultiblockMachineBuilderWrapper langValue(@Nullable String langValue) {
        internal.langValue(langValue);
        return this;
    }

    public MultiblockMachineBuilderWrapper tooltips(Component... components) {
        internal.tooltips(components);
        return this;
    }

    public MultiblockMachineBuilderWrapper conditionalTooltip(Component component, Supplier<Boolean> condition) {
        internal.conditionalTooltip(component, condition.get());
        return this;
    }

    public MultiblockMachineBuilderWrapper conditionalTooltip(Component component, boolean condition) {
        internal.conditionalTooltip(component, condition);
        return this;
    }

    public MultiblockMachineBuilderWrapper abilities(PartAbility... abilities) {
        internal.abilities(abilities);
        return this;
    }

    public MultiblockMachineBuilderWrapper paintingColor(int paintingColor) {
        internal.paintingColor(paintingColor);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeModifier(RecipeModifier recipeModifier) {
        internal.recipeModifier(recipeModifier);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeModifier(RecipeModifier recipeModifier,
                                                          boolean alwaysTryModifyRecipe) {
        internal.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeModifiers(RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(recipeModifiers);
        return this;
    }

    public MultiblockMachineBuilderWrapper recipeModifiers(boolean alwaysTryModifyRecipe,
                                                           RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
        return this;
    }

    public MultiblockMachineBuilderWrapper noRecipeModifier() {
        internal.noRecipeModifier();
        return this;
    }

    public MultiblockMachineBuilderWrapper alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        internal.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this;
    }

    public MultiblockMachineBuilderWrapper beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        internal.beforeWorking(beforeWorking);
        return this;
    }

    public MultiblockMachineBuilderWrapper onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        internal.onWorking(onWorking);
        return this;
    }

    public MultiblockMachineBuilderWrapper onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        internal.onWaiting(onWaiting);
        return this;
    }

    public MultiblockMachineBuilderWrapper afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        internal.afterWorking(afterWorking);
        return this;
    }

    public MultiblockMachineBuilderWrapper regressWhenWaiting(boolean regressWhenWaiting) {
        internal.regressWhenWaiting(regressWhenWaiting);
        return this;
    }

    public MultiblockMachineBuilderWrapper editableUI(@Nullable EditableMachineUI editableUI) {
        internal.editableUI(editableUI);
        return this;
    }

    public MultiblockMachineBuilderWrapper onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        internal.onBlockEntityRegister(onBlockEntityRegister);
        return this;
    }

    public MultiblockMachineBuilderWrapper allowExtendedFacing(boolean allowExtendedFacing) {
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

    public MultiblockMachineDefinition createObject() {
        return internal.register();
    }

    public static MultiblockMachineBuilderWrapper createKJSMulti(ResourceLocation id) {
        var baseBuilder = new MultiblockMachineBuilder<>(GTRegistrate.createIgnoringListenerErrors(id.getNamespace()),
                id.getPath(),
                WorkableElectricMultiblockMachine::new,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
        return new MultiblockMachineBuilderWrapper(id, baseBuilder);
    }

    public static MultiblockMachineBuilderWrapper createKJSMulti(ResourceLocation id,
                                                                 KJSTieredMachineBuilder.CreationFunction<? extends MultiblockControllerMachine> machine) {
        var baseBuilder = new MultiblockMachineBuilder<>(GTRegistrate.createIgnoringListenerErrors(id.getNamespace()),
                id.getPath(),
                machine::create,
                MetaMachineBlock::new,
                MetaMachineItem::new,
                MetaMachineBlockEntity::new);
        return new MultiblockMachineBuilderWrapper(id, baseBuilder);
    }
}
