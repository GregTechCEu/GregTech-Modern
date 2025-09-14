package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.*;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.me.helpers.IGridConnectedBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTAEPlaceholders {

    private GTAEPlaceholders() {}

    private static IGrid getGrid(PlaceholderContext ctx) throws PlaceholderException {
        if (ctx.pos() == null) throw new NotSupportedException();
        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(ctx.level(), ctx.pos());
        if (nodeHost != null) {
            IGridNode node = nodeHost.getGridNode(ctx.side());
            if (node != null) return node.getGrid();
        } ;
        BlockEntity blockEntity = ctx.level().getBlockEntity(ctx.pos());
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            if (machineBlockEntity.getMetaMachine() instanceof IGridConnectedBlockEntity gridMachine) {
                IGrid nullable = gridMachine.getMainNode().getGrid();
                if (nullable == null) throw new NoMENetworkException();
                return nullable;
            }
        }
        if (blockEntity instanceof IGridConnectedBlockEntity gridBlockEntity) {
            IGridNode node = gridBlockEntity.getGridNode();
            if (node != null) return gridBlockEntity.getGridNode().getGrid();
        }
        throw new NoMENetworkException();
    }

    private static long countItems(String id, IGrid grid) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) return 0;
        GenericStack stack = GenericStack.fromItemStack(new ItemStack(item, 1));
        if (stack == null) return 0;
        return grid.getStorageService().getInventory().getAvailableStacks().get(stack.what());
    }

    private static long countItems(@Nullable ItemFilter filter, IGrid grid) {
        KeyCounter stacks = grid.getStorageService().getCachedInventory();
        long count = 0;
        for (var stack : stacks) {
            if (stack.getKey() instanceof AEItemKey &&
                    (filter == null || filter.test(stack.getKey().wrapForDisplayOrFilter())))
                count += stack.getLongValue();
        }
        return count;
    }

    private static long countFluids(@Nullable String id, IGrid grid) {
        if (id == null) {
            KeyCounter stacks = grid.getStorageService().getCachedInventory();
            long count = 0;
            for (var stack : stacks) {
                if (stack.getKey() instanceof AEFluidKey) count += stack.getLongValue();
            }
            return count;
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
        if (fluid == null) return 0;
        GenericStack stack = GenericStack.fromFluidStack(new FluidStack(fluid, 1));
        if (stack == null) return 0;
        return grid.getStorageService().getInventory().getAvailableStacks().get(stack.what());
    }

    private static Vector3i getSpatialSize(IGrid grid) {
        BlockPos start = grid.getSpatialService().getMin();
        BlockPos end = grid.getSpatialService().getMax();
        BlockPos tmp = end.subtract(start);
        return new Vector3i(tmp.getX(), tmp.getY(), tmp.getZ()).absolute();
    }

    public static void init() {
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2itemCount") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                if (args.isEmpty()) return MultiLineComponent.literal(countItems((ItemFilter) null, grid));
                if (args.size() == 1)
                    return MultiLineComponent.literal(countItems(GTStringUtils.componentsToString(args.get(0)), grid));
                if (GTStringUtils.equals(args.get(0), "filter")) {
                    int slot = PlaceholderUtils.toInt(args.get(1));
                    try {
                        PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                        if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                        return MultiLineComponent.literal(countItems(
                                ItemFilter.loadFilter(ctx.itemStackHandler().getStackInSlot(slot - 1)), grid));
                    } catch (NullPointerException e) {
                        throw new MissingItemException("filter", slot);
                    }
                }
                throw new InvalidArgsException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2fluidCount") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                if (args.isEmpty()) return MultiLineComponent.literal(countFluids(null, grid));
                if (args.size() == 1)
                    return MultiLineComponent.literal(countFluids(GTStringUtils.componentsToString(args.get(0)), grid));
                throw new WrongNumberOfArgsException(1, args.size());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2power") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                PlaceholderUtils.checkArgs(args, 0);
                return MultiLineComponent.literal(grid.getEnergyService().getStoredPower());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2maxPower") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                PlaceholderUtils.checkArgs(args, 0);
                return MultiLineComponent.literal(grid.getEnergyService().getMaxStoredPower());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2powerUsage") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                PlaceholderUtils.checkArgs(args, 0);
                return MultiLineComponent.literal(grid.getEnergyService().getAvgPowerUsage());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2spatial") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                PlaceholderUtils.checkArgs(args, 1);
                if (GTStringUtils.equals(args.get(0), "power")) {
                    return MultiLineComponent.literal(grid.getSpatialService().requiredPower());
                } else if (GTStringUtils.equals(args.get(0), "efficiency")) {
                    return MultiLineComponent.literal(grid.getSpatialService().currentEfficiency());
                } else if (GTStringUtils.equals(args.get(0), "sizeX")) {
                    return MultiLineComponent.literal(getSpatialSize(grid).x);
                } else if (GTStringUtils.equals(args.get(0), "sizeY")) {
                    return MultiLineComponent.literal(getSpatialSize(grid).y);
                } else if (GTStringUtils.equals(args.get(0), "sizeZ")) {
                    return MultiLineComponent.literal(getSpatialSize(grid).z);
                } else throw new InvalidArgsException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ae2crafting") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IGrid grid = getGrid(ctx);
                PlaceholderUtils.checkArgs(args, 1, true);
                ICraftingService crafting = grid.getCraftingService();
                if (GTStringUtils.equals(args.get(0), "get")) {
                    if (GTStringUtils.equals(args.get(1), "amount"))
                        return MultiLineComponent.literal(crafting.getCpus().size());
                    int index = PlaceholderUtils.toInt(args.get(1));
                    int i = 0;
                    for (ICraftingCPU cpu : crafting.getCpus()) {
                        if (index - 1 == i) {
                            CraftingJobStatus job = cpu.getJobStatus();
                            if (GTStringUtils.equals(args.get(2), "storage"))
                                return MultiLineComponent.literal(cpu.getAvailableStorage());
                            else if (GTStringUtils.equals(args.get(2), "threads"))
                                return MultiLineComponent.literal(cpu.getCoProcessors());
                            else if (GTStringUtils.equals(args.get(2), "name"))
                                return MultiLineComponent
                                        .of(cpu.getName() == null ? Component.literal("Crafting CPU " + i) :
                                                cpu.getName().copy());
                            else if (GTStringUtils.equals(args.get(2), "selectionMode"))
                                return MultiLineComponent.literal(cpu.getSelectionMode().name());
                            else if (job == null) return MultiLineComponent.literal(0);
                            else if (GTStringUtils.equals(args.get(2), "amount"))
                                return MultiLineComponent.literal(job.crafting().amount());
                            else if (GTStringUtils.equals(args.get(2), "item"))
                                return MultiLineComponent.of(job.crafting().what().getDisplayName().copy());
                            else if (GTStringUtils.equals(args.get(2), "progress"))
                                return MultiLineComponent.literal(job.progress());
                            else if (GTStringUtils.equals(args.get(2), "time"))
                                return MultiLineComponent.literal(job.elapsedTimeNanos());
                            else throw new InvalidArgsException();
                        }
                        i++;
                    }
                    throw new OutOfRangeException("cpu number", 1, crafting.getCpus().size(), index);
                } // else if (GTStringUtils.equals(args.get(0), "request")) {} gonna implement that someday :)
                throw new InvalidArgsException();
            }
        });
    }
}
