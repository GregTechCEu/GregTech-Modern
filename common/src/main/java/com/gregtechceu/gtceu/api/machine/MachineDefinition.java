package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ShapeUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MachineDefinition
 * Representing basic information of a machine.
 */
public class MachineDefinition implements Supplier<MetaMachineBlock> {
    @Getter
    private final ResourceLocation id;
    @Setter
    private Supplier<? extends MetaMachineBlock> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Supplier<BlockEntityType<? extends MetaMachineBlockEntity>> blockEntityTypeSupplier;
    @Setter
    private Function<IMetaMachineBlockEntity, MetaMachine> machineSupplier;
    @Getter
    @Setter
    @Nullable
    private GTRecipeType recipeType;
    @Getter
    @Setter
    private int tier;
    @Setter @Getter
    private int defaultPaintingColor;
    @Setter @Getter
    private OverclockingLogic overclockingLogic;
    @Setter
    @Getter
    private IRenderer renderer;
    @Setter
    private VoxelShape shape;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;

    protected MachineDefinition(ResourceLocation id) {
        this.id = id;
    }

    public static MachineDefinition createDefinition(ResourceLocation id) {
       return new MachineDefinition(id);
    }

    public MetaMachineBlock getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public BlockEntityType<? extends MetaMachineBlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }

    public MetaMachine createMetaMachine(IMetaMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }

    @Override
    public MetaMachineBlock get() {
        return blockSupplier.get();
    }

    public String getName() {
        return id.getPath();
    }

    @Override
    public String toString() {
        return "[Definition: %s]".formatted(id);
    }
}
