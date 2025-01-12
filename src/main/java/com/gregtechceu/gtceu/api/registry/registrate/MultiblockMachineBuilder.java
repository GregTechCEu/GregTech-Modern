package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
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
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

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

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

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
    private boolean generator;
    @Setter
    private Function<MultiblockMachineDefinition, BlockPattern> pattern;
    private final List<Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>>> shapeInfos = new ArrayList<>();
    /**
     * Whether this multi can be rotated or face upwards.
     */
    @Setter
    private boolean allowExtendedFacing = true;
    /**
     * Set this to false only if your multiblock is set up such that it could have a wall-shared controller.
     */
    @Setter
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    @Setter
    private Comparator<IMultiPart> partSorter = (a, b) -> 0;
    @Setter
    private TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Getter
    @Setter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay = (m, l) -> {};

    protected MultiblockMachineBuilder(Registrate registrate, String name,
                                       Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, MultiblockMachineDefinition::createDefinition, metaMachine::apply, blockFactory,
                itemFactory, blockEntityFactory);
    }

    public static MultiblockMachineBuilder createMulti(Registrate registrate, String name,
                                                       Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                                       BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                                       BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                       TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MultiblockMachineBuilder(registrate, name, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
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
    public MultiblockMachineBuilder definition(Function<ResourceLocation, MultiblockMachineDefinition> definition) {
        return (MultiblockMachineBuilder) super.definition(definition);
    }

    @Override
    public MultiblockMachineBuilder machine(Function<IMachineBlockEntity, MetaMachine> machine) {
        return (MultiblockMachineBuilder) super.machine(machine);
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
    public MultiblockMachineBuilder multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                              boolean multiBlockXEIPreview) {
        return (MultiblockMachineBuilder) super.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
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
    public MultiblockMachineBuilder tieredHullRenderer(ResourceLocation model) {
        return (MultiblockMachineBuilder) super.tieredHullRenderer(model);
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

    @Override
    public MultiblockMachineBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel,
                                                           boolean tint) {
        return (MultiblockMachineBuilder) super.workableCasingRenderer(baseCasing, overlayModel, tint);
    }

    @Override
    public MultiblockMachineBuilder sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel,
                                                                boolean tint) {
        return (MultiblockMachineBuilder) super.sidedWorkableCasingRenderer(basePath, overlayModel, tint);
    }

    @Override
    public MultiblockMachineBuilder sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel) {
        return (MultiblockMachineBuilder) super.sidedWorkableCasingRenderer(basePath, overlayModel);
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
    public MultiblockMachineBuilder conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return conditionalTooltip(component, condition.get());
    }

    @Override
    public MultiblockMachineBuilder conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips(component);
        return this;
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
    public MultiblockMachineBuilder regressWhenWaiting(boolean dampingWhenWaiting) {
        return (MultiblockMachineBuilder) super.regressWhenWaiting(dampingWhenWaiting);
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
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        if (langValue() != null) {
            lang.add(GTCEu.MOD_ID, value.getDescriptionId(), value.getLangValue());
        }
    }

    @Override
    @HideFromJS
    public MultiblockMachineDefinition register() {
        var definition = (MultiblockMachineDefinition) super.register();
        definition.setGenerator(generator);
        if (pattern == null) {
            throw new IllegalStateException("missing pattern while creating multiblock " + name);
        }
        definition.setPatternFactory(SupplierMemoizer.memoize(() -> pattern.apply(definition)));
        definition.setShapes(() -> shapeInfos.stream().map(factory -> factory.apply(definition))
                .flatMap(Collection::stream).toList());
        definition.setAllowExtendedFacing(allowExtendedFacing);
        definition.setAllowFlip(allowFlip);
        if (!recoveryItems.isEmpty()) {
            definition.setRecoveryItems(
                    () -> recoveryItems.stream().map(Supplier::get).flatMap(Arrays::stream).toArray(ItemStack[]::new));
        }
        definition.setPartSorter(partSorter);
        if (partAppearance == null) {
            partAppearance = (controller, part, side) -> definition.getAppearance().get();
        }
        definition.setPartAppearance(partAppearance);
        definition.setAdditionalDisplay(additionalDisplay);
        return value = definition;
    }
}
