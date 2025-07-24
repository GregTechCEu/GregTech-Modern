package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.cover.ComputerMonitorCover;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

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

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTAEPlaceholders {

    private GTAEPlaceholders() {}

    private static @Nullable IGrid getGrid(ComputerMonitorCover cover) {
        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(cover.coverHolder.getLevel(),
                cover.coverHolder.getPos());
        if (nodeHost != null) {
            IGridNode node = nodeHost.getGridNode(cover.attachedSide);
            if (node != null) return node.getGrid();
        } ;
        BlockEntity blockEntity = cover.coverHolder.getLevel().getBlockEntity(cover.coverHolder.getPos());
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            if (machineBlockEntity.getMetaMachine() instanceof IGridConnectedBlockEntity gridMachine) {
                return gridMachine.getMainNode().getGrid();
            }
        }
        if (blockEntity instanceof IGridConnectedBlockEntity gridBlockEntity) {
            IGridNode node = gridBlockEntity.getGridNode();
            if (node != null) return gridBlockEntity.getGridNode().getGrid();
        }
        return null;
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
        ComputerMonitorCover.addPlaceholder("ae2itemCount", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (args.isEmpty()) return GTStringUtils.literalLine(countItems((ItemFilter) null, grid));
            if (args.size() == 1)
                return GTStringUtils.literalLine(countItems(GTStringUtils.componentsToString(args.get(0)), grid));
            if (GTStringUtils.equals(args.get(0), "filter")) {
                try {
                    int slot = GTStringUtils.toInt(args.get(1));
                    if (slot > 8 || slot < 1)
                        return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range",
                                "slot index", 1, 8, slot));
                    return GTStringUtils.literalLine(
                            countItems(ItemFilter.loadFilter(cover.itemStackHandler.getStackInSlot(slot - 1)), grid));
                } catch (NumberFormatException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_number",
                            e.getMessage()));
                } catch (NullPointerException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.missing_item",
                            "filter", args.get(1)));
                }
            }
            return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
        });
        ComputerMonitorCover.addPlaceholder("ae2fluidCount", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (args.isEmpty()) return GTStringUtils.literalLine(countFluids(null, grid));
            if (args.size() == 1) return GTStringUtils
                    .literalLine(countFluids(GTStringUtils.componentsToString(args.get(0)), grid));
            return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
        });
        ComputerMonitorCover.addPlaceholder("ae2power", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            return GTStringUtils.literalLine(grid.getEnergyService().getStoredPower());
        });
        ComputerMonitorCover.addPlaceholder("ae2maxPower", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            return GTStringUtils.literalLine(grid.getEnergyService().getMaxStoredPower());
        });
        ComputerMonitorCover.addPlaceholder("ae2powerUsage", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            return GTStringUtils.literalLine(grid.getEnergyService().getAvgPowerUsage());
        });
        ComputerMonitorCover.addPlaceholder("ae2spatial", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            if (GTStringUtils.equals(args.get(0), "power")) {
                return GTStringUtils.literalLine(grid.getSpatialService().requiredPower());
            } else if (GTStringUtils.equals(args.get(0), "efficiency")) {
                return GTStringUtils.literalLine(grid.getSpatialService().currentEfficiency());
            } else if (GTStringUtils.equals(args.get(0), "sizeX")) {
                return GTStringUtils.literalLine(getSpatialSize(grid).x);
            } else if (GTStringUtils.equals(args.get(0), "sizeY")) {
                return GTStringUtils.literalLine(getSpatialSize(grid).y);
            } else if (GTStringUtils.equals(args.get(0), "sizeZ")) {
                return GTStringUtils.literalLine(getSpatialSize(grid).z);
            } else return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
        });
        ComputerMonitorCover.addPlaceholder("ae2crafting", (cover, args) -> {
            IGrid grid = getGrid(cover);
            if (grid == null) return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_ae"));
            if (args.isEmpty())
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_enough_args", 1, 0));
            ICraftingService crafting = grid.getCraftingService();
            if (GTStringUtils.equals(args.get(0), "get")) {
                if (GTStringUtils.equals(args.get(1), "amount"))
                    return GTStringUtils.literalLine(crafting.getCpus().size());
                try {
                    int index = GTStringUtils.toInt(args.get(1));
                    int i = 0;
                    for (ICraftingCPU cpu : crafting.getCpus()) {
                        if (index - 1 == i) {
                            CraftingJobStatus job = cpu.getJobStatus();
                            if (GTStringUtils.equals(args.get(2), "storage"))
                                return GTStringUtils.literalLine(cpu.getAvailableStorage());
                            else if (GTStringUtils.equals(args.get(2), "threads"))
                                return GTStringUtils.literalLine(cpu.getCoProcessors());
                            else if (GTStringUtils.equals(args.get(2), "name"))
                                return GTUtil.list(cpu.getName() == null ? Component.literal("Crafting CPU " + i) :
                                        cpu.getName().copy());
                            else if (GTStringUtils.equals(args.get(2), "selectionMode"))
                                return GTStringUtils.literalLine(cpu.getSelectionMode().name());
                            else if (job == null) return GTStringUtils.literalLine(0);
                            else if (GTStringUtils.equals(args.get(2), "amount"))
                                return GTStringUtils.literalLine(job.crafting().amount());
                            else if (GTStringUtils.equals(args.get(2), "item"))
                                return GTUtil.list(job.crafting().what().getDisplayName().copy());
                            else if (GTStringUtils.equals(args.get(2), "progress"))
                                return GTStringUtils.literalLine(job.progress());
                            else if (GTStringUtils.equals(args.get(2), "time"))
                                return GTStringUtils.literalLine(job.elapsedTimeNanos());
                            else return GTUtil
                                    .list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
                        }
                        i++;
                    }
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range",
                            "cpu number", 1, crafting.getCpus().size(), index));
                } catch (NumberFormatException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e));
                }
            } // else if (GTStringUtils.equals(args.get(0), "request")) {}
            return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
        });
    }
}
