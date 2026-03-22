package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
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
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;

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

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@RemapPrefixForJS("kjs$")
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition, TYPE extends MachineBuilder<DEFINITION, TYPE>>
                           extends BuilderBase<DEFINITION> {

    protected final GTRegistrate registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory;
    protected final BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory;

    protected final Function<ResourceLocation, DEFINITION> definition;
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
    private @NotNull GTRecipeType @NotNull [] recipeTypes = new GTRecipeType[0];
    @Getter // getter for KJS
    private int tier;
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
    private int paintingColor = ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
    private BiFunction<ItemStack, Integer, Integer> itemColor = ((itemStack, tintIndex) -> tintIndex == 2 ?
            GTValues.VC[tier] : tintIndex == 1 ? paintingColor : -1);
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
    @Nullable
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    private RecipeModifier recipeModifier = new RecipeModifierList(GTRecipeModifiers.OC_NON_PERFECT);
    private boolean alwaysTryModifyRecipe;
    @NotNull
    @Getter
    private BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking = (machine, recipe) -> true;
    @NotNull
    @Getter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    @NotNull
    @Getter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
    @NotNull
    @Getter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
    @Getter
    private boolean regressWhenWaiting = true;
    private boolean allowCoverOnFront = false;
    private Supplier<BlockState> appearance;
    @Getter // getter for KJS
    @Nullable
    private EditableMachineUI editableUI;
    @Getter // getter for KJS
    @Nullable
    private String langValue = null;

    public MachineBuilder(GTRegistrate registrate, String name,
                          Function<ResourceLocation, DEFINITION> definition,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                          BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        super(new ResourceLocation(registrate.getModid(), name));
        this.registrate = registrate;
        this.name = name;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
        this.definition = definition;
    }

    @SuppressWarnings("unchecked")
    public TYPE getThis() {
        return (TYPE) this;
    }

    public TYPE blockModel(NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        this.blockModel = blockModel;
        return getThis();
    }

    public TYPE shape(VoxelShape shape) {
        this.shape = shape;
        return getThis();
    }

    public TYPE rotationState(RotationState rotationState) {
        this.rotationState = rotationState;
        return getThis();
    }

    public TYPE allowExtendedFacing(boolean allowExtendedFacing) {
        this.allowExtendedFacing = allowExtendedFacing;
        return getThis();
    }

    public TYPE hasBER(boolean hasBER) {
        this.hasBER = hasBER;
        return getThis();
    }

    public TYPE renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        this.renderMultiblockWorldPreview = renderMultiblockWorldPreview;
        return getThis();
    }

    public TYPE renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        this.renderMultiblockXEIPreview = renderMultiblockXEIPreview;
        return getThis();
    }

    public TYPE blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        this.blockProp = blockProp;
        return getThis();
    }

    public TYPE itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        this.itemProp = itemProp;
        return getThis();
    }

    public TYPE blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        this.blockBuilder = blockBuilder;
        return getThis();
    }

    public TYPE itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        this.itemBuilder = itemBuilder;
        return getThis();
    }

    public TYPE onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        this.onBlockEntityRegister = onBlockEntityRegister;
        return getThis();
    }

    public TYPE tier(int tier) {
        this.tier = tier;
        return getThis();
    }

    public TYPE recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        this.recipeOutputLimits = recipeOutputLimits;
        return getThis();
    }

    public TYPE paintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        return getThis();
    }

    public TYPE itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        this.itemColor = itemColor;
        return getThis();
    }

    public TYPE tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        this.tooltipBuilder = tooltipBuilder;
        return getThis();
    }

    public TYPE alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return getThis();
    }

    public TYPE beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        this.beforeWorking = beforeWorking;
        return getThis();
    }

    public TYPE onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        this.onWorking = onWorking;
        return getThis();
    }

    public TYPE onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        this.onWaiting = onWaiting;
        return getThis();
    }

    public TYPE afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        this.afterWorking = afterWorking;
        return getThis();
    }

    public TYPE regressWhenWaiting(boolean regressWhenWaiting) {
        this.regressWhenWaiting = regressWhenWaiting;
        return getThis();
    }

    public TYPE allowCoverOnFront(boolean allowCoverOnFront) {
        this.allowCoverOnFront = allowCoverOnFront;
        return getThis();
    }

    public TYPE appearance(Supplier<BlockState> appearance) {
        this.appearance = appearance;
        return getThis();
    }

    public TYPE editableUI(EditableMachineUI editableUI) {
        this.editableUI = editableUI;
        return getThis();
    }

    public TYPE langValue(String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public TYPE recipeType(GTRecipeType type) {
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
    public TYPE recipeTypes(GTRecipeType... types) {
        List<GTRecipeType> typeList = new ArrayList<>();
        Collections.addAll(typeList, this.recipeTypes);

        for (int i = 0; i < types.length; i++) {
            GTRecipeType type = types[i];
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

    public TYPE model(MachineBuilder.ModelInitializer model) {
        this.model = model;
        return getThis();
    }

    public TYPE simpleModel(ResourceLocation modelName) {
        return model(createBasicMachineModel(modelName));
    }

    public TYPE defaultModel() {
        return simpleModel(new ResourceLocation(registrate.getModid(), "block/machine/template/" + name));
    }

    public TYPE tieredHullModel(ResourceLocation model) {
        return model(createTieredHullMachineModel(model));
    }

    public TYPE overlayTieredHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlayTieredHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public TYPE overlayTieredHullModel(ResourceLocation overlayModel) {
        return model(createOverlayTieredHullMachineModel(overlayModel));
    }

    public TYPE colorOverlayTieredHullModel(String overlay) {
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public TYPE colorOverlayTieredHullModel(String overlay,
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

    public TYPE colorOverlayTieredHullModel(ResourceLocation overlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public TYPE colorOverlayTieredHullModel(ResourceLocation overlay,
                                            @Nullable ResourceLocation pipeOverlay,
                                            @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlayTieredHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public TYPE overlaySteamHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlaySteamHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public TYPE overlaySteamHullModel(ResourceLocation overlayModel) {
        modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
        return model(createOverlaySteamHullMachineModel(overlayModel));
    }

    public TYPE colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public TYPE colorOverlaySteamHullModel(String overlay,
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

    public TYPE colorOverlaySteamHullModel(String overlay,
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

    public TYPE colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public TYPE colorOverlaySteamHullModel(ResourceLocation overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlaySteamHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public TYPE workableTieredHullModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableTieredHullMachineModel(workableModel));
    }

    public TYPE simpleGeneratorModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSimpleGeneratorModel(workableModel));
    }

    public TYPE workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableSteamHullMachineModel(isHighPressure, workableModel));
    }

    public TYPE workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public TYPE sidedOverlayCasingModel(ResourceLocation baseCasing,
                                        ResourceLocation workableModel) {
        return model(createSidedOverlayCasingMachineModel(baseCasing, workableModel));
    }

    public TYPE sidedWorkableCasingModel(ResourceLocation baseCasing,
                                         ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSidedWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public TYPE appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return getThis();
    }

    public TYPE tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public TYPE tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(components.stream().filter(Objects::nonNull).toList());
        return getThis();
    }

    public TYPE conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public TYPE conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return getThis();
    }

    public TYPE abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return getThis();
    }

    public TYPE modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> TYPE modelProperty(Property<T> property,
                                                        @Nullable T defaultValue) {
        this.modelProperties.put(property, defaultValue);
        return getThis();
    }

    // KJS helpers for model property defaults
    // These don't need to be copied to the multiblock builder because KJS doesn't care about the return type downgrade

    public TYPE kjs$modelPropertyBool(Property<Boolean> property, boolean defaultValue) {
        return modelProperty(property, defaultValue);
    }

    public TYPE kjs$modelPropertyInt(Property<Integer> property, int defaultValue) {
        return modelProperty(property, defaultValue);
    }

    public <T extends Enum<T> & Comparable<T>> TYPE kjs$modelPropertyEnum(Property<T> property,
                                                                          T defaultValue) {
        return modelProperty(property, defaultValue);
    }

    @Tolerate
    public TYPE modelProperties(Property<?>... properties) {
        return this.modelProperties(List.of(properties));
    }

    @Tolerate
    public TYPE modelProperties(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            this.modelProperties.put(prop, null);
        }
        return getThis();
    }

    @Tolerate
    public TYPE modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        this.modelProperties.putAll(properties);
        return getThis();
    }

    public TYPE removeModelProperty(Property<?> property) {
        this.modelProperties.remove(property);
        return getThis();
    }

    public TYPE clearModelProperties() {
        this.modelProperties.clear();
        return getThis();
    }

    public TYPE recipeModifier(RecipeModifier recipeModifier) {
        this.recipeModifier = recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier);
        return getThis();
    }

    public TYPE recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this.recipeModifier(recipeModifier);
    }

    public TYPE recipeModifiers(RecipeModifier... recipeModifiers) {
        this.recipeModifier = new RecipeModifierList(recipeModifiers);
        return getThis();
    }

    public TYPE recipeModifiers(boolean alwaysTryModifyRecipe,
                                RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }

    public TYPE noRecipeModifier() {
        this.recipeModifier = new RecipeModifierList(RecipeModifier.NO_MODIFIER);
        this.alwaysTryModifyRecipe = false;
        return getThis();
    }

    public TYPE addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return getThis();
    }

    public TYPE multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                          boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return getThis();
    }

    protected DEFINITION createDefinition() {
        return definition.apply(new ResourceLocation(registrate.getModid(), name));
    }

    @Override
    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {
        super.generateAssetJsons(generator);
        KJSCallWrapper.generateAssetJsons(generator, this, this.value);
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        if (langValue() != null) {
            lang.add(GTCEu.MOD_ID, value.getDescriptionId(), value.getLangValue());
        }
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
    public DEFINITION register() {
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
                        (type, pos, state) -> blockEntityFactory.apply(new BlockEntityCreationInfo(type, pos, state)))
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasBER) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BlockEntityWithBERModelRenderer::new);
        }
        var blockEntity = blockEntityBuilder.register();
        definition.setRecipeTypes(recipeTypes);
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setTier(tier);
        definition.setRecipeOutputLimits(recipeOutputLimits);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips);
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
        if (editableUI != null) {
            definition.setEditableUI(editableUI);
        }
        definition.setAppearance(appearance);
        definition.setAllowExtendedFacing(allowExtendedFacing);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        GTRegistries.MACHINES.register(definition.getId(), definition);
        return value = definition;
    }

    @FunctionalInterface
    public interface ModelInitializer {

        void configureModel(@NotNull DataGenContext<Block, ? extends Block> context,
                            @NotNull GTBlockstateProvider provider,
                            @NotNull MachineModelBuilder<BlockModelBuilder> builder);

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

        public static <DEFINITION extends MachineDefinition> BlockBuilder<Block, ? extends AbstractRegistrate<?>> makeBlockBuilder(MachineBuilder<DEFINITION, ?> builder,
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

        private static <DEFINITION extends MachineDefinition> Block makeBlock(MachineBuilder<DEFINITION, ?> builder, DEFINITION definition,
                                                                              BlockBehaviour.Properties properties) {
            MachineDefinition.setBuilt(definition);
            var b = builder.blockFactory.apply(properties, definition);
            MachineDefinition.clearBuilt();
            return b;
        }
    }

    protected static class ItemBuilderWrapper {

        public static <DEFINITION extends MachineDefinition> ItemBuilder<MetaMachineItem, ? extends AbstractRegistrate<?>> makeItemBuilder(MachineBuilder<DEFINITION, ?> builder,
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

    protected static final class KJSCallWrapper {

        public static <D extends MachineDefinition> void generateAssetJsons(@Nullable AssetJsonGenerator generator,
                                                                            MachineBuilder<D, ?> builder,
                                                                            D definition) {
            if (builder.model() == null && builder.blockModel() == null) return;

            final ResourceLocation id = definition.getId();
            // if generator is null, we're making the block models through GT
            if (generator == null) {
                // Fake a data provider for the GT model builders
                var context = new DataGenContext<>(definition::getBlock, definition.getName(), id);
                if (builder.blockModel() != null) {
                    builder.blockModel().accept(context, RuntimeBlockstateProvider.INSTANCE);
                } else {
                    GTMachineModels.createMachineModel(builder.model())
                            .accept(context, RuntimeBlockstateProvider.INSTANCE);
                }
            } else {
                generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
            }
        }
    }
}
