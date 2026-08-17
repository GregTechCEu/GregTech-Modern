package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
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
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.*;

@SuppressWarnings("unused")
public class KJSMultiblockMachineBuilderWrapper extends BuilderBase<MachineDefinition>
                                                implements IMachineBuilderKJS {

    private final MultiblockMachineBuilder<MultiblockMachineDefinition, ?, ?> internal;

    public KJSMultiblockMachineBuilderWrapper(ResourceLocation id,
                                              MultiblockMachineBuilder<MultiblockMachineDefinition, ?, ?> internal) {
        super(id);
        this.internal = internal;
        this.dummyBuilder = true;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    public KJSMultiblockMachineBuilderWrapper generator(boolean generator) {
        internal.generator(generator);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper pattern(Function<MultiblockMachineDefinition, IBlockPattern> pattern) {
        internal.pattern(pattern);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper allowFlip(boolean allowFlip) {
        internal.allowFlip(allowFlip);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper partSorter(Comparator<MultiblockPartMachine> partSorter) {
        internal.partSorter(partSorter);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper partAppearance(@Nullable TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance) {
        internal.partAppearance(partAppearance);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recoveryItems(Supplier<ItemLike[]> items) {
        internal.recoveryItems(items);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recoveryStacks(Supplier<ItemStack[]> stacks) {
        internal.recoveryStacks(stacks);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper blockEntityFactory(MachineInstanceFactory machine) {
        internal.instanceFactory(machine);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper model(@Nullable MachineBuilder.ModelInitializer model) {
        internal.model(model);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        internal.blockModel(blockModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper shape(VoxelShape shape) {
        internal.shape(shape);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                        boolean multiBlockXEIPreview) {
        internal.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper rotationState(RotationState rotationState) {
        internal.rotationState(rotationState);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper hasBER(boolean hasBER) {
        internal.hasBER(hasBER);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        internal.blockProp(blockProp);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        internal.itemProp(itemProp);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper blockBuilder(@Nullable Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        internal.blockBuilder(blockBuilder);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper itemBuilder(@Nullable Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        internal.itemBuilder(itemBuilder);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeTypes(GTRecipeType... recipeTypes) {
        internal.recipeTypes(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeType(GTRecipeType recipeTypes) {
        internal.recipeType(recipeTypes);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper tier(int tier) {
        internal.tier(tier);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        internal.recipeOutputLimits(map);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper addOutputLimit(RecipeCapability<?> capability, int limit) {
        internal.addOutputLimit(capability, limit);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        internal.itemColor(itemColor);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper simpleModel(ResourceLocation model) {
        internal.simpleModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper defaultModel() {
        internal.defaultModel();
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper tieredHullModel(ResourceLocation model) {
        internal.tieredHullModel(model);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper overlayTieredHullModel(ResourceLocation overlayModel) {
        internal.overlayTieredHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper colorOverlayTieredHullModel(ResourceLocation overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                          @Nullable ResourceLocation pipeOverlay,
                                                                          @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper workableTieredHullModel(ResourceLocation workableModel) {
        internal.workableTieredHullModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper simpleGeneratorModel(ResourceLocation workableModel) {
        internal.simpleGeneratorModel(workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper workableCasingModel(ResourceLocation baseCasing,
                                                                  ResourceLocation overlayModel) {
        internal.workableCasingModel(baseCasing, overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                                      ResourceLocation workableModel) {
        internal.sidedOverlayCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                                       ResourceLocation workableModel) {
        internal.sidedWorkableCasingModel(baseCasing, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper overlaySteamHullModel(ResourceLocation overlayModel) {
        internal.overlaySteamHullModel(overlayModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                         @Nullable ResourceLocation pipeOverlay,
                                                                         @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper colorOverlaySteamHullModel(ResourceLocation overlay) {
        internal.colorOverlaySteamHullModel(overlay);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper workableSteamHullModel(boolean isHighPressure,
                                                                     ResourceLocation workableModel) {
        internal.workableSteamHullModel(isHighPressure, workableModel);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        internal.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper appearance(@Nullable Supplier<BlockState> state) {
        internal.appearance(state);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper appearanceBlock(Supplier<? extends Block> block) {
        internal.appearanceBlock(block);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper langValue(@Nullable String langValue) {
        internal.langValue(langValue);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper tooltips(Component... components) {
        internal.tooltips(components);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper conditionalTooltip(Component component, Supplier<Boolean> condition) {
        internal.conditionalTooltip(component, condition.get());
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper conditionalTooltip(Component component, boolean condition) {
        internal.conditionalTooltip(component, condition);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper abilities(PartAbility... abilities) {
        internal.abilities(abilities);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper paintingColor(int paintingColor) {
        internal.paintingColor(paintingColor);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeModifier(RecipeModifier recipeModifier) {
        internal.recipeModifier(recipeModifier);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeModifier(RecipeModifier recipeModifier,
                                                             boolean alwaysTryModifyRecipe) {
        internal.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeModifiers(RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper recipeModifiers(boolean alwaysTryModifyRecipe,
                                                              RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper noRecipeModifier() {
        internal.noRecipeModifier();
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        internal.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        internal.beforeWorking(beforeWorking);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        internal.onWorking(onWorking);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        internal.onWaiting(onWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        internal.afterWorking(afterWorking);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper regressWhenWaiting(boolean regressWhenWaiting) {
        internal.regressWhenWaiting(regressWhenWaiting);
        return this;
    }

    public KJSMultiblockMachineBuilderWrapper allowExtendedFacing(boolean allowExtendedFacing) {
        internal.allowExtendedFacing(allowExtendedFacing);
        return this;
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(internal, object);
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        final ResourceLocation id = this.id;
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
    }

    @Override
    public void generateLang(LangEventJS lang) {
        if (object != null) {
            lang.add(id.getNamespace(), object.getDescriptionId(), object.getLangValue());
        }
    }

    @Override
    public void createAdditionalObjects() {
        internal.register();
    }

    public @Nullable MultiblockMachineDefinition createObject() {
        return null;
    }

    public static KJSMultiblockMachineBuilderWrapper createKJSMulti(ResourceLocation id) {
        var baseBuilder = new MultiblockMachineBuilder<>(GTRegistrate.createIgnoringListenerErrors(id.getNamespace()),
                id.getPath(),
                MetaMachineBlock::new,
                MetaMachineItem::new,
                WorkableElectricMultiblockMachine::new);
        return new KJSMultiblockMachineBuilderWrapper(id, baseBuilder);
    }

    public static KJSMultiblockMachineBuilderWrapper createKJSMulti(ResourceLocation id,
                                                                    MachineInstanceFactory<? extends MultiblockControllerMachine> machine) {
        var baseBuilder = new MultiblockMachineBuilder<>(GTRegistrate.createIgnoringListenerErrors(id.getNamespace()),
                id.getPath(),
                MetaMachineBlock::new,
                MetaMachineItem::new,
                machine);
        return new KJSMultiblockMachineBuilderWrapper(id, baseBuilder);
    }
}
