package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.RotationState;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.GTRecipeModifiers;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition> {

    protected final AbstractRegistrate<?> registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;
    @Setter // non-final for KJS
    protected Function<ResourceLocation, DEFINITION> definition;
    @Setter // non-final for KJS
    protected Function<IMachineBlockEntity, MetaMachine> machine;
    @Nullable
    @Setter
    private Supplier<IRenderer> renderer;
    @Setter
    private VoxelShape shape = Shapes.block();
    @Setter
    private RotationState rotationState = RotationState.NON_Y_AXIS;
    /**
     * Whether this machine can be rotated or face upwards.
     * todo: set to true by default if we manage to rotate the model accordingly
     */
    @Setter
    private boolean allowExtendedFacing = false;
    @Setter
    private boolean hasTESR;
    @Setter
    private boolean renderMultiblockWorldPreview = true;
    @Setter
    private boolean renderMultiblockXEIPreview = true;
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Setter
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Setter
    @Nullable
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;
    @Getter // getter for KJS
    private GTRecipeType @Nullable [] recipeTypes;
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
    @Setter
    @Nullable
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
    @Nullable
    private Supplier<BlockState> appearance;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private EditableMachineUI editableUI;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private String langValue = null;

    public MachineBuilder(AbstractRegistrate<?> registrate, String name,
                          Function<ResourceLocation, DEFINITION> definition,
                          Function<IMachineBlockEntity, MetaMachine> machine,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        this.registrate = registrate;
        this.name = name;
        this.machine = machine;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
        this.definition = definition;
    }

    public MachineBuilder<DEFINITION> recipeType(GTRecipeType type) {
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        return this;
    }

    @Tolerate
    public MachineBuilder<DEFINITION> recipeTypes(GTRecipeType... types) {
        for (GTRecipeType type : types) {
            this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        }
        return this;
    }

    public MachineBuilder<DEFINITION> modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> new MachineRenderer(model.get());
        return this;
    }

    public MachineBuilder<DEFINITION> defaultModelRenderer() {
        return modelRenderer(() -> ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/" + name));
    }

    public MachineBuilder<DEFINITION> tieredHullRenderer(ResourceLocation model) {
        return renderer(() -> new TieredHullMachineRenderer(tier, model));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullRenderer(String name) {
        return renderer(() -> new OverlayTieredMachineRenderer(tier,
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullRenderer(String name) {
        return renderer(() -> new OverlaySteamMachineRenderer(
                ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> workableTieredHullRenderer(ResourceLocation workableModel) {
        return renderer(() -> new WorkableTieredHullMachineRenderer(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> simpleGeneratorMachineRenderer(ResourceLocation workableModel) {
        return renderer(() -> new SimpleGeneratorMachineRenderer(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> workableSteamHullRenderer(boolean isHighPressure,
                                                                ResourceLocation workableModel) {
        return renderer(() -> new WorkableSteamMachineRenderer(isHighPressure, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel, boolean tint) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel,
                                                                  boolean tint) {
        return renderer(() -> new WorkableSidedCasingMachineRenderer(basePath, overlayModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel) {
        return renderer(() -> new WorkableSidedCasingMachineRenderer(basePath, overlayModel));
    }

    public MachineBuilder<DEFINITION> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    // Go home IntelliJ, you're drunk.
    @SuppressWarnings("NullableProblems")
    public MachineBuilder<DEFINITION> tooltips(@Nullable Component... components) {
        tooltips.addAll(Arrays.stream(components).filter(Objects::nonNull).toList());
        return this;
    }

    public MachineBuilder<DEFINITION> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return conditionalTooltip(component, condition.get());
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
        return definition.apply(ResourceLocation.fromNamespaceAndPath(registrate.getModid(), name));
    }

    @HideFromJS
    public @NotNull DEFINITION register() {
        var definition = createDefinition();

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
                .blockEntity(name, (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasTESR) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> GTRendererProvider::getOrCreate);
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
            renderer = () -> new MachineRenderer(
                    ResourceLocation.fromNamespaceAndPath(registrate.getModid(), "block/machine/" + name));
        }
        if (recipeTypes != null) {
            for (GTRecipeType type : recipeTypes) {
                Objects.requireNonNull(type, "Cannot use null recipe type for machine %s:%s"
                        .formatted(registrate.getModid(), this.name));
                if (type.getIconSupplier() == null) {
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
        definition.setRenderer(GTCEu.isClientSide() ? renderer.get() : IRenderer.EMPTY);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        GTRegistries.register(GTRegistries.MACHINES, definition.getId(), definition);
        return definition;
    }

    static class BlockBuilderWrapper {

        @SuppressWarnings("removal")
        public static <
                D extends MachineDefinition> BlockBuilder<Block, MachineBuilder<D>> makeBlockBuilder(MachineBuilder<D> builder,
                                                                                                     D definition) {
            return builder.registrate.block(builder, builder.name, properties -> {
                RotationState.set(builder.rotationState);
                MachineDefinition.setBuilt(definition);
                var b = builder.blockFactory.apply(properties, definition);
                RotationState.clear();
                MachineDefinition.clearBuilt();
                return b.self();
            })
                    .color(() -> () -> IMachineBlock::colorTinted)
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(NonNullBiConsumer.noop())
                    .properties(builder.blockProp)
                    .onRegister(b -> Arrays.stream(builder.abilities).forEach(a -> a.register(builder.tier, b)));
        }
    }

    static class ItemBuilderWrapper {

        public static <
                D extends MachineDefinition> ItemBuilder<MetaMachineItem, MachineBuilder<D>> makeItemBuilder(MachineBuilder<D> builder,
                                                                                                             BlockEntry<Block> block) {
            return builder.registrate
                    .item(builder, builder.name,
                            properties -> builder.itemFactory.apply((IMachineBlock) block.get(), properties))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .model(NonNullBiConsumer.noop())
                    .color(() -> () -> builder.itemColor::apply)
                    .properties(builder.itemProp);
        }
    }
}
