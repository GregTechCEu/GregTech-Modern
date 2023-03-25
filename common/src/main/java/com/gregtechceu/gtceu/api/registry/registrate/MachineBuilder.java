package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MachineBuilder
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class MachineBuilder {

    protected final Registrate registrate;
    protected final String name;
    protected final Function<IMetaMachineBlockEntity, MetaMachine> metaMachine;
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
    private Consumer<BlockBuilder<? extends MetaMachineBlock, ?>> blockBuilder;
    @Setter
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    @Setter
    private GTRecipeType recipeType;
    @Setter
    private int tier;
    @Setter
    private int paintingColor = -1;
    @Setter
    private BiFunction<ItemStack, Integer, Integer> itemColor;
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Setter
    private OverclockingLogic overclockingLogic = OverclockingLogic.NON_PERFECT_OVERCLOCK;
    @Setter
    private Supplier<ResourceLocation> connectedID = () -> null;
    @Setter
    private String langValue = null;

    protected MachineBuilder(Registrate registrate, String name, Function<IMetaMachineBlockEntity, MetaMachine> metaMachine) {
        this.registrate = registrate;
        this.name = name;
        this.metaMachine = metaMachine;
    }

    public static MachineBuilder create(Registrate registrate, String name, Function<IMetaMachineBlockEntity, MetaMachine> metaMachine) {
        return new MachineBuilder(registrate, name, metaMachine);
    }

    public MachineBuilder modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> new MachineRenderer(model.get());
        return this;
    }

    public MachineBuilder defaultModelRenderer() {
        return modelRenderer(() -> new ResourceLocation(registrate.getModid(), "block/" + name));
    }

    public MachineBuilder overlayTieredHullRenderer(String name) {
        return renderer(() -> new OverlayTieredMachineRenderer(tier, new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder overlaySteamHullRenderer(String name) {
        return renderer(() -> new OverlaySteamMachineRenderer(new ResourceLocation(registrate.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder workableTieredHullRenderer(ResourceLocation workableModel) {
        return renderer(() -> new WorkableTieredHullMachineRenderer(tier, workableModel));
    }

    public MachineBuilder workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
        return renderer(() -> new WorkableSteamMachineRenderer(isHighPressure, workableModel));
    }

    public MachineBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel));
    }

    public MachineBuilder workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation workableModel, boolean tint) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel, tint));
    }

    public MachineBuilder connectBlock(Supplier<? extends Block> block) {
        connectedID = () -> Registry.BLOCK.getKey(block.get());
        return this;
    }

    public MachineBuilder tooltips(Component... components) {
        tooltips.addAll(Arrays.stream(components).filter(Objects::nonNull).toList());
        return this;
    }

    public MachineBuilder abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return this;
    }

    protected MachineDefinition createDefinition() {
        return MachineDefinition.createDefinition(new ResourceLocation(registrate.getModid(), name));
    }

    public MachineDefinition register() {
        var definition = createDefinition();

        var blockBuilder = registrate.block(name, properties -> {
                    RotationState.set(rotationState);
                    var b = new MetaMachineBlock(properties, definition);
                    RotationState.clear();
                    return b;
                })
                .color(() -> () -> MetaMachineBlock::colorTinted)
                .initialProperties(() -> Blocks.DISPENSER)
                .addLayer(() -> RenderType::cutoutMipped)
                .tag(GTToolType.WRENCH.harvestTag)
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

        var itemBuilder = registrate.item(name, properties -> new MetaMachineItem(block.get(), properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                .model(NonNullBiConsumer.noop())
                .properties(itemProp);
        if (itemColor != null) {
            itemBuilder.color(() -> () -> itemColor::apply);
        }
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = registrate.blockEntity(name, MachineBuilder::createBlockEntity)
                .onRegister(MachineBuilder::onBlockEntityRegister)
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
        definition.setOverclockingLogic(overclockingLogic);
        if (renderer == null) {
            renderer = () -> new MachineRenderer(new ResourceLocation(registrate.getModid(), "block/machine/" + name));
        }
        if (recipeType != null && recipeType.getIconSupplier() == null) {
            recipeType.setIconSupplier(definition::asStack);
        }
        definition.setConnectedID(connectedID);
        definition.setRenderer(renderer.get());
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        GTRegistries.MACHINES.register(definition.getId(), definition);
        return definition;
    }

    @ExpectPlatform
    private static void onBlockEntityRegister(BlockEntityType<MetaMachineBlockEntity> metaMachineBlockEntityBlockEntityType) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        throw new AssertionError();
    }

}
