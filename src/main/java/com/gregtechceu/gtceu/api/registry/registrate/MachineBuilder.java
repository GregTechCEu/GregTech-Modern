package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.events.ModifyMachineEvent;
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
import com.gregtechceu.gtceu.client.renderer.ItemWithBERModelRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.ModifyMachineEventJS;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import lombok.Setter;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;

import brachy.modularui.theme.ThemeAPI;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition, MACHINE extends MetaMachine,
        SELF extends MachineBuilder<DEFINITION, MACHINE, SELF>> {

    protected final GTRegistrate registrate;
    protected final String name;

    protected MachineInstanceFactory<MACHINE> instanceFactory;
    @Setter(onMethod_ = @ApiStatus.Internal)
    public Function<ResourceLocation, DEFINITION> definitionFactory;

    private @Nullable BlockBuilder<? extends MetaMachineBlock, MachineBuilder<DEFINITION, MACHINE, SELF>> blockBuilder;
    private @Nullable BlockEntry<? extends MetaMachineBlock> blockEntry;

    private @Nullable ItemBuilder<? extends MetaMachineItem, MachineBuilder<DEFINITION, MACHINE, SELF>> itemBuilder;

    @Nullable
    @Getter
    private MachineBuilder.ModelInitializer model = null;
    @Getter
    private @Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel = null;
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
    private NonNullConsumer<BlockEntityType<MACHINE>> onBlockEntityRegister = NonNullConsumer.noop();
    @Getter // getter for KJS
    private GTRecipeType[] recipeTypes = new GTRecipeType[0];
    @Getter // getter for KJS
    private int tier = -1;
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
    private int paintingColor = ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
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
    @Nullable
    private PanelFactory ui = null;
    @Getter
    private String themeId = ThemeAPI.DEFAULT_ID;
    private @Nullable Supplier<BlockState> appearance;
    @Getter // getter for KJS
    @Nullable
    private String langValue = null;

    public MachineBuilder(GTRegistrate registrate, String name,
                          Function<ResourceLocation, DEFINITION> definitionFactory,
                          MachineInstanceFactory<MACHINE> instanceFactory) {
        this.registrate = registrate;
        this.name = name;
        this.instanceFactory = instanceFactory;
        this.definitionFactory = definitionFactory;

        this.defaultLang();
    }

    @SuppressWarnings("unchecked")
    public SELF getThis() {
        return (SELF) this;
    }

    public SELF instanceFactory(MachineInstanceFactory<MACHINE> instanceFactory) {
        this.instanceFactory = instanceFactory;
        return getThis();
    }

    public SELF machine(MachineInstanceFactory<MACHINE> instanceFactory) {
        return instanceFactory(instanceFactory);
    }

    public SELF blockModel(@Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel) {
        this.blockModel = blockModel;
        return getThis();
    }

    public SELF shape(VoxelShape shape) {
        this.shape = shape;
        return getThis();
    }

    public SELF rotationState(RotationState rotationState) {
        this.rotationState = rotationState;
        return getThis();
    }

    public SELF allowExtendedFacing(boolean allowExtendedFacing) {
        this.allowExtendedFacing = allowExtendedFacing;
        return getThis();
    }

    public SELF hasBER(boolean hasBER) {
        this.hasBER = hasBER;
        return getThis();
    }

    public SELF renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        this.renderMultiblockWorldPreview = renderMultiblockWorldPreview;
        return getThis();
    }

    public SELF renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        this.renderMultiblockXEIPreview = renderMultiblockXEIPreview;
        return getThis();
    }

    /**
     * Gets the {@link MetaMachineBlock} builder for this machine so that further customization can be done.<br>
     * If the {@link BlockBuilder} has not been created yet, then the default one is created.
     *
     * @return the {@link BlockBuilder} for the {@link MetaMachineBlock}
     */
    public BlockBuilder<? extends MetaMachineBlock, MachineBuilder<DEFINITION, MACHINE, SELF>> block() {
        if (blockBuilder != null) return blockBuilder;
        return block(MetaMachineBlock::new);
    }

    @SuppressWarnings("unchecked")
    public NonNullSupplier<DEFINITION> asSupplier() {
        // TODO replace with better method once this is converted to an actual registrate builder
        return () -> (DEFINITION)Objects.requireNonNull(GTRegistries.MACHINES.get(registrate.makeResourceLocation(name)));
    }

    /**
     * Create a {@link MetaMachineBlock} for this machine, which is created by the given factory, and return the builder for it so that further customization can be done.<br>
     * Cannot be called if {@link #block()} has already been called.
     *
     * @param <B> The type of the block.
     * @param factory A factory for the block, which accepts the block properties and machine definition and returns a new block.
     * @return the {@link BlockBuilder} for the {@link MetaMachineBlock}
     */
    public <B extends MetaMachineBlock> BlockBuilder<B, MachineBuilder<DEFINITION, MACHINE, SELF>> block(BiFunction<BlockBehaviour.Properties, DEFINITION, B> factory) {
        if (blockBuilder != null) throw new IllegalStateException("Block builder for machine %s has already been initialized.".formatted(name));

        var newBlockBuilder = this.registrate.block(this, name, properties -> {
                    MachineDefinition.setBuilt(this.asSupplier().get());
                    var b = factory.apply(properties, this.asSupplier().get());
                    MachineDefinition.clearBuilt();
                    return b;
                })
                .color(() -> () -> MetaMachineBlock::colorTinted)
                .initialProperties(() -> Blocks.DISPENSER)
                .properties(BlockBehaviour.Properties::noLootTable)
                .addLayer(() -> RenderType::cutout)
                .exBlockstate(this.blockModel != null ? this.blockModel : createMachineModel(this.model))
                .onRegister(b -> Arrays.stream(this.abilities).forEach(a -> a.register(this.tier, b)));

        blockBuilder = newBlockBuilder;
        return newBlockBuilder;
    }


    /**
     * Gets the {@link MetaMachineItem} builder for this machine so that further customization can be done.<br>
     * If the {@link ItemBuilder} has not been created yet, then the default one is created.
     *
     * @return the {@link ItemBuilder} for the {@link MetaMachineItem}
     */
    public ItemBuilder<? extends MetaMachineItem, MachineBuilder<DEFINITION, MACHINE, SELF>> item() {
        if (itemBuilder != null) return itemBuilder;
        return item(MetaMachineItem::new);
    }

    /**
     * Create a {@link MetaMachineItem} for this machine, which is created by the given factory, and return the builder for it so that further customization can be done.<br>
     * Cannot be called if {@link #item()} has already been called.
     *
     * @param <I> The type of the item.
     * @param factory A factory for the item, which accepts the block and item properties and returns a new item.
     * @return the {@link ItemBuilder} for the {@link MetaMachineItem}
     */
    public <I extends MetaMachineItem> ItemBuilder<I, MachineBuilder<DEFINITION, MACHINE, SELF>> item(BiFunction<MetaMachineBlock, Item.Properties, I> factory) {
        if (itemBuilder != null) throw new IllegalStateException("Item builder for machine %s has already been initialized.".formatted(name));

        var newItemBuilder = this.registrate
                .item(this, name, properties -> factory.apply(Objects.requireNonNull(blockEntry, "Item factory called before block resolved.").get(), properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                // copied from BlockBuilder#item
                .model((ctx, prov) -> {
                    prov.withExistingParent(ctx.getName(), registrate.makeResourceLocation("block/machine/" + ctx.getName()));
                })
                .color(() -> () -> ((itemStack, tintIndex) -> tintIndex == 2 ?
                        GTValues.VC[tier == -1 ? 0 : tier] : tintIndex == 1 ? paintingColor : -1))
                .clientExtension(() -> () -> new IClientItemExtensions() {
                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return ItemWithBERModelRenderer.INSTANCE;
                    }
                });

        itemBuilder = newItemBuilder;
        return newItemBuilder;
    }

    public SELF onBlockEntityRegister(NonNullConsumer<BlockEntityType<MACHINE>> onBlockEntityRegister) {
        this.onBlockEntityRegister = onBlockEntityRegister;
        return getThis();
    }

    public SELF tier(int tier) {
        this.tier = tier;
        return getThis();
    }

    public SELF recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        this.recipeOutputLimits = recipeOutputLimits;
        return getThis();
    }

    public SELF paintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        return getThis();
    }

    public SELF tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        this.tooltipBuilder = tooltipBuilder;
        return getThis();
    }

    public SELF alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return getThis();
    }

    public SELF beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        this.beforeWorking = beforeWorking;
        return getThis();
    }

    public SELF onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        this.onWorking = onWorking;
        return getThis();
    }

    public SELF onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        this.onWaiting = onWaiting;
        return getThis();
    }

    public SELF afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        this.afterWorking = afterWorking;
        return getThis();
    }

    public SELF regressWhenWaiting(boolean regressWhenWaiting) {
        this.regressWhenWaiting = regressWhenWaiting;
        return getThis();
    }

    public SELF allowCoverOnFront(boolean allowCoverOnFront) {
        this.allowCoverOnFront = allowCoverOnFront;
        return getThis();
    }

    public SELF appearance(Supplier<BlockState> appearance) {
        this.appearance = appearance;
        return getThis();
    }

    public SELF ui(PanelFactory ui) {
        this.ui = ui;
        return getThis();
    }

    public SELF defaultLang() {
        return lang(RegistrateLangProvider.toEnglishName(name));
    }

    public SELF lang(String name) {
        return langValue(name);
    }

    public SELF langValue(String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public SELF recipeType(GTRecipeType type) {
        // noinspection ConstantValue
        if (type == null) {
            GTCEu.LOGGER.error(
                    "Tried to set null recipe type on machine {}. Did you create the recipe type before this machine?",
                    this.registrate.makeResourceLocation(this.name));
            return getThis();
        }
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        initRecipeMachineModelProperties(type);
        return getThis();
    }

    @Tolerate
    public SELF recipeTypes(GTRecipeType... types) {
        Validate.noNullElements(types, "Cannot add null recipe type to machine.");

        List<GTRecipeType> typeList = new ArrayList<>();
        Collections.addAll(typeList, this.recipeTypes);

        for (GTRecipeType type : types) {
            initRecipeMachineModelProperties(type);
            typeList.add(type);
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

    public SELF model(@Nullable MachineBuilder.ModelInitializer model) {
        this.model = model;
        return getThis();
    }

    public SELF simpleModel(ResourceLocation modelName) {
        return model(createBasicMachineModel(modelName));
    }

    public SELF defaultModel() {
        return simpleModel(registrate.makeResourceLocation("block/machine/template/" + name));
    }

    public SELF tieredHullModel(ResourceLocation model) {
        return model(createTieredHullMachineModel(model));
    }

    public SELF overlayTieredHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlayTieredHullModel(
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/machine/part/" + name));
    }

    public SELF overlayTieredHullModel(ResourceLocation overlayModel) {
        return model(createOverlayTieredHullMachineModel(overlayModel));
    }

    public SELF colorOverlayTieredHullModel(String overlay) {
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public SELF colorOverlayTieredHullModel(String overlay,
                                            @Nullable String pipeOverlay,
                                            @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = ResourceLocation.fromNamespaceAndPath(registrate.getModid(),
                "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                registrate.makeResourceLocation("block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                registrate.makeResourceLocation("block/overlay/machine/" + emissiveOverlay);
        return colorOverlayTieredHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public SELF colorOverlayTieredHullModel(ResourceLocation overlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return colorOverlayTieredHullModel(overlay, null, null);
    }

    public SELF colorOverlayTieredHullModel(ResourceLocation overlay,
                                            @Nullable ResourceLocation pipeOverlay,
                                            @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlayTieredHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public SELF overlaySteamHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlaySteamHullModel(
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/machine/part/" + name));
    }

    public SELF overlaySteamHullModel(ResourceLocation overlayModel) {
        modelProperty(GTMachineModelProperties.IS_STEEL_MACHINE, ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
        return model(createOverlaySteamHullMachineModel(overlayModel));
    }

    public SELF colorOverlaySteamHullModel(String overlay) {
        return colorOverlaySteamHullModel(overlay, (String) null, null);
    }

    public SELF colorOverlaySteamHullModel(String overlay,
                                           @Nullable String pipeOverlay,
                                           @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = ResourceLocation.fromNamespaceAndPath(registrate.getModid(),
                "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(),
                        "block/overlay/machine/" + emissiveOverlay);
        return colorOverlaySteamHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public SELF colorOverlaySteamHullModel(String overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = ResourceLocation.fromNamespaceAndPath(registrate.getModid(),
                "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                registrate.makeResourceLocation("block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                registrate.makeResourceLocation("block/overlay/machine/" + emissiveOverlay);
        return colorOverlaySteamHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public SELF colorOverlaySteamHullModel(ResourceLocation overlay) {
        return colorOverlaySteamHullModel(overlay, null, null);
    }

    public SELF colorOverlaySteamHullModel(ResourceLocation overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable ResourceLocation emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_PAINTED, false);
        return model(createColorOverlaySteamHullMachineModel(overlay, pipeOverlay, emissiveOverlay));
    }

    public SELF workableTieredHullModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableTieredHullMachineModel(workableModel));
    }

    public SELF simpleGeneratorModel(ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSimpleGeneratorModel(workableModel));
    }

    public SELF workableSteamHullModel(boolean isHighPressure, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableSteamHullMachineModel(isHighPressure, workableModel));
    }

    public SELF workableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public SELF sidedOverlayCasingModel(ResourceLocation baseCasing,
                                        ResourceLocation workableModel) {
        return model(createSidedOverlayCasingMachineModel(baseCasing, workableModel));
    }

    public SELF sidedWorkableCasingModel(ResourceLocation baseCasing,
                                         ResourceLocation workableModel) {
        modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        return model(createSidedWorkableCasingMachineModel(baseCasing, workableModel));
    }

    public SELF appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return getThis();
    }

    public SELF tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public SELF tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(components.stream().filter(Objects::nonNull).toList());
        return getThis();
    }

    public SELF conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public SELF conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return getThis();
    }

    public SELF abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return getThis();
    }

    public SELF themeId(String themeId) {
        this.themeId = themeId;
        return getThis();
    }

    public SELF themeId(Function<Integer, String> themeId) {
        this.themeId = themeId.apply(tier);
        return getThis();
    }

    public SELF modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> SELF modelProperty(Property<T> property,
                                                        @Nullable T defaultValue) {
        this.modelProperties.put(property, defaultValue);
        return getThis();
    }

    @Tolerate
    public SELF modelProperties(Property<?>... properties) {
        return this.modelProperties(List.of(properties));
    }

    @Tolerate
    public SELF modelProperties(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            this.modelProperties.put(prop, null);
        }
        return getThis();
    }

    @Tolerate
    public SELF modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        this.modelProperties.putAll(properties);
        return getThis();
    }

    public SELF removeModelProperty(Property<?> property) {
        this.modelProperties.remove(property);
        return getThis();
    }

    public SELF clearModelProperties() {
        this.modelProperties.clear();
        return getThis();
    }

    public SELF recipeModifier(RecipeModifier recipeModifier) {
        this.recipeModifier = recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier);
        return getThis();
    }

    public SELF addRecipeModifier(RecipeModifier recipeModifier) {
        if (this.recipeModifier instanceof RecipeModifierList list) {
            this.recipeModifier = new RecipeModifierList(ArrayUtils.add(list.getModifiers(), recipeModifier));
        } else {
            this.recipeModifier = new RecipeModifierList(this.recipeModifier, recipeModifier);
        }
        return getThis();
    }

    public SELF recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this.recipeModifier(recipeModifier);
    }

    public SELF recipeModifiers(RecipeModifier... recipeModifiers) {
        this.recipeModifier = new RecipeModifierList(recipeModifiers);
        return getThis();
    }

    public SELF recipeModifiers(boolean alwaysTryModifyRecipe,
                                RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }

    public SELF noRecipeModifier() {
        this.recipeModifier = new RecipeModifierList(RecipeModifier.NO_MODIFIER);
        this.alwaysTryModifyRecipe = false;
        return getThis();
    }

    public SELF addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return getThis();
    }

    public SELF multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                          boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return getThis();
    }

    protected DEFINITION createDefinition() {
        return definitionFactory.apply(registrate.makeResourceLocation(name));
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

    public DEFINITION createEntry() {
        DEFINITION definition = createDefinition();


        return definition;
    }

    /**
     * Finalise the builder for registration (does not actually register the machine, registration is performed during the register event in {@link #createEntry()}
     */
    @HideFromJS
    public DEFINITION register() {
        ModifyMachineEvent event = new ModifyMachineEvent(this);
        ModLoader.postEvent(event);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent(event);
        }
        this.registrate.object(name);
        var definition = createDefinition();

        definition.setRotationState(rotationState);
        setupStateDefinition(definition);
        if (model == null && blockModel == null) {
            simpleModel(registrate.makeResourceLocation("block/machine/template/" + name));
        }
        if (this.langValue != null) {
            block().lang(langValue);
            definition.setLangValue(langValue);
        }

        blockEntry = block().register();

        var blockEntityBuilder = registrate
                .<MACHINE>blockEntity(
                        (type, pos, state) -> instanceFactory
                                .buildMachine(new BlockEntityCreationInfo(type, pos, state)))
                .onRegister(onBlockEntityRegister)
                .validBlock(blockEntry);
        if (hasBER) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BlockEntityWithBERModelRenderer::new);
        }
        var blockEntity = blockEntityBuilder.register();
        if (this.ui != null) {
            definition.setUI(ui);
        }
        definition.setThemeId(themeId);
        definition.setRecipeTypes(recipeTypes);

        definition.setBlockHolder(blockEntry);
        definition.setItemHolder(item().register());
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
            appearance = blockEntry::getDefaultState;
        }
        definition.setAppearance(appearance);
        definition.setAllowExtendedFacing(allowExtendedFacing);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        GTRegistries.register(GTRegistries.MACHINES, definition.getId(), definition);

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

    protected static final class KJSCallWrapper {

        public static void fireKJSEvent(ModifyMachineEvent event) {
            GTCEuStartupEvents.MACHINE_MODIFICATION.post(new ModifyMachineEventJS(event));
        }
    }
}
