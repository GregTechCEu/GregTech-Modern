package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.common.data.GTCompassSections;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MachineBuilder
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MachineDefinition> extends BuilderBase<DEFINITION> {

    protected final Registrate registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;
    @Setter
    protected Function<ResourceLocation, DEFINITION> definitionFactory; // non-final for KJS
    @Setter
    protected Function<IMachineBlockEntity, MetaMachine> metaMachine; // non-final for KJS
    @Nullable
    @Setter
    private Supplier<IRenderer> renderer;
    @Setter
    private VoxelShape shape = Shapes.block();
    @Setter
    private RotationState rotationState = RotationState.NONE;
    @Setter
    private boolean hasTESR;
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Setter
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Setter
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;
    @Setter
    private GTRecipeType recipeType;
    @Getter @Setter // getter for KJS
    private int tier;
    @Setter
    private int paintingColor = Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();
    @Setter
    private BiFunction<ItemStack, Integer, Integer> itemColor = ((itemStack, tintIndex) -> tintIndex == 2 ? GTValues.VC[tier] : tintIndex == 1 ? paintingColor : -1);
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Setter
    private BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier = (machine, recipe) -> recipe;
    @Setter
    private boolean alwaysTryModifyRecipe;
    @Setter
    private Supplier<BlockState> appearance;
    @Setter @Nullable
    private EditableMachineUI editableUI;
    @Setter
    private String langValue = null;
    private final Set<CompassSection> compassSections = new HashSet<>();
    @Nullable
    private String compassNode = null;
    @Nullable
    @Setter
    private ResourceLocation compassPage = null;
    private final List<ResourceLocation> preNodes = new ArrayList<>();

    protected MachineBuilder(Registrate registrate, String name,
                             Function<ResourceLocation, DEFINITION> definitionFactory,
                             Function<IMachineBlockEntity, MetaMachine> metaMachine,
                             BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                             BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                             TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(new ResourceLocation(registrate.getModid(), name));
        this.registrate = registrate;
        this.name = name;
        this.metaMachine = metaMachine;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
        this.definitionFactory = definitionFactory;
    }

    public static <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION> create(Registrate registrate, String name,
                                                                                           Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                           Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                           BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                           BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                           TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MachineBuilder<>(registrate, name, definitionFactory, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public MachineBuilder<DEFINITION> modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> new MachineRenderer(model.get());
        return this;
    }

    public MachineBuilder<DEFINITION> defaultModelRenderer() {
        return modelRenderer(() -> new ResourceLocation(registrate.getModid(), "block/" + name));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullRenderer(String name) {
        return renderer(() -> new OverlayTieredMachineRenderer(tier, new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullRenderer(String name) {
        return renderer(() -> new OverlaySteamMachineRenderer(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> workableTieredHullRenderer(ResourceLocation workableModel) {
        return renderer(() -> new WorkableTieredHullMachineRenderer(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
        return renderer(() -> new WorkableSteamMachineRenderer(isHighPressure, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation workableModel, boolean tint) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel, boolean tint) {
        return renderer(() -> new WorkableSidedCasingMachineRenderer(basePath, overlayModel, tint));
    }

    public MachineBuilder<DEFINITION> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    public MachineBuilder<DEFINITION> tooltips(Component... components) {
        tooltips.addAll(Arrays.stream(components).filter(Objects::nonNull).toList());
        return this;
    }

    public MachineBuilder<DEFINITION> abilities(PartAbility... abilities) {
        this.abilities = abilities;
        compassSections(GTCompassSections.PARTS);
        return this;
    }

    public MachineBuilder<DEFINITION> recipeModifier(BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier, boolean alwaysTryModifyRecipe) {
        this.recipeModifier = recipeModifier;
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this;
    }

    public MachineBuilder<DEFINITION> compassSections(CompassSection... sections) {
        this.compassSections.addAll(Arrays.stream(sections).toList());
        return this;
    }

    public MachineBuilder<DEFINITION> compassNodeSelf() {
        this.compassNode = name;
        return this;
    }

    public MachineBuilder<DEFINITION> compassNode(String compassNode) {
        this.compassNode = compassNode;
        return this;
    }

    public MachineBuilder<DEFINITION> compassPreNodes(CompassSection section, String... compassNodes) {
        for (String nodeID : compassNodes) {
            preNodes.add(GTCEu.id(section.sectionID().getPath() + "/" + nodeID));
        }
        return this;
    }

    public MachineBuilder<DEFINITION> compassPreNodes(ResourceLocation... compassNodes) {
        preNodes.addAll(Arrays.asList(compassNodes));
        return this;
    }

    public MachineBuilder<DEFINITION> compassPreNodes(CompassNode... compassNodes) {
        preNodes.addAll(Arrays.stream(compassNodes).map(CompassNode::nodeID).toList());
        return this;
    }

    protected DEFINITION createDefinition() {
        return definitionFactory.apply(new ResourceLocation(registrate.getModid(), name));
    }

//    @HideFromJS
    public DEFINITION register() {
        var definition = createDefinition();

        var blockBuilder = registrate.block(name, properties -> {
                    RotationState.set(rotationState);
                    var b = blockFactory.apply(properties, definition);
                    RotationState.clear();
                    return b.self();
                })
                .color(() -> () -> IMachineBlock::colorTinted)
                .initialProperties(() -> Blocks.DISPENSER)
                .properties(properties -> properties.noLootTable())
                .addLayer(() -> RenderType::cutoutMipped)
                //.tag(GTToolType.WRENCH.harvestTag)
                .blockstate(NonNullBiConsumer.noop())
                .properties(blockProp)
                .onRegister(b -> Arrays.stream(abilities).forEach(a -> a.register(tier, b)));
        if (this.langValue != null) {
            blockBuilder.lang(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = registrate.item(name, properties -> itemFactory.apply((IMachineBlock) block.get(), properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                .model(NonNullBiConsumer.noop())
                .color(() -> () -> itemColor::apply)
                .properties(itemProp);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        if (this.compassNode != null) {
            if (compassSections.isEmpty()) {
                compassSections.add(GTCompassSections.MACHINES);
            }
            for (CompassSection section : compassSections) {
                itemBuilder.onRegister(item -> {
                    var node = CompassNode.getOrCreate(section, compassNode)
                            .addItem(item::asItem)
                            .addPreNode(preNodes.toArray(ResourceLocation[]::new));
                    if (compassPage != null) {
                        node.page(compassPage);
                    }
                });
            }
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = registrate.blockEntity(name, (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasTESR) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> GTRendererProvider::getOrCreate);
        }
        var blockEntity = blockEntityBuilder.register();
        definition.setRecipeType(recipeType);
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setTier(tier);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setMachineSupplier(metaMachine);
        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips);
            if (tooltipBuilder != null) tooltipBuilder.accept(itemStack, components);
        });
        definition.setRecipeModifier(recipeModifier);
        definition.setAlwaysTryModifyRecipe(alwaysTryModifyRecipe);
        if (renderer == null) {
            renderer = () -> new MachineRenderer(new ResourceLocation(registrate.getModid(), "block/machine/" + name));
        }
        if (recipeType != null && recipeType.getIconSupplier() == null) {
            recipeType.setIconSupplier(definition::asStack);
        }
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        if (editableUI != null) {
            definition.setEditableUI(editableUI);
        }
        definition.setAppearance(appearance);
        definition.setRenderer(LDLib.isClient() ? renderer.get() : IRenderer.EMPTY);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        GTRegistries.MACHINES.register(definition.getId(), definition);
        return definition;
    }

}
