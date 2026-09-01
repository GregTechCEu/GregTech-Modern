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
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.renderer.BlockEntityWithBERModelRenderer;
import com.gregtechceu.gtceu.client.renderer.ItemWithBERModelRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.ModifyMachineEventJS;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.*;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition, MACHINE extends MetaMachine,
        SELF extends MachineBuilder<DEFINITION, MACHINE, SELF>>
                           extends AbstractBuilder<MachineDefinition, DEFINITION, GTRegistrate, SELF> {

    protected MachineInstanceFactory<MACHINE> instanceFactory;

    @Getter
    private final MachineDefinition.Properties properties;

    private @Nullable BlockBuilder<? extends MetaMachineBlock, MachineBuilder<DEFINITION, MACHINE, SELF>> blockBuilder;
    private @Nullable BlockEntry<? extends MetaMachineBlock> blockEntry;
    private @Nullable ItemBuilder<? extends MetaMachineItem, MachineBuilder<DEFINITION, MACHINE, SELF>> itemBuilder;

    private final Set<Supplier<GTRecipeType>> unresolvedRecipeTypes = new ObjectArraySet<>();

    private NonNullConsumer<BlockEntityType<MACHINE>> onBlockEntityRegister = NonNullConsumer.noop();

    public MachineBuilder(GTRegistrate registrate, String name,
                          BuilderCallback callback,
                          MachineInstanceFactory<MACHINE> instanceFactory) {
        super(registrate, registrate, name, callback, GTRegistries.Keys.MACHINE);
        this.instanceFactory = instanceFactory;
        this.properties = createProperties();
        this.defaultLang();
    }

    protected MachineDefinition.Properties createProperties() {
        return new MachineDefinition.Properties();
    }

    protected MachineDefinition.Properties getProperties() {
        return properties;
    }

    @Override
    public GTRegistrate getOwner() {
        return (GTRegistrate) super.getOwner();
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
        properties.blockModel(blockModel);
        return getThis();
    }

    public SELF shape(VoxelShape shape) {
        properties.shape(shape);
        return getThis();
    }

    public SELF rotationState(RotationState rotationState) {
        properties.rotationState(rotationState);
        return getThis();
    }

    public SELF allowExtendedFacing(boolean allowExtendedFacing) {
        properties.allowExtendedFacing(allowExtendedFacing);
        return getThis();
    }

    public SELF hasBER(boolean hasBER) {
        properties.hasBER(hasBER);
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

    /**
     * Create a {@link MetaMachineBlock} for this machine, which is created by the given factory, and return the builder
     * for it so that further customization can be done.<br>
     * Cannot be called if {@link #block()} has already been called.
     *
     * @param <B>     The type of the block.
     * @param factory A factory for the block, which accepts the block properties and machine definition and returns a
     *                new block.
     * @return the {@link BlockBuilder} for the {@link MetaMachineBlock}
     */
    public <B extends MetaMachineBlock> BlockBuilder<B, MachineBuilder<DEFINITION, MACHINE, SELF>> block(BiFunction<BlockBehaviour.Properties, DEFINITION, B> factory) {
        if (blockBuilder != null) throw new IllegalStateException(
                "Block builder for machine %s has already been initialized.".formatted(getName()));

        var newBlockBuilder = this.getOwner().block(this, getName(), properties -> {
            MachineDefinition.setBuilt(this.asSupplier().get());
            var b = factory.apply(properties, this.asSupplier().get());
            MachineDefinition.clearBuilt();
            return b;
        })
                .color(() -> () -> MetaMachineBlock::colorTinted)
                .initialProperties(() -> Blocks.DISPENSER)
                .properties(BlockBehaviour.Properties::noLootTable)
                .addLayer(() -> RenderType::cutout)
                .exBlockstate(properties.blockModel() != null ? properties.blockModel() :
                        createMachineModel(properties.model()))
                .onRegister(b -> Arrays.stream(properties.abilities()).forEach(a -> a.register(properties.tier(), b)));

        blockBuilder = newBlockBuilder;
        return newBlockBuilder;
    }

    /**
     * Gets the {@link MetaMachineItem} builder for this machine so that further customization can be done.<br>
     * If the {@link ItemBuilder} has not been created yet, then the default one is created.
     *
     * @return The {@link ItemBuilder} for the {@link MetaMachineItem}
     */
    public ItemBuilder<? extends MetaMachineItem, MachineBuilder<DEFINITION, MACHINE, SELF>> item() {
        if (itemBuilder != null) return itemBuilder;
        return item(MetaMachineItem::new);
    }

    /**
     * Create a {@link MetaMachineItem} for this machine, which is created by the given factory, and return the builder
     * for it so that further customization can be done.<br>
     * Cannot be called if {@link #item()} has already been called.
     *
     * @param <I>     The type of the item.
     * @param factory A factory for the item, which accepts the block and item properties and returns a new item.
     * @return the {@link ItemBuilder} for the {@link MetaMachineItem}
     */
    public <I extends MetaMachineItem> ItemBuilder<I, MachineBuilder<DEFINITION, MACHINE, SELF>> item(BiFunction<MetaMachineBlock, Item.Properties, I> factory) {
        if (itemBuilder != null) throw new IllegalStateException(
                "Item builder for machine %s has already been initialized.".formatted(getName()));

        var newItemBuilder = getOwner()
                .item(this, getName(),
                        properties -> factory.apply(
                                Objects.requireNonNull(blockEntry, "Item factory called before block resolved.").get(),
                                properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                // copied from BlockBuilder#item
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
                        getOwner().makeResourceLocation("block/machine/" + ctx.getName())))
                .color(() -> () -> ((itemStack, tintIndex) -> tintIndex == 2 ?
                        GTValues.VC[properties.tier() == -1 ? 0 : properties.tier()] :
                        tintIndex == 1 ? properties.paintingColor() : -1))
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
        properties.tier(tier);
        return getThis();
    }

    public SELF recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        properties.recipeOutputLimits(recipeOutputLimits);
        return getThis();
    }

    public SELF paintingColor(int paintingColor) {
        properties.paintingColor(paintingColor);
        return getThis();
    }

    public SELF tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        properties.tooltipBuilder(tooltipBuilder);
        return getThis();
    }

    public SELF alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        properties.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return getThis();
    }

    public SELF beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        properties.beforeWorking(beforeWorking);
        return getThis();
    }

    public SELF onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        properties.onWorking(onWorking);
        return getThis();
    }

    public SELF onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        properties.onWaiting(onWaiting);
        return getThis();
    }

    public SELF afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        properties.afterWorking(afterWorking);
        return getThis();
    }

    public SELF regressWhenWaiting(boolean regressWhenWaiting) {
        properties.regressWhenWaiting(regressWhenWaiting);
        return getThis();
    }

    public SELF allowCoverOnFront(boolean allowCoverOnFront) {
        properties.allowCoverOnFront(allowCoverOnFront);
        return getThis();
    }

    public SELF appearance(Supplier<BlockState> appearance) {
        properties.appearance(appearance);
        return getThis();
    }

    public SELF ui(PanelFactory ui) {
        properties.ui(ui);
        return getThis();
    }

    public SELF defaultLang() {
        return lang(RegistrateLangProvider.toEnglishName(getName()));
    }

    public SELF lang(String name) {
        return langValue(name);
    }

    public SELF langValue(String langValue) {
        properties.langValue(langValue);
        return getThis();
    }

    public SELF recipeType(Supplier<GTRecipeType> type) {
        unresolvedRecipeTypes.add(type);
        initRecipeMachineModelProperties(type);
        return getThis();
    }

    @SafeVarargs
    public final SELF recipeTypes(Supplier<GTRecipeType>... types) {
        Validate.noNullElements(types, "Cannot add null recipe type to machine.");

        for (Supplier<GTRecipeType> type : types) {
            initRecipeMachineModelProperties(type);
            unresolvedRecipeTypes.add(type);
        }
        return getThis();
    }

    protected void initRecipeMachineModelProperties(Supplier<GTRecipeType> type) {
        if (type == GTRecipeTypes.DUMMY_RECIPES) {
            return;
        }
        if (!properties.modelProperties().containsKey(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE);
        }
    }

    public SELF model(@Nullable MachineBuilder.ModelInitializer model) {
        properties.model(model);
        return getThis();
    }

    public SELF simpleModel(ResourceLocation modelName) {
        return model(createBasicMachineModel(modelName));
    }

    public SELF defaultModel() {
        return simpleModel(getOwner().makeResourceLocation("block/machine/template/" + getName()));
    }

    public SELF tieredHullModel(ResourceLocation model) {
        return model(createTieredHullMachineModel(model));
    }

    public SELF overlayTieredHullModel(String name) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        return overlayTieredHullModel(
                getOwner().makeResourceLocation("block/machine/part/" + name));
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
        ResourceLocation overlayTex = getOwner().makeResourceLocation("block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                getOwner().makeResourceLocation("block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                getOwner().makeResourceLocation("block/overlay/machine/" + emissiveOverlay);
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
                getOwner().makeResourceLocation("block/machine/part/" + name));
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
        ResourceLocation overlayTex = getOwner().makeResourceLocation("block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                getOwner().makeResourceLocation("block/overlay/machine/" + pipeOverlay);
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                getOwner().makeResourceLocation("block/overlay/machine/" + emissiveOverlay);
        return colorOverlaySteamHullModel(overlayTex, pipeOverlayTex, emissiveOverlayTex);
    }

    public SELF colorOverlaySteamHullModel(String overlay,
                                           @Nullable ResourceLocation pipeOverlay,
                                           @Nullable String emissiveOverlay) {
        modelProperty(GTMachineModelProperties.IS_FORMED, false);
        ResourceLocation overlayTex = getOwner().makeResourceLocation(
                "block/overlay/machine/" + overlay);
        ResourceLocation pipeOverlayTex = pipeOverlay == null ? null :
                getOwner().makeResourceLocation(("block/overlay/machine/" + pipeOverlay));
        ResourceLocation emissiveOverlayTex = emissiveOverlay == null ? null :
                getOwner().makeResourceLocation(("block/overlay/machine/" + emissiveOverlay));
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
        properties.appearance(() -> block.get().defaultBlockState());
        return getThis();
    }

    @SafeVarargs
    public final SELF tooltips(@Nullable Supplier<? extends @Nullable Component>... components) {
        for (var comp: components) {
            if (comp == null) continue;
            properties.tooltips().add(comp);
        }
        return getThis();
    }

    public SELF tooltips(@Nullable Component... components) {
        for (var comp: components) {
            if (comp == null) continue;
            properties.tooltips().add(() -> comp);
        }
        return getThis();
    }

    public SELF tooltips(List<? extends Component> components) {
        for (var comp: components) {
            properties.tooltips().add(() -> comp);
        }
        return getThis();
    }

    public SELF conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public SELF conditionalTooltip(Component component, boolean condition) {
        if (condition)
            properties.tooltips().add(() -> component);
        return getThis();
    }

    public SELF abilities(PartAbility... abilities) {
        properties.abilities(abilities);
        return getThis();
    }

    public SELF themeId(String themeId) {
        properties.themeId(themeId);
        return getThis();
    }

    public SELF themeId(Function<Integer, String> themeId) {
        properties.themeId(themeId.apply(properties.tier()));
        return getThis();
    }

    public SELF modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> SELF modelProperty(Property<T> property,
                                                        @Nullable T defaultValue) {
        properties.modelProperties().put(property, defaultValue);
        return getThis();
    }

    @Tolerate
    public SELF modelProperties(Property<?>... properties) {
        return this.modelProperties(List.of(properties));
    }

    @Tolerate
    public SELF modelProperties(Collection<Property<?>> modelProperties) {
        for (Property<?> prop : modelProperties) {
            properties.modelProperties().put(prop, null);
        }
        return getThis();
    }

    @Tolerate
    public SELF modelProperties(Map<Property<?>, ? extends Comparable<?>> modelProperties) {
        properties.modelProperties().putAll(modelProperties);
        return getThis();
    }

    public SELF removeModelProperty(Property<?> property) {
        properties.modelProperties().remove(property);
        return getThis();
    }

    public SELF clearModelProperties() {
        properties.modelProperties().clear();
        return getThis();
    }

    public SELF recipeModifier(RecipeModifier recipeModifier) {
        properties.recipeModifier(recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier));
        return getThis();
    }

    public SELF addRecipeModifier(RecipeModifier recipeModifier) {
        if (properties.recipeModifier() instanceof RecipeModifierList list) {
            properties.recipeModifier(new RecipeModifierList(ArrayUtils.add(list.getModifiers(), recipeModifier)));
        } else {
            properties.recipeModifier(new RecipeModifierList(properties.recipeModifier(), recipeModifier));
        }
        return getThis();
    }

    public SELF recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        properties.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
        return this.recipeModifier(recipeModifier);
    }

    public SELF recipeModifiers(RecipeModifier... recipeModifiers) {
        properties.recipeModifier(new RecipeModifierList(recipeModifiers));
        return getThis();
    }

    public SELF recipeModifiers(boolean alwaysTryModifyRecipe,
                                RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }

    public SELF noRecipeModifier() {
        properties.recipeModifier(new RecipeModifierList(RecipeModifier.NO_MODIFIER));
        properties.alwaysTryModifyRecipe(false);
        return getThis();
    }

    public SELF addOutputLimit(RecipeCapability<?> capability, int limit) {
        properties.recipeOutputLimits().put(capability, limit);
        return getThis();
    }

    @SuppressWarnings({ "NullableProblems", "unchecked" })
    protected @NonNull DEFINITION createEntry() {
        properties.recipeTypes(unresolvedRecipeTypes.stream().map(Supplier::get).map(Objects::requireNonNull).toArray(GTRecipeType[]::new));
        return (DEFINITION) new MachineDefinition(getOwner().makeResourceLocation(getName()), properties);
    }

    @HideFromJS
    public MachineEntry<DEFINITION> register() {
        ModifyMachineEvent event = new ModifyMachineEvent(this);
        ModLoader.postEvent(event);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent(event);
        }

        if (properties.model() == null && properties.blockModel() == null) {
            simpleModel(getOwner().makeResourceLocation("block/machine/template/" + getName()));
        }
        if (properties.langValue() != null) {
            block().lang(properties.langValue());
        }

        blockEntry = block().register();

        var blockEntityBuilder = getOwner()
                .<MACHINE, MachineBuilder<DEFINITION, MACHINE, SELF>>blockEntity(this, getName(),
                        (type, pos, state) -> instanceFactory
                                .buildMachine(new BlockEntityCreationInfo(type, pos, state)))
                .onRegister(onBlockEntityRegister)
                .validBlock(blockEntry);

        if (properties.hasBER()) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BlockEntityWithBERModelRenderer::new);
        }

        properties.blockHolder(blockEntry);
        properties.itemHolder(item().register());
        properties.blockEntityTypeSupplier(blockEntityBuilder.register()::get);

        return (MachineEntry<DEFINITION>) super.register();
    }

    @Override
    protected MachineEntry<DEFINITION> createEntryWrapper(DeferredHolder<MachineDefinition, DEFINITION> delegate) {
        return new MachineEntry<>(getOwner(), delegate);
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
