package com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public class MonitorGroup {

    public static final Codec<MonitorGroup> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                    BlockPos.CODEC.listOf().fieldOf("monitorPositions")
                            .forGetter(g -> g.monitorPositions.stream().toList()),
                    Codec.STRING.fieldOf("name").forGetter(MonitorGroup::getName),
                    ItemStack.CODEC.listOf().fieldOf("items").forGetter(g -> g.getItemStackHandler().toList()),
                    ItemStack.CODEC.listOf().fieldOf("placeholderItems")
                            .forGetter(g -> g.getPlaceholderSlotsHandler().toList()),
                    BlockPos.CODEC.optionalFieldOf("target").forGetter(g -> Optional.ofNullable(g.getTargetRaw())),
                    Direction.CODEC.optionalFieldOf("targetSide")
                            .forGetter(g -> Optional.ofNullable(g.getTargetCoverSide())),
                    Codec.INT.fieldOf("dataSlot").forGetter(MonitorGroup::getDataSlot))
                    .apply(instance, MonitorGroup::new));

    @Getter
    private final Set<BlockPos> monitorPositions = new HashSet<>();
    @Setter
    @Getter
    private String name;
    @Getter
    private final CustomItemStackHandler itemStackHandler;
    @Getter
    private final CustomItemStackHandler placeholderSlotsHandler;
    @Setter
    private @Nullable BlockPos target;
    @Setter
    @Getter
    private @Nullable Direction targetCoverSide;
    @Setter
    @Getter
    private int dataSlot = 0;

    public static boolean isModule(ItemStack stack) {
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent itemComponent : componentItem.getComponents()) {
                if (itemComponent instanceof IMonitorModuleItem) return true;
            }
        }
        return false;
    }

    public static CustomItemStackHandler createModuleHandler() {
        CustomItemStackHandler customItemStackHandler = new CustomItemStackHandler(1);
        customItemStackHandler.setFilter(MonitorGroup::isModule);
        return customItemStackHandler;
    }

    public MonitorGroup(String name) {
        this(name, createModuleHandler(), new CustomItemStackHandler(8));
    }

    public MonitorGroup(String name, CustomItemStackHandler handler, CustomItemStackHandler placeholderSlotsHandler) {
        this.name = name;
        this.itemStackHandler = handler;
        this.itemStackHandler.setFilter(MonitorGroup::isModule);
        this.placeholderSlotsHandler = placeholderSlotsHandler;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MonitorGroup(List<BlockPos> monitorPositions, String name, List<ItemStack> items,
                        List<ItemStack> placeholderItems, Optional<BlockPos> rawTarget,
                        Optional<Direction> targetCoverSide, int dataSlot) {
        this.monitorPositions.addAll(monitorPositions);
        this.name = name;
        this.itemStackHandler = new CustomItemStackHandler(
                NonNullList.of(ItemStack.EMPTY, items.toArray(ItemStack[]::new)));
        this.placeholderSlotsHandler = new CustomItemStackHandler(
                NonNullList.of(ItemStack.EMPTY, placeholderItems.toArray(ItemStack[]::new)));
        this.target = rawTarget.orElse(null);
        this.targetCoverSide = targetCoverSide.orElse(null);
        this.dataSlot = dataSlot;
    }

    public void add(BlockPos pos) {
        monitorPositions.add(pos);
    }

    public void remove(BlockPos pos) {
        monitorPositions.remove(pos);
    }

    public List<BlockPos> getRow(int row, UnaryOperator<BlockPos> toRelative) throws IndexOutOfBoundsException {
        IntSet yLevelsSet = new IntOpenHashSet();
        for (BlockPos pos : monitorPositions) {
            yLevelsSet.add(toRelative.apply(pos).getY());
        }
        if (row < 0) row += yLevelsSet.size();
        int y = yLevelsSet.intStream().sorted().toArray()[row];
        List<BlockPos> rowPositions = new ArrayList<>();
        for (BlockPos pos : monitorPositions) {
            if (toRelative.apply(pos).getY() == y) {
                rowPositions.add(toRelative.apply(pos));
            }
        }
        rowPositions.sort(Comparator.comparingInt(Vec3i::getX));
        return rowPositions;
    }

    public boolean contains(BlockPos pos) {
        return monitorPositions.contains(pos);
    }

    public boolean isEmpty() {
        return monitorPositions.isEmpty();
    }

    public @Nullable CoverBehavior getTargetCover(Level level) {
        if (getTarget(level) != null && targetCoverSide != null) {
            ICoverable coverable = GTCapabilityHelper.getCoverable(level, getTarget(level), targetCoverSide);
            if (coverable != null) return coverable.getCoverAtSide(targetCoverSide);
        }
        return null;
    }

    public @Nullable BlockPos getTargetRaw() {
        return target;
    }

    public @Nullable BlockPos getTarget(Level level) {
        if (target == null) return null;

        IMonitorComponent component = GTCapabilityHelper.getMonitorComponent(level, target, null);
        if (component != null && component.getDataItems() != null) {
            ItemStack stack = component.getDataItems().getStackInSlot(dataSlot);
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                return null;
            }
            int x = tag.getInt("targetX");
            int y = tag.getInt("targetY");
            int z = tag.getInt("targetZ");
            Direction face = Direction.byName(tag.getString("face"));
            if (face == null) {
                return null;
            }
            setTargetCoverSide(face);
            return new BlockPos(x, y, z);
        }
        return target;
    }

    public Level getTargetLevel(Level level) {
        if (target == null) return level;

        IMonitorComponent component = GTCapabilityHelper.getMonitorComponent(level, target, null);
        if (component != null && component.getDataItems() != null) {
            ItemStack stack = component.getDataItems().getStackInSlot(dataSlot);
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("dim")) {
                return level;
            }
            if (level.getServer() == null) return level;
            return level.getServer()
                    .getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dim"))));
        }
        return level;
    }
}
