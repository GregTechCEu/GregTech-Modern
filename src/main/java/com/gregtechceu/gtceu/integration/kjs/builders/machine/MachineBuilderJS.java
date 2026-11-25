package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class MachineBuilderJS<S extends MachineBuilderJS<S>> extends BuilderBase<MachineDefinition>
                             implements IMachineBuilderKJS {

    @HideFromJS
    public final MachineBuilder<?, ?, ?> internal;

    @HideFromJS
    private @Nullable MachineDefinition entry = null;

    private @Nullable String langValue;

    public MachineBuilderJS(ResourceLocation id, MachineBuilder<?, ?, ?> internal) {
        super(id);
        this.internal = internal;

        defaultLang();
    }

    @SuppressWarnings("unchecked")
    protected S getThis() {
        return (S) this;
    }

    public S shape(VoxelShape shape) {
        internal.shape(shape);
        return getThis();
    }

    public S rotationState(RotationState rotationState) {
        internal.rotationState(rotationState);
        return getThis();
    }

    public S allowExtendedFacing(boolean allowExtendedFacing) {
        internal.allowExtendedFacing(allowExtendedFacing);
        return getThis();
    }

    public S hasBER(boolean hasBER) {
        internal.hasBER(hasBER);
        return getThis();
    }

    public S tier(int tier) {
        internal.tier(tier);
        return getThis();
    }

    public S recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        internal.recipeOutputLimits(recipeOutputLimits);
        return getThis();
    }

    public S paintingColor(int paintingColor) {
        internal.paintingColor(paintingColor);
        return getThis();
    }

    public S tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        internal.tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    public S beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        internal.beforeWorking(beforeWorking);
        return getThis();
    }

    public S onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        internal.onWorking(onWorking);
        return getThis();
    }

    public S onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        internal.onWaiting(onWaiting);
        return getThis();
    }

    public S afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        internal.afterWorking(afterWorking);
        return getThis();
    }

    public S regressWhenWaiting(boolean regressWhenWaiting) {
        internal.regressWhenWaiting(regressWhenWaiting);
        return getThis();
    }

    public S allowCoverOnFront(boolean allowCoverOnFront) {
        internal.allowCoverOnFront(allowCoverOnFront);
        return getThis();
    }

    public S appearance(Supplier<BlockState> appearance) {
        internal.appearance(appearance);
        return getThis();
    }

    public S defaultLang() {
        langValue = RegistrateLangProvider.toEnglishName(id.getPath());
        return getThis();
    }

    public S lang(String name) {
        return langValue(name);
    }

    public S langValue(String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public S recipeType(GTResourceLocation type) {
        internal.recipeType(GTRegistries.RECIPE_TYPES.get(type.wrapped()));
        return getThis();
    }

    public final S recipeTypes(GTResourceLocation... types) {
        internal.recipeTypes(Arrays.stream(types).map(v -> GTRegistries.RECIPE_TYPES.get(v.wrapped()))
                .toArray(GTRecipeType[]::new));
        return getThis();
    }

    public S model(MachineBuilder.ModelInitializer model) {
        internal.model(model);
        return getThis();
    }

    public S simpleModel(ResourceLocation modelName) {
        internal.simpleModel(modelName);
        return getThis();
    }

    public S defaultModel() {
        internal.defaultModel();
        return getThis();
    }

    public S tieredHullModel(ResourceLocation model) {
        internal.tieredHullModel(model);
        return getThis();
    }

    public S overlayTieredHullModel(String name) {
        internal.overlayTieredHullModel(name);
        return getThis();
    }

    public S overlayTieredHullModel(ResourceLocation overlayModel) {
        internal.overlayTieredHullModel(overlayModel);
        return getThis();
    }

    public S colorOverlayTieredHullModel(String overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return getThis();
    }

    public S colorOverlayTieredHullModel(String overlay,
                                         @Nullable String pipeOverlay,
                                         @Nullable String emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public S colorOverlayTieredHullModel(ResourceLocation overlay) {
        internal.colorOverlayTieredHullModel(overlay);
        return getThis();
    }

    public S colorOverlayTieredHullModel(ResourceLocation overlay,
                                         @Nullable ResourceLocation pipeOverlay,
                                         @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public S overlaySteamHullModel(String name) {
        internal.overlaySteamHullModel(name);
        return getThis();
    }

    public S overlaySteamHullModel(ResourceLocation overlayModel) {
        internal.overlaySteamHullModel(overlayModel);
        return getThis();
    }

    public S colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public S colorOverlaySteamHullModel(String overlay,
                                        @Nullable String pipeOverlay,
                                        @Nullable String emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public S colorOverlaySteamHullModel(String overlay,
                                        @Nullable ResourceLocation pipeOverlay,
                                        @Nullable String emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public S colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public S colorOverlaySteamHullModel(ResourceLocation overlay,
                                        @Nullable ResourceLocation pipeOverlay,
                                        @Nullable ResourceLocation emissiveOverlay) {
        internal.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay);
        return getThis();
    }

    public S workableTieredHullModel(ResourceLocation workableModel) {
        internal.workableTieredHullModel(workableModel);
        return getThis();
    }

    public S simpleGeneratorModel(ResourceLocation workableModel) {
        internal.simpleGeneratorModel(workableModel);
        return getThis();
    }

    public S workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        internal.workableSteamHullModel(isHighPressure, workableModel);
        return getThis();
    }

    public S workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        internal.workableCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public S sidedOverlayCasingModel(ResourceLocation baseCasing,
                                     ResourceLocation workableModel) {
        internal.sidedOverlayCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public S sidedWorkableCasingModel(ResourceLocation baseCasing,
                                      ResourceLocation workableModel) {
        internal.sidedWorkableCasingModel(baseCasing, workableModel);
        return getThis();
    }

    public S appearanceBlock(Supplier<? extends Block> block) {
        internal.appearanceBlock(block);
        return getThis();
    }

    public S tooltips(List<? extends Component> components) {
        internal.tooltips(components);
        return getThis();
    }

    public S conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public S conditionalTooltip(Component component, boolean condition) {
        internal.conditionalTooltip(component, condition);
        return getThis();
    }

    public S abilities(PartAbility... abilities) {
        internal.abilities(abilities);
        return getThis();
    }

    public S themeId(Function<Integer, String> themeId) {
        internal.themeId(themeId);
        return getThis();
    }

    public <T extends Comparable<T>> S modelProperty(Property<T> property,
                                                     @Nullable T defaultValue) {
        internal.modelProperty(property, defaultValue);
        return getThis();
    }

    @Tolerate
    public S modelProperties(Map<Property<?>, ? extends Comparable<?>> modelProperties) {
        internal.modelProperties(modelProperties);
        return getThis();
    }

    public S removeModelProperty(Property<?> property) {
        internal.removeModelProperty(property);
        return getThis();
    }

    public S clearModelProperties() {
        internal.clearModelProperties();
        return getThis();
    }

    public S addRecipeModifier(RecipeModifier recipeModifier) {
        internal.addRecipeModifier(recipeModifier);
        return getThis();
    }

    public S recipeModifier(RecipeModifier recipeModifier) {
        internal.recipeModifier(recipeModifier);
        return getThis();
    }

    public S alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        internal.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return getThis();
    }

    public S recipeModifiers(RecipeModifier... recipeModifiers) {
        internal.recipeModifiers(recipeModifiers);
        return getThis();
    }

    public S noRecipeModifier() {
        internal.noRecipeModifier();
        return getThis();
    }

    public S addOutputLimit(RecipeCapability<?> capability, int limit) {
        internal.addOutputLimit(capability, limit);
        return getThis();
    }

    public S ui(PanelFactory factory) {
        internal.ui(factory);
        return getThis();
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(internal, Objects.requireNonNull(entry));
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        final ResourceLocation id = this.id;
        generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
    }

    @Override
    public void generateLang(LangEventJS lang) {
        if (langValue != null) {
            lang.add(id.getNamespace(), Objects.requireNonNull(entry).getDescriptionId(), langValue);
        }
    }

    @Override
    public @Nullable MachineDefinition register() {
        entry = internal.register();
        return entry;
    }
}
