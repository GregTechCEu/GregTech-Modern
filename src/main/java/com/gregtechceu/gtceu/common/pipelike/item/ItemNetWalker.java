package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.cover.data.ItemFilterMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

public class ItemNetWalker extends PipeNetWalker<ItemPipeBlockEntity, ItemPipeProperties, ItemPipeNet> {

    public static List<ItemPipeNet.Inventory> createNetData(ItemPipeNet pipeNet, BlockPos sourcePipe) {
        try {
            ItemNetWalker walker = new ItemNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>(), null);
            walker.traversePipeNet();
            return walker.inventories;
        } catch (Exception e){
            GTCEu.LOGGER.error("error while create net data for ItemPipeNet", e);
        }
        return null;
    }

    private ItemPipeProperties minProperties;
    private final List<ItemPipeNet.Inventory> inventories;
    private final List<Predicate<ItemStack>> filters = new ArrayList<>();
    private final EnumMap<Direction, List<Predicate<ItemStack>>> nextFilters = new EnumMap<>(Direction.class);
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected ItemNetWalker(ItemPipeNet world, BlockPos sourcePipe, int distance, List<ItemPipeNet.Inventory> inventories, ItemPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @NotNull
    @Override
    protected PipeNetWalker<ItemPipeBlockEntity, ItemPipeProperties, ItemPipeNet> createSubWalker(ItemPipeNet pipeNet, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        ItemNetWalker walker = new ItemNetWalker(pipeNet, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);
        List<Predicate<ItemStack>> moreFilters = nextFilters.get(facingToNextPos);
        if (moreFilters != null && !moreFilters.isEmpty()) {
            walker.filters.addAll(moreFilters);
        }
        return walker;
    }

    @Override
    protected Class<ItemPipeBlockEntity> getBasePipeClass() {
        return ItemPipeBlockEntity.class;
    }

    @Override
    protected void checkPipe(ItemPipeBlockEntity pipeTile, BlockPos pos) {
        for (List<Predicate<ItemStack>> filters : nextFilters.values()) {
            if (!filters.isEmpty()) {
                this.filters.addAll(filters);
            }
        }
        nextFilters.clear();
        ItemPipeProperties pipeProperties = pipeTile.getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new ItemPipeProperties(minProperties.getPriority() + pipeProperties.getPriority(),
                Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
    }

    @Override
    protected boolean isValidPipe(ItemPipeBlockEntity currentPipe, ItemPipeBlockEntity neighbourPipe, BlockPos pipePos, Direction faceToNeighbour) {
        CoverBehavior thisCover = currentPipe.getCoverContainer().getCoverAtSide(faceToNeighbour);
        CoverBehavior neighbourCover = neighbourPipe.getCoverContainer().getCoverAtSide(faceToNeighbour.getOpposite());
        List<Predicate<ItemStack>> filters = new ArrayList<>();
        /*
        if (thisCover instanceof CoverShutter) {
            filters.add(stack -> !((CoverShutter) thisCover).isWorkingEnabled());
        } else */if (thisCover instanceof ItemFilterCover itemFilterCover &&
            itemFilterCover.getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            filters.add(itemFilterCover.getItemFilter());
        }/*
        if (neighbourCover instanceof CoverShutter) {
            filters.add(stack -> !((CoverShutter) neighbourCover).isWorkingEnabled());
        } else */if (neighbourCover instanceof ItemFilterCover itemFilterCover &&
            itemFilterCover.getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            filters.add(itemFilterCover.getItemFilter());
        }
        if (!filters.isEmpty()) {
            nextFilters.put(faceToNeighbour, filters);
        }
        return true;
    }
}
