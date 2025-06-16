package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.*;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.impl.*;
import com.gregtechceu.gtceu.client.renderer.BakedModelWithBERRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.ConfiguredMachineModel;
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
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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

import static com.gregtechceu.gtceu.common.data.GTModels.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition> extends BuilderBase<DEFINITION> {

    protected final GTRegistrate registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    @Setter // non-final for KJS
    protected Function<ResourceLocation, DEFINITION> definition;
    @Setter // non-final for KJS
    protected Function<IMachineBlockEntity, MetaMachine> machine;
    @Setter
    private ModelConstructor machineModel = (ctx, prov, builder) -> {};
    @Nullable
    @Setter
    private NonNullBiConsumer<DataGenContext<Block, Block>, GTBlockstateProvider> model;
    @Getter
    protected final Map<Property<?>, @Nullable Comparable<?>> modelProperties = new IdentityHashMap<>();
    @Nullable
    @Setter
    private Supplier<MachineModel> renderer;
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
    private boolean hasBER;
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
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;
    @Getter // getter for KJS
    private @Nullable GTRecipeType @Nullable [] recipeTypes = null;
    @Getter
    @Setter // getter for KJS
    private int tier;
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
    @Setter
    private int paintingColor = Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();
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

    @Nullable
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

    @SuppressWarnings("NullableProblems")
    public MachineBuilder<DEFINITION> recipeType(GTRecipeType type) {
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        if (type != GTRecipeTypes.DUMMY_RECIPES) {
            this.setupDefaultRecipeMachineModels();
        }
        return this;
    }

    @SuppressWarnings("NullableProblems")
    @Tolerate
    public MachineBuilder<DEFINITION> recipeTypes(GTRecipeType... types) {
        for (GTRecipeType type : types) {
            this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        }
        this.setupDefaultRecipeMachineModels();
        return this;
    }

    protected MachineBuilder<DEFINITION> setupDefaultRecipeMachineModels() {
        modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE);

        machineModel((ctx, prov, builder) -> {
            final ResourceLocation baseModel = GTCEu.id("block/machine/hull_machine");
            WorkableOverlays overlays = WorkableOverlays.get(
                    this.id.withPrefix("block/machines/"),
                    prov.getExistingFileHelper());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RecipeLogic.STATUS_PROPERTY);

                BlockModelBuilder model = prov.models().withExistingParent(ctx.getName(), baseModel);
                hullTextures(model, this.tier);
                for (var entry : overlays.getTextures().entrySet()) {
                    var face = entry.getKey();
                    var textures = entry.getValue();

                    ResourceLocation overlay = textures.getTexture(status);
                    ResourceLocation overlayEmissive = textures.getEmissiveTexture(status);

                    if (overlay == null) continue;
                    model.texture(OVERLAY_PREFIX + face.getName(), overlay);
                    if (overlayEmissive != null) {
                        model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_POSTFIX, overlayEmissive);
                    }
                }

                return ConfiguredMachineModel.builder().modelFile(model).build();
            });
        });
        return this;
    }

    public MachineBuilder<DEFINITION> model(Supplier<ResourceLocation> model) {
        this.renderer = () -> new MachineModel(model.get());
        return this;
    }

    public MachineBuilder<DEFINITION> defaultModelRenderer() {
        return model(() -> new ResourceLocation(registrate.getModid(), "block/" + name));
    }

    public MachineBuilder<DEFINITION> tieredHullRenderer(ResourceLocation model) {
        return renderer(() -> new TieredHullMachineModel(tier, model));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullRenderer(String name) {
        return renderer(() -> new OverlayTieredMachineModel(tier,
                new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullRenderer(String name) {
        return renderer(() -> new OverlaySteamMachineModel(
                new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> workableTieredHullRenderer(ResourceLocation workableModel) {
        return renderer(() -> new WorkableTieredHullMachineModel(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> simpleGeneratorMachineRenderer(ResourceLocation workableModel) {
        return renderer(() -> new SimpleGeneratorMachineModel(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> workableSteamHullRenderer(boolean isHighPressure,
                                                                ResourceLocation workableModel) {
        return renderer(() -> new WorkableSteamMachineModel(isHighPressure, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel) {
        return renderer(() -> new WorkableCasingMachineModel(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel, boolean tint) {
        return renderer(() -> new WorkableCasingMachineModel(baseCasing, workableModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel,
                                                                  boolean tint) {
        return renderer(() -> new WorkableSidedCasingMachineModel(basePath, overlayModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel) {
        return renderer(() -> new WorkableSidedCasingMachineModel(basePath, overlayModel));
    }

    public MachineBuilder<DEFINITION> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    @SuppressWarnings("NullableProblems")
    public MachineBuilder<DEFINITION> tooltips(@Nullable Component... components) {
        tooltips.addAll(Arrays.stream(components).filter(Objects::nonNull).toList());
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

    public MachineBuilder<DEFINITION> removeRenderProperty(Property<?> property) {
        this.modelProperties.remove(property);
        return this;
    }

    public MachineBuilder<DEFINITION> clearRenderProperties() {
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
            defaultState.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
        }

        definition.registerDefaultState(definition.getStateDefinition().any());
    }

    @HideFromJS
    public DEFINITION register() {
        this.registrate.object(name);
        var definition = createDefinition();

        setupStateDefinition(definition);
        definition.setRotationState(rotationState);
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
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BakedModelWithBERRenderer::new);
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

        if (renderer == null) {
            renderer = () -> new MachineModel(new ResourceLocation(registrate.getModid(), "block/machine/" + name));
        }
        if (recipeTypes != null) {
            for (GTRecipeType type : recipeTypes) {
                if (type != null && type.getIconSupplier() == null) {
                    type.setIconSupplier(definition::asStack);
                }
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
        return definition;
    }

    @FunctionalInterface
    public interface ModelConstructor {

        void configureModel(@NotNull DataGenContext<Block, Block> context, @NotNull GTBlockstateProvider provider,
                            @NotNull MachineModelBuilder<BlockModelBuilder> builder);
    }

    // spotless:off
    protected static class BlockBuilderWrapper {

        @SuppressWarnings("unchecked")
        public static <DEFINITION extends MachineDefinition> BlockBuilder<? extends IMachineBlock, ? extends AbstractRegistrate<?>> makeBlockBuilder(MachineBuilder<DEFINITION> builder,
                                                                                                                                                     DEFINITION definition) {
            return (BlockBuilder<? extends IMachineBlock, ? extends AbstractRegistrate<?>>) builder.registrate
                    .block(properties -> makeBlock(builder, definition, properties))
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(builder.blockProp)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .exBlockstate(builder.model != null ? builder.model : createMachineModel(builder.machineModel))
                    .color(() -> () -> IMachineBlock::colorTinted)
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
                                                                                                                                           BlockEntry<? extends IMachineBlock> block) {
            return builder.registrate
                    .item(properties -> builder.itemFactory.apply(block.get(), properties))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .properties(builder.itemProp)
                    .model(NonNullBiConsumer.noop())
                    .color(() -> () -> builder.itemColor::apply);
        }
    }
    // spotless:on
}
