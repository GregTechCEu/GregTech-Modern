package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

@SuppressWarnings("unused")
public class MachineBuilderJS<SELF extends MachineBuilderJS<SELF>> extends BuilderBase<MachineDefinition>
                             implements IMachineBuilderKJS {

    @HideFromJS
    public final MachineBuilder<?, ?, ?> internal;

    @HideFromJS
    private @Nullable MachineEntry<?> entry = null;

    private @Nullable String langValue;

    public MachineBuilderJS(ResourceLocation id, MachineBuilder<?, ?, ?> internal) {
        super(id);
        this.internal = internal;
        this.dummyBuilder = true;

        defaultLang();
    }

    @SuppressWarnings("unchecked")
    protected SELF getThis() {
        return (SELF) this;
    }

    public SELF shape(VoxelShape shape) {
        internal.shape(shape);
        return getThis();
    }

    public SELF rotationState(RotationState rotationState) {
        internal.rotationState(rotationState);
        return getThis();
    }

    public SELF allowExtendedFacing(boolean allowExtendedFacing) {
        internal.allowExtendedFacing(allowExtendedFacing);
        return getThis();
    }

    public SELF hasBER(boolean hasBER) {
        internal.hasBER(hasBER);
        return getThis();
    }

    public SELF tier(int tier) {
        internal.tier(tier);
        return getThis();
    }

    public SELF recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        internal.recipeOutputLimits(recipeOutputLimits);
        return getThis();
    }

    public SELF paintingColor(IntSupplier paintingColor) {
        internal.paintingColor(paintingColor);
        return getThis();
    }

    public SELF tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        internal.tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    public SELF beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        internal.beforeWorking(beforeWorking);
        return getThis();
    }

    public SELF onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        internal.onWorking(onWorking);
        return getThis();
    }

    public SELF onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        internal.onWaiting(onWaiting);
        return getThis();
    }

    public SELF afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        internal.afterWorking(afterWorking);
        return getThis();
    }

    public SELF regressWhenWaiting(boolean regressWhenWaiting) {
        internal.regressWhenWaiting(regressWhenWaiting);
        return getThis();
    }

    public SELF allowCoverOnFront(boolean allowCoverOnFront) {
        internal.allowCoverOnFront(allowCoverOnFront);
        return getThis();
    }

    public SELF appearance(Supplier<BlockState> appearance) {
        internal.appearance(appearance);
        return getThis();
    }

    public SELF defaultLang() {
        langValue = RegistrateLangProvider.toEnglishName(id.getPath());
        return getThis();
    }

    public SELF lang(String name) {
        return langValue(name);
    }

    public SELF langValue(String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public SELF recipeType(GTResourceLocation type) {
        internal.recipeType(DeferredHolder.create(Registries.RECIPE_TYPE, type.wrapped()));
        return getThis();
    }

    @SuppressWarnings("unchecked")
    public final SELF recipeTypes(GTResourceLocation... types) {
        internal.recipeTypes(Arrays.stream(types)
                .map(v -> DeferredHolder.create(Registries.RECIPE_TYPE, v.wrapped()))
                .toArray(DeferredHolder[]::new));
        return getThis();
    }

    public SELF simpleModel(ResourceLocation modelName) {
        internal.simpleModel(modelName);
        return getThis();
    }

    public SELF defaultModel() {
        internal.defaultModel();
        return getThis();
    }

    public SELF tieredHullModel(ResourceLocation model) {
        internal.tieredHullModel(model);
        return getThis();
    }

    public SELF overlayTieredHullModel(String name) {
        internal.overlayTieredHullModel(name);
        return getThis();
    }

    public SELF overlayTieredHullModel(ResourceLocation overlayModel) {
        internal.overlayTieredHullModel(overlayModel);
        return getThis();
    }

    public SELF colorOverlayTieredHullModel(String overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return getThis();
    }

    public SELF colorOverlayTieredHullModel(String overlay,
                                            @Nullable String pipeOverlay,
                                            @Nullable String emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public SELF colorOverlayTieredHullModel(ResourceLocation overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return getThis();
    }

    public SELF colorOverlayTieredHullModel(ResourceLocation overlay,
                                            @Nullable ResourceLocation pipeOverlay,
                                            @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public SELF overlaySteamHullModel(String name) {
        internal.overlaySteamHullModel(name);
        return getThis();
    }

    public SELF overlaySteamHullModel(ResourceLocation overlayModel) {
        internal.overlaySteamHullModel(overlayModel);
        return getThis();
    }

    public SELF colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public SELF colorOverlaySteamHullModel(String overlay,
                                           @Nullable String pipeOverlay,
                                           @Nullable String emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public SELF colorOverlaySteamHullModel(String overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable String emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public SELF colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public SELF colorOverlaySteamHullModel(ResourceLocation overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public SELF workableTieredHullModel(ResourceLocation workableModel) {
        internal.workableTieredHullModel(workableModel);
        return getThis();
    }

    public SELF simpleGeneratorModel(ResourceLocation workableModel) {
        internal.simpleGeneratorModel(workableModel);
        return getThis();
    }

    public SELF workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        internal.workableSteamHullModel(isHighPressure, workableModel);
        return getThis();
    }

    public SELF workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        internal.workableCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public SELF sidedOverlayCasingModel(ResourceLocation baseCasing,
                                        ResourceLocation workableModel) {
        internal.sidedOverlayCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public SELF sidedWorkableCasingModel(ResourceLocation baseCasing,
                                         ResourceLocation workableModel) {
        internal.sidedWorkableCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public SELF appearanceBlock(Supplier<? extends Block> block) {
        internal.appearanceBlock(block);
        return getThis();
    }

    public SELF tooltips(List<? extends Component> components) {
        internal.tooltips(components);
        return getThis();
    }

    public SELF conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public SELF conditionalTooltip(Component component, boolean condition) {
        internal.conditionalTooltip(component, condition);
        return getThis();
    }

    public SELF abilities(PartAbility... abilities) {
        internal.abilities(abilities);
        return getThis();
    }

    public SELF themeId(Function<Integer, String> themeId) {
        internal.themeId(themeId);
        return getThis();
    }

    public <T extends Comparable<T>> SELF modelProperty(Property<T> property,
                                                        @Nullable T defaultValue) {
        internal.modelProperty(property, defaultValue);
        return getThis();
    }

    @Tolerate
    public SELF modelProperties(Map<Property<?>, ? extends Comparable<?>> modelProperties) {
        internal.modelProperties(modelProperties);
        return getThis();
    }

    public SELF removeModelProperty(Property<?> property) {
        internal.removeModelProperty(property);
        return getThis();
    }

    public SELF clearModelProperties() {
        internal.clearModelProperties();
        return getThis();
    }

    public SELF addRecipeModifier(RecipeModifier recipeModifier) {
        internal.addRecipeModifier(recipeModifier);
        return getThis();
    }

    public SELF recipeModifier(RecipeModifier recipeModifier) {
        internal.recipeModifier(recipeModifier);
        return getThis();
    }

    public SELF alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        internal.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return getThis();
    }

    public SELF recipeModifiers(RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(recipeModifiers);
        return getThis();
    }

    public SELF noRecipeModifier() {
        internal.noRecipeModifier();
        return getThis();
    }

    public SELF addOutputLimit(RecipeCapability<?> capability, int limit) {
        internal.addOutputLimit(capability, limit);
        return getThis();
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(internal, Objects.requireNonNull(entry).value());
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        final ResourceLocation id = this.id;
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/")));
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        if (object != null && langValue != null) {
            lang.add(id.getNamespace(), object.getDescriptionId(), langValue);
        }
    }

    @Override
    public @Nullable MachineDefinition createObject() {
        entry = internal.register();
        return null;
    }
}
