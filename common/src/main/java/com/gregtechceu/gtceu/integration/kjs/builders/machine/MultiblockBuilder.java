package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.*;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
@SuppressWarnings("unused")
public class MultiblockBuilder extends MachineBuilder {
    public transient MultiblockMachineBuilder builder;

    public MultiblockBuilder(ResourceLocation i) {
        super(i);
        this.builder = GTRegistries.REGISTRATE.multiblock(name, WorkableElectricMultiblockMachine::new);
    }

    public MultiblockBuilder shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        builder.shapeInfo(shape);
        return this;
    }

    public MultiblockBuilder shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        builder.shapeInfos(shapes);
        return this;
    }

    public MultiblockBuilder recoveryItems(Supplier<ItemLike[]> items) {
        builder.recoveryItems(items);
        return this;
    }

    public MultiblockBuilder recoveryStacks(Supplier<ItemStack[]> items) {
        builder.recoveryStacks(items);
        return this;
    }

    public MultiblockBuilder renderer(@Nullable Supplier<IRenderer> renderer) {
        builder.renderer(renderer);
        return this;
    }

    public MultiblockBuilder shape(VoxelShape shape) {
        builder.shape(shape);
        return this;
    }

    public MultiblockBuilder rotationState(RotationState rotationState) {
        builder.rotationState(rotationState);
        return this;
    }

    public MultiblockBuilder hasTESR(boolean hasTESR) {
        builder.hasTESR(hasTESR);
        return this;
    }

    public MultiblockBuilder blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        builder.blockProp(blockProp);
        return this;
    }

    public MultiblockBuilder itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        builder.itemProp(itemProp);
        return this;
    }

    public MultiblockBuilder blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        builder.blockBuilder(blockBuilder);
        return this;
    }

    public MultiblockBuilder itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        builder.itemBuilder(itemBuilder);
        return this;
    }

    @Override
    public MultiblockBuilder recipeType(String recipeType) {
        builder.recipeType(recipeType);
        return this;
    }

    public MultiblockBuilder tier(int tier) {
        builder.tier(tier);
        return this;
    }

    public MultiblockBuilder itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        builder.itemColor(itemColor);
        return this;
    }

    public MultiblockBuilder overclockingLogic(OverclockingLogic overclockingLogic) {
        builder.overclockingLogic(overclockingLogic);
        return this;
    }

    public MultiblockBuilder modelRenderer(Supplier<ResourceLocation> model) {
        builder.modelRenderer(model);
        return this;
    }

    public MultiblockBuilder defaultModelRenderer() {
        builder.defaultModelRenderer();
        return this;
    }

    public MultiblockBuilder overlayTieredHullRenderer(String name) {
        builder.overlayTieredHullRenderer(name);
        return this;
    }

    public MultiblockBuilder workableTieredHullRenderer(ResourceLocation workableModel) {
        builder.workableTieredHullRenderer(workableModel);
        return this;
    }

    public MultiblockBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel) {
        builder.workableCasingRenderer(baseCasing, overlayModel);
        return this;
    }

    public MultiblockBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation overlayModel, boolean tint) {
        builder.workableCasingRenderer(baseCasing, overlayModel, tint);
        return this;
    }

    public MultiblockBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        builder.tooltipBuilder(tooltipBuilder);
        return this;
    }

    public MultiblockBuilder appearance(Supplier<BlockState> state) {
        builder.appearance(state);
        return this;
    }

    public MultiblockBuilder appearanceBlock(Supplier<? extends Block> block) {
        builder.appearanceBlock(block);
        return this;
    }

    public MultiblockBuilder langValue(String langValue) {
        builder.langValue(langValue);
        return this;
    }

    public MultiblockBuilder overlaySteamHullRenderer(String name) {
        builder.overlaySteamHullRenderer(name);
        return this;
    }

    public MultiblockBuilder workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
        builder.workableSteamHullRenderer(isHighPressure, workableModel);
        return this;
    }

    public MultiblockBuilder tooltips(Component... components) {
        builder.tooltips(components);
        return this;
    }

    public MultiblockBuilder abilities(PartAbility... abilities) {
        builder.abilities(abilities);
        return this;
    }

    public MultiblockBuilder paintingColor(int paintingColor) {
        builder.paintingColor(paintingColor);
        return this;
    }

    public MultiblockBuilder pattern(Function<MultiblockMachineDefinition, BlockPattern> patternFunction) {
        builder.pattern(patternFunction);
        return this;
    }

    @Override
    public Block createObject() {
        var val = this.builder.register();
        return null;
    }
}
