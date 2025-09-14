package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.IRoutePath;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ItemRoutePath implements IRoutePath<IItemHandler> {

    @Getter
    private final ItemPipeBlockEntity targetPipe;
    @NotNull
    @Getter
    private final Direction targetFacing;
    @Getter
    private final int distance;
    @Getter
    private final ItemPipeProperties properties;
    private final Predicate<ItemStack> filters;
    @Getter
    private final boolean restrictive;

    public ItemRoutePath(ItemPipeBlockEntity targetPipe, @NotNull Direction facing, int distance,
                         ItemPipeProperties properties, boolean restrictive,
                         List<Predicate<ItemStack>> filters) {
        this.targetPipe = targetPipe;
        this.targetFacing = facing;
        this.distance = distance;
        this.properties = properties;
        this.restrictive = restrictive;
        this.filters = stack -> {
            for (Predicate<ItemStack> filter : filters)
                if (!filter.test(stack)) return false;
            return true;
        };
    }

    @Override
    public @NotNull BlockPos getTargetPipePos() {
        return targetPipe.getPipePos();
    }

    @Override
    public @Nullable IItemHandler getHandler(Level world) {
        return GTTransferUtils.getAdjacentItemHandler(world, getTargetPipePos(), targetFacing).resolve().orElse(null);
    }

    public boolean matchesFilters(ItemStack stack) {
        return filters.test(stack);
    }

    public FacingPos toFacingPos() {
        return new FacingPos(getTargetPipePos(), targetFacing);
    }
}
