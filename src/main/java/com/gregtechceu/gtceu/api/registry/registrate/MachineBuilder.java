package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin.RUNTIME_BLOCKSTATE_PROVIDER;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@RemapPrefixForJS("kjs$")
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition> extends BuilderBase<DEFINITION> {

    protected final GTRegistrate registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    protected final Function<ResourceLocation, DEFINITION> definition;
    @Setter
    protected Function<IMachineBlockEntity, MetaMachine> machine;
    @Nullable
    @Getter
    @Setter
    private MachineBuilder.ModelInitializer model = null;
    @Nullable
    @Getter
    @Setter
    private NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel = null;
    @Getter
    protected final Map<Property<?>, @Nullable Comparable<?>> modelProperties = new IdentityHashMap<>();
    @Setter
    private VoxelShape shape = Shapes.block();
    @Setter
    private RotationState rotationState = RotationState.NON_Y_AXIS;
    /**
     * Whether this machine can be rotated or face upwards.
     */
    @Setter
    private boolean allowExtendedFacing = false;
    @Setter
    private boolean hasBER = ConfigHolder.INSTANCE.client.machinesHaveBERsByDefault;
    @Setter
    private boolean renderMultiblockWorldPreview = true;
    @Setter
    private boolean renderMultiblockXEIPreview = true;
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Nullable
    @Setter
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Nullable
    @Setter
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = NonNullConsumer.noop();
    @Getter // getter for KJS
    private @NotNull GTRecipeType @NotNull [] recipeTypes = new GTRecipeType[0];
    @Getter
    @Setter // getter for KJS
    private int tier;
    @Setter
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
    @Setter
    private int paintingColor = ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
    @Setter
    private BiFunction<ItemStack, Integer, Integer> itemColor = ((itemStack, tintIndex) -> tintIndex == 2 ?
            GTValues.VC[tier] : tintIndex == 1 ? paintingColor : -1);
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
    @Nullable
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    private RecipeModifier recipeModifier = new RecipeModifierList(GTRecipeModifiers.OC_NON_PERFECT);
    @Setter
    private boolean alwaysTryModifyRecipe;
    @NotNull
    @Getter
    @Setter
    private BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking = (machine, recipe) -> true;
    @NotNull
    @Getter
    @Setter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
    @Getter
    @Setter
    private boolean regressWhenWaiting = true;

    @Setter
    private boolean allowCoverOnFront = false;
    @Setter
    private Supplier<BlockState> appearance;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private EditableMachineUI editableUI;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private String langValue = null;

    public MachineBuilder(GTRegistrate registrate, String name,
                          Function<ResourceLocation, DEFINITION> definition,
                          Function<IMachineBlockEntity, MetaMachine> machine,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(new ResourceLocation(registrate.getModid(), name));
        this.registrate = registrate;
        this.name = name;
        this.machine = machine;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
        this.definition = definition;
    }

    public MachineBuilder<DEFINITION> recipeType(GTRecipeType type) {
        // noinspection ConstantValue
        if (type == null) {
            GTCEu.LOGGER.error(
                    "Tried to set null recipe type on machine {}. Did you create the recipe type before this machine?",
                    this.id);
            return this;
        }
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        initRecipeMachineModelProperties(type);
        return this;
    }

    @Tolerate
    public MachineBuilder<DEFINITION> recipeTypes(GTRecipeType... types) {
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
        return this;
    }

    protected void initRecipeMachineModelProperties(GTRecipeType type) {
        if (type == GTRecipeTypes.DUMMY_RECIPES) {
            return;
        }
        if (!modelProperties.containsKey(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        }
    }

    public MachineBuilder<DEFINITION> simpleModel(ResourceLocation modelName) {
        return model(createBasicMachineModel(modelName));
    }

    public MachineBuilder<DEFINITION> defaultModel() {
        return simpleModel(new ResourceLocation(registrate.getModid(), "block/machine/template/" + name));
    }

    public MachineBuilder<DEFINITION> tieredHullModel(ResourceLocation model) {
        return model(createTieredHullMachineModel(model));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlayTieredHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullModel(ResourceLocation overlayModel) {
        return model(createOverlayTieredHullMachineModel(overlayModel));
    }

    public MachineBuilder<DEFINITION> colorOverlayTieredHullModel(String overlay) {
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public MachineBuilder<DEFINITION> colorOverlayTieredHullModel(String overlay,
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

    public MachineBuilder<DEFINITION> colorOverlayTieredHullModel(ResourceLocation overlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public MachineBuilder<DEFINITION> colorOverlayTieredHullModel(ResourceLocation overlay,
                                                                  @Nullable ResourceLocation pipeOverlay,
                                                                  @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlayTieredHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlaySteamHullModel(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullModel(ResourceLocation overlayModel) {
        modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
        return model(createOverlaySteamHullMachineModel(overlayModel));
    }

    public MachineBuilder<DEFINITION> colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public MachineBuilder<DEFINITION> colorOverlaySteamHullModel(String overlay,
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

    public MachineBuilder<DEFINITION> colorOverlaySteamHullModel(String overlay,
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

    public MachineBuilder<DEFINITION> colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public MachineBuilder<DEFINITION> colorOverlaySteamHullModel(ResourceLocation overlay,
                                                                 @Nullable ResourceLocation pipeOverlay,
                                                                 @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlaySteamHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public MachineBuilder<DEFINITION> workableTieredHullModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableTieredHullMachineModel(workableModel));
    }

    public MachineBuilder<DEFINITION> simpleGeneratorModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSimpleGeneratorModel(workableModel));
    }

    public MachineBuilder<DEFINITION> workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableSteamHullMachineModel(isHighPressure, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> sidedOverlayCasingModel(ResourceLocation baseCasing,
                                                              ResourceLocation workableModel) {
        return model(createSidedOverlayCasingMachineModel(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingModel(ResourceLocation baseCasing,
                                                               ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSidedWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    public MachineBuilder<DEFINITION> tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public MachineBuilder<DEFINITION> tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(components.stream().filter(Objects::nonNull).toList());
        return this;
    }

    public MachineBuilder<DEFINITION> conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public MachineBuilder<DEFINITION> conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return this;
    }

    public MachineBuilder<DEFINITION> abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return this;
    }

    public MachineBuilder<DEFINITION> modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> MachineBuilder<DEFINITION> modelProperty(Property<T> property,
                                                                              @Nullable T defaultValue) {
        this.modelProperties.put(property, defaultValue);
        return this;
    }

    // KJS helpers for model property defaults
    // These don't need to be copied to the multiblock builder because KJS doesn't care about the return type downgrade

    public MachineBuilder<DEFINITION> kjs$modelPropertyBool(Property<Boolean> property, boolean defaultValue) {
        return modelProperty(property, defaultValue);
    }

    public MachineBuilder<DEFINITION> kjs$modelPropertyInt(Property<Integer> property, int defaultValue) {
        return modelProperty(property, defaultValue);
    }

    public <T extends Enum<T> & Comparable<T>> MachineBuilder<DEFINITION> kjs$modelPropertyEnum(Property<T> property,
                                                                                                T defaultValue) {
        return modelProperty(property, defaultValue);
    }

    @Tolerate
    public MachineBuilder<DEFINITION> modelProperties(Property<?>... properties) {
        return this.modelProperties(List.of(properties));
    }

    @Tolerate
    public MachineBuilder<DEFINITION> modelProperties(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            this.modelProperties.put(prop, null);
        }
        return this;
    }

    @Tolerate
    public MachineBuilder<DEFINITION> modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        this.modelProperties.putAll(properties);
        return this;
    }

    public MachineBuilder<DEFINITION> removeModelProperty(Property<?> property) {
        this.modelProperties.remove(property);
        return this;
    }

    public MachineBuilder<DEFINITION> clearModelProperties() {
        this.modelProperties.clear();
        return this;
    }

    public MachineBuilder<DEFINITION> recipeModifier(RecipeModifier recipeModifier) {
        this.recipeModifier = recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier);
        return this;
    }

    public MachineBuilder<DEFINITION> recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this.recipeModifier(recipeModifier);
    }

    public MachineBuilder<DEFINITION> recipeModifiers(RecipeModifier... recipeModifiers) {
        this.recipeModifier = new RecipeModifierList(recipeModifiers);
        return this;
    }

    public MachineBuilder<DEFINITION> recipeModifiers(boolean alwaysTryModifyRecipe,
                                                      RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }

    public MachineBuilder<DEFINITION> noRecipeModifier() {
        this.recipeModifier = new RecipeModifierList(RecipeModifier.NO_MODIFIER);
        this.alwaysTryModifyRecipe = false;
        return this;
    }

    public MachineBuilder<DEFINITION> addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return this;
    }

    public MachineBuilder<DEFINITION> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return this;
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
        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(this, definition);
        if (this.langValue != null) {
            blockBuilder.lang(langValue);
            definition.setLangValue(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(this, block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = registrate
                .blockEntity((type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
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
        definition.setMachineSupplier(machine);
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
            return (ctx, prov, builder) -> {
                this.configureModel(ctx, prov, before.apply(builder));
            };
        }
    }

    // spotless:off
    protected static class BlockBuilderWrapper {

        public static <DEFINITION extends MachineDefinition> BlockBuilder<Block, ? extends AbstractRegistrate<?>> makeBlockBuilder(MachineBuilder<DEFINITION> builder,
                                                                                                                                   DEFINITION definition) {
            return builder.registrate.block(properties -> makeBlock(builder, definition, properties))
                    .color(() -> () -> IMachineBlock::colorTinted)
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutout)
                    .exBlockstate(builder.blockModel != null ? builder.blockModel : createMachineModel(builder.model))
                    .properties(builder.blockProp)
                    .onRegister(b -> Arrays.stream(builder.abilities).forEach(a -> a.register(builder.tier, b)));
        }

        private static <DEFINITION extends MachineDefinition> Block makeBlock(MachineBuilder<DEFINITION> builder, DEFINITION definition,
                                                                              BlockBehaviour.Properties properties) {
            MachineDefinition.setBuilt(definition);
            var b = builder.blockFactory.apply(properties, definition);
            MachineDefinition.clearBuilt();
            return b.self();
        }
    }

    protected static class ItemBuilderWrapper {

        public static <DEFINITION extends MachineDefinition> ItemBuilder<MetaMachineItem, ? extends AbstractRegistrate<?>> makeItemBuilder(MachineBuilder<DEFINITION> builder,
                                                                                                                                           BlockEntry<Block> block) {
            return builder.registrate
                    .item(properties -> builder.itemFactory.apply((IMachineBlock) block.get(), properties))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    // copied from BlockBuilder#item
                    .model((ctx, prov) -> {
                        prov.withExistingParent(ctx.getName(), new ResourceLocation(builder.registrate.getModid(),
                                "block/machine/" + ctx.getName()));
                    })
                    .color(() -> () -> builder.itemColor::apply)
                    .properties(builder.itemProp);
        }
    }
    // spotless:on

    protected static final class KJSCallWrapper {

        public static <D extends MachineDefinition> void generateAssetJsons(@Nullable AssetJsonGenerator generator,
                                                                            MachineBuilder<D> builder, D definition) {
            if (builder.model() == null && builder.blockModel() == null) return;

            final ResourceLocation id = definition.getId();
            // if generator is null, we're making the block models through GT
            if (generator == null) {
                // Fake a data provider for the GT model builders
                var context = new DataGenContext<>(definition::getBlock, definition.getName(), id);
                if (builder.blockModel() != null) {
                    builder.blockModel().accept(context, RUNTIME_BLOCKSTATE_PROVIDER);
                } else {
                    GTMachineModels.createMachineModel(builder.model()).accept(context, RUNTIME_BLOCKSTATE_PROVIDER);
                }
            } else {
                generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
            }
        }
    }
}
