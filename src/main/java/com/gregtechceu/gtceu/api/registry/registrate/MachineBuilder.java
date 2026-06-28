package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.renderer.BlockEntityWithBERModelRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.generators.BlockModelBuilder;

import brachy.modularui.theme.ThemeAPI;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MachineBuilder<D extends MachineDefinition, M extends MetaMachine, S extends MachineBuilder<D, M, S>> {

    protected final ResourceLocation id;
    protected final GTRegistrate registrate;
    protected final String name;

    protected final BiFunction<BlockBehaviour.Properties, D, MetaMachineBlock> blockFactory;
    protected final BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory;

    @Setter(onMethod_ = @ApiStatus.Internal)
    protected MachineInstanceFactory<M> instanceFactory;
    @Setter(onMethod_ = @ApiStatus.Internal)
    protected Function<ResourceLocation, D> definition;

    @Nullable
    @Getter
    private MachineBuilder.ModelInitializer model = null;
    @Nullable
    @Getter
    private NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel = null;
    @Getter
    protected final Map<Property<?>, @Nullable Comparable<?>> modelProperties = new IdentityHashMap<>();
    private VoxelShape shape = Shapes.block();
    private RotationState rotationState = RotationState.NON_Y_AXIS;

    /**
     * Whether this machine can be rotated or face upwards.
     */
    private boolean allowExtendedFacing = false;
    private boolean hasBER = ConfigHolder.INSTANCE.client.machinesHaveBERsByDefault;
    private boolean renderMultiblockWorldPreview = true;
    private boolean renderMultiblockXEIPreview = true;
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Nullable
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = NonNullConsumer.noop();
    @Getter // getter for KJS
    private GTRecipeType[] recipeTypes = new GTRecipeType[0];
    @Getter // getter for KJS
    private int tier = -1;
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
    private int paintingColor = ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
    private BiFunction<ItemStack, Integer, Integer> itemColor = ((itemStack, tintIndex) -> tintIndex == 2 ?
            GTValues.VC[tier == -1 ? 0 : tier] : tintIndex == 1 ? paintingColor : -1);
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Supplier<Component>> tooltips = new ArrayList<>();
    private @Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    private RecipeModifier recipeModifier = new RecipeModifierList(GTRecipeModifiers.OC_NON_PERFECT);
    private boolean alwaysTryModifyRecipe;
    @Getter
    private BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking = (machine, recipe) -> true;
    @Getter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    @Getter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
    @Getter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
    @Getter
    private boolean regressWhenWaiting = true;
    private boolean allowCoverOnFront = false;
    @Getter
    private @Nullable PanelFactory ui = null;
    @Getter
    private @Nullable String themeId = ThemeAPI.DEFAULT_ID;
    private @Nullable Supplier<BlockState> appearance;
    @Getter // getter for KJS
    private @Nullable String langValue = null;

    public MachineBuilder(GTRegistrate registrate, String name,
                          Function<ResourceLocation, D> definition,
                          BiFunction<BlockBehaviour.Properties, D, MetaMachineBlock> blockFactory,
                          BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          MachineInstanceFactory<M> instanceFactory) {
        this.id = new ResourceLocation(registrate.getModid(), name);
        this.registrate = registrate;
        this.name = name;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.instanceFactory = instanceFactory;
        this.definition = definition;
    }

    @SuppressWarnings("unchecked")
    public S getThis() {
        return (S) this;
    }

    public S blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        this.blockModel = blockModel;
        return getThis();
    }

    public S shape(VoxelShape shape) {
        this.shape = shape;
        return getThis();
    }

    public S rotationState(RotationState rotationState) {
        this.rotationState = rotationState;
        return getThis();
    }

    public S allowExtendedFacing(boolean allowExtendedFacing) {
        this.allowExtendedFacing = allowExtendedFacing;
        return getThis();
    }

    public S hasBER(boolean hasBER) {
        this.hasBER = hasBER;
        return getThis();
    }

    public S renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        this.renderMultiblockWorldPreview = renderMultiblockWorldPreview;
        return getThis();
    }

    public S renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        this.renderMultiblockXEIPreview = renderMultiblockXEIPreview;
        return getThis();
    }

    public S blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        this.blockProp = blockProp;
        return getThis();
    }

    public S itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        this.itemProp = itemProp;
        return getThis();
    }

    public S blockBuilder(@Nullable Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        this.blockBuilder = blockBuilder;
        return getThis();
    }

    public S itemBuilder(@Nullable Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        this.itemBuilder = itemBuilder;
        return getThis();
    }

    public S onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        this.onBlockEntityRegister = onBlockEntityRegister;
        return getThis();
    }

    public S tier(int tier) {
        this.tier = tier;
        return getThis();
    }

    public S recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        this.recipeOutputLimits = recipeOutputLimits;
        return getThis();
    }

    public S paintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        return getThis();
    }

    public S itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        this.itemColor = itemColor;
        return getThis();
    }

    public S tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        this.tooltipBuilder = tooltipBuilder;
        return getThis();
    }

    public S alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return getThis();
    }

    public S beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        this.beforeWorking = beforeWorking;
        return getThis();
    }

    public S onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        this.onWorking = onWorking;
        return getThis();
    }

    public S onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        this.onWaiting = onWaiting;
        return getThis();
    }

    public S afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        this.afterWorking = afterWorking;
        return getThis();
    }

    public S regressWhenWaiting(boolean regressWhenWaiting) {
        this.regressWhenWaiting = regressWhenWaiting;
        return getThis();
    }

    public S allowCoverOnFront(boolean allowCoverOnFront) {
        this.allowCoverOnFront = allowCoverOnFront;
        return getThis();
    }

    public S appearance(@Nullable Supplier<BlockState> appearance) {
        this.appearance = appearance;
        return getThis();
    }

    public S ui(@Nullable PanelFactory ui) {
        this.ui = ui;
        return getThis();
    }

    public S langValue(@Nullable String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public S recipeType(GTRecipeType type) {
        // noinspection ConstantValue
        if (type == null) {
            GTCEu.LOGGER.error(
                    "Tried to set null recipe type on machine {}. Did you create the recipe type before this machine?",
                    this.id);
            return getThis();
        }
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        initRecipeMachineModelProperties(type);
        return getThis();
    }

    @Tolerate
    public S recipeTypes(GTRecipeType... types) {
        List<GTRecipeType> typeList = new ArrayList<>();
        Collections.addAll(typeList, this.recipeTypes);

        for (int i = 0; i < types.length; i++) {
            GTRecipeType type = types[i];
            // noinspection ConstantValue
            if (type != null) {
                initRecipeMachineModelProperties(type);
                typeList.add(type);
            } else {
                GTCEu.LOGGER.error(
                        "Tried to set null recipe type on machine {} (index {}). Did you create the recipe type before this machine?",
                        this.id, i);
            }
        }
        this.recipeTypes = typeList.toArray(GTRecipeType[]::new);
        return getThis();
    }

    protected void initRecipeMachineModelProperties(GTRecipeType type) {
        if (type == GTRecipeTypes.DUMMY_RECIPES) {
            return;
        }
        if (!modelProperties.containsKey(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        }
    }

    public S model(@Nullable MachineBuilder.ModelInitializer model) {
        this.model = model;
        return getThis();
    }

    public S simpleModel(ResourceLocation modelName) {
        return model(createBasicMachineModel(modelName));
    }

    public S defaultModel() {
        return simpleModel(new ResourceLocation(registrate.getModid(), "block/machine/template/" + name));
    }

    public S tieredHullModel(ResourceLocation model) {
        return model(createTieredHullMachineModel(model));
    }

    public S overlayTieredHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlayTieredHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public S overlayTieredHullModel(ResourceLocation overlayModel) {
        return model(createOverlayTieredHullMachineModel(overlayModel));
    }

    public S colorOverlayTieredHullModel(String overlay) {
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public S colorOverlayTieredHullModel(String overlay,
                                         @Nullable String pipeOverlay,
                                         @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + emissiveOverlay);
        return colorOverlayTieredHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public S colorOverlayTieredHullModel(ResourceLocation overlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public S colorOverlayTieredHullModel(ResourceLocation overlay,
                                         @Nullable ResourceLocation pipeOverlay,
                                         @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlayTieredHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public S overlaySteamHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlaySteamHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public S overlaySteamHullModel(ResourceLocation overlayModel) {
        modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
        return model(createOverlaySteamHullMachineModel(overlayModel));
    }

    public S colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public S colorOverlaySteamHullModel(String overlay,
                                        @Nullable String pipeOverlay,
                                        @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + emissiveOverlay);
        return colorOverlaySteamHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public S colorOverlaySteamHullModel(String overlay,
                                        @Nullable ResourceLocation pipeOverlay,
                                        @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                new ResourceLocation(registrate.getModid(), "block/overlay/machine/" + emissiveOverlay);
        return colorOverlaySteamHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public S colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public S colorOverlaySteamHullModel(ResourceLocation overlay,
                                        @Nullable ResourceLocation pipeOverlay,
                                        @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlaySteamHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public S workableTieredHullModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableTieredHullMachineModel(workableModel));
    }

    public S simpleGeneratorModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSimpleGeneratorModel(workableModel));
    }

    public S workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableSteamHullMachineModel(isHighPressure, workableModel));
    }

    public S workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public S sidedOverlayCasingModel(ResourceLocation baseCasing,
                                     ResourceLocation workableModel) {
        return model(createSidedOverlayCasingMachineModel(baseCasing, workableModel));
    }

    public S sidedWorkableCasingModel(ResourceLocation baseCasing,
                                      ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSidedWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public S appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return getThis();
    }

    public S tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public S tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(
                components.stream().filter(Objects::nonNull).map(c -> (Supplier<Component>) (() -> c)).toList());
        return getThis();
    }

    @SafeVarargs
    public final S tooltips(Supplier<Component>... componentSuppliers) {
        tooltips.addAll(List.of(componentSuppliers));
        return getThis();
    }

    public S conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public S conditionalTooltip(Component component, boolean condition) {
        if (condition) tooltips.add(() -> component);
        return getThis();
    }

    public S conditionalTooltip(Supplier<Component> component, boolean condition) {
        if (condition) tooltips.add(component);
        return getThis();
    }

    public S abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return getThis();
    }

    public S themeId(String themeId) {
        this.themeId = themeId;
        return getThis();
    }

    public S themeId(Function<Integer, String> themeId) {
        this.themeId = themeId.apply(tier);
        return getThis();
    }

    public S modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> S modelProperty(Property<T> property,
                                                     @Nullable T defaultValue) {
        this.modelProperties.put(property, defaultValue);
        return getThis();
    }

    @Tolerate
    public S modelProperties(Property<?>... properties) {
        return this.modelProperties(List.of(properties));
    }

    @Tolerate
    public S modelProperties(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            this.modelProperties.put(prop, null);
        }
        return getThis();
    }

    @Tolerate
    public S modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        this.modelProperties.putAll(properties);
        return getThis();
    }

    public S removeModelProperty(Property<?> property) {
        this.modelProperties.remove(property);
        return getThis();
    }

    public S clearModelProperties() {
        this.modelProperties.clear();
        return getThis();
    }

    public S recipeModifier(RecipeModifier recipeModifier) {
        this.recipeModifier = recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier);
        return getThis();
    }

    public S recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this.recipeModifier(recipeModifier);
    }

    public S recipeModifiers(RecipeModifier... recipeModifiers) {
        this.recipeModifier = new RecipeModifierList(recipeModifiers);
        return getThis();
    }

    public S recipeModifiers(boolean alwaysTryModifyRecipe,
                             RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }

    public S noRecipeModifier() {
        this.recipeModifier = new RecipeModifierList(RecipeModifier.NO_MODIFIER);
        this.alwaysTryModifyRecipe = false;
        return getThis();
    }

    public S addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return getThis();
    }

    public S multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                       boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return getThis();
    }

    protected D createDefinition() {
        return definition.apply(new ResourceLocation(registrate.getModid(), name));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setupStateDefinition(MachineDefinition definition) {
        StateDefinition.Builder<MachineDefinition, MachineRenderState> builder = new StateDefinition.Builder<>(
                definition);
        this.modelProperties.keySet().forEach(builder::add);
        definition.setStateDefinition(builder.create(MachineDefinition::defaultRenderState, MachineRenderState::new));

        MachineRenderState defaultState = definition.getStateDefinition().any();
        for (var entry : this.modelProperties.entrySet()) {
            if (entry.getValue() == null) continue;
            defaultState = defaultState.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
        }

        definition.registerDefaultState(defaultState);
    }

    @HideFromJS
    public D register() {
        this.registrate.object(name);

        var definition = createDefinition();

        definition.setRotationState(rotationState);
        setupStateDefinition(definition);
        if (model == null && blockModel == null) {
            simpleModel(new ResourceLocation(registrate.getModid(), "block/machine/template/" + name));
        }
        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(getThis(), definition);
        if (this.langValue != null) {
            blockBuilder.lang(langValue);
            definition.setLangValue(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(getThis(), block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = registrate
                .blockEntity(
                        (type, pos, state) -> instanceFactory
                                .buildMachine(new BlockEntityCreationInfo(type, pos, state)))
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasBER) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BlockEntityWithBERModelRenderer::new);
        }
        var blockEntity = blockEntityBuilder.register();
        if (this.ui != null) {
            definition.setUI(ui);
        }
        if (this.themeId != null) {
            definition.setThemeId(themeId);
        }
        definition.setRecipeTypes(recipeTypes);
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setTier(tier);
        definition.setRecipeOutputLimits(recipeOutputLimits);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips.stream().map(Supplier::get).toList());
            if (tooltipBuilder != null) tooltipBuilder.accept(itemStack, components);
        });
        definition.setRecipeModifier(recipeModifier);
        definition.setAlwaysTryModifyRecipe(alwaysTryModifyRecipe);
        definition.setBeforeWorking(this.beforeWorking);
        definition.setOnWorking(this.onWorking);
        definition.setOnWaiting(this.onWaiting);
        definition.setAfterWorking(this.afterWorking);
        definition.setRegressWhenWaiting(this.regressWhenWaiting);
        definition.setAllowCoverOnFront(this.allowCoverOnFront);

        for (GTRecipeType type : recipeTypes) {
            if (type.getIconSupplier() == null) {
                type.setIconSupplier(definition::asStack);
            }
        }
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        definition.setAppearance(appearance);
        definition.setAllowExtendedFacing(allowExtendedFacing);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);

        this.registrate.generic(definition.getId().getPath(), GTRegistries.Keys.MACHINE, () -> definition).register();

        return definition;
    }

    @FunctionalInterface
    public interface ModelInitializer {

        void configureModel(DataGenContext<Block, ? extends Block> context,
                            GTBlockstateProvider provider,
                            MachineModelBuilder<BlockModelBuilder> builder);

        default ModelInitializer andThen(ModelInitializer after) {
            Objects.requireNonNull(after);
            return (ctx, prov, builder) -> {
                this.configureModel(ctx, prov, builder);
                after.configureModel(ctx, prov, builder);
            };
        }

        default ModelInitializer andThen(Consumer<MachineModelBuilder<BlockModelBuilder>> after) {
            Objects.requireNonNull(after);
            return (ctx, prov, builder) -> {
                this.configureModel(ctx, prov, builder);
                after.accept(builder);
            };
        }

        default ModelInitializer compose(ModelInitializer before) {
            Objects.requireNonNull(before);
            return (ctx, prov, builder) -> {
                before.configureModel(ctx, prov, builder);
                this.configureModel(ctx, prov, builder);
            };
        }

        default ModelInitializer compose(UnaryOperator<MachineModelBuilder<BlockModelBuilder>> before) {
            Objects.requireNonNull(before);
            return (ctx, prov, builder) -> this.configureModel(ctx, prov, before.apply(builder));
        }
    }

    // spotless:off
    protected static class BlockBuilderWrapper {

        public static <DEFINITION extends MachineDefinition> BlockBuilder<Block, ? extends AbstractRegistrate<?>> makeBlockBuilder(MachineBuilder<DEFINITION, ?, ?> builder,
                                                                                                                                   DEFINITION definition) {
            return builder.registrate.block(properties -> makeBlock(builder, definition, properties))
                    .color(() -> () -> MetaMachineBlock::colorTinted)
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutout)
                    .exBlockstate(builder.blockModel != null ? builder.blockModel : createMachineModel(builder.model))
                    .properties(builder.blockProp)
                    .onRegister(b -> Arrays.stream(builder.abilities).forEach(a -> a.register(builder.tier, b)));
        }

        private static <DEFINITION extends MachineDefinition> Block makeBlock(MachineBuilder<DEFINITION, ?, ?> builder, DEFINITION definition,
                                                                              BlockBehaviour.Properties properties) {
            MachineDefinition.setBuilt(definition);
            var b = builder.blockFactory.apply(properties, definition);
            MachineDefinition.clearBuilt();
            return b;
        }
    }

    protected static class ItemBuilderWrapper {

        public static <DEFINITION extends MachineDefinition> ItemBuilder<MetaMachineItem, ? extends AbstractRegistrate<?>> makeItemBuilder(MachineBuilder<DEFINITION, ?, ?> builder,
                                                                                                                                           BlockEntry<Block> block) {
            return builder.registrate
                    .item(properties -> builder.itemFactory.apply((MetaMachineBlock) block.get(), properties))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    // copied from BlockBuilder#item
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation(builder.registrate.getModid(),
                            "block/machine/" + ctx.getName())))
                    .color(() -> () -> builder.itemColor::apply)
                    .properties(builder.itemProp);
        }
    }
    // spotless:on
}
