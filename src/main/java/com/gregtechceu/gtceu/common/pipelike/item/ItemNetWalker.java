package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.cover.data.ItemFilterMode;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNetWalker;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemNetWalker extends PipeNetWalker<ItemPipeData, ItemPipeNet> {

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
    private final List<Predicate<ItemStack>> nextFilters = new ArrayList<>();
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected ItemNetWalker(ItemPipeNet world, BlockPos sourcePipe, int distance, List<ItemPipeNet.Inventory> inventories, ItemPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker<ItemPipeData, ItemPipeNet> createSubWalker(ItemPipeNet world, BlockPos nextPos, int walkedBlocks) {
        ItemNetWalker walker = new ItemNetWalker(world, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);
        List<Predicate<ItemStack>> moreFilters = nextFilters;
        if (moreFilters != null && !moreFilters.isEmpty()) {
            walker.filters.addAll(moreFilters);
        }
        return walker;
    }

    @Override
    protected boolean checkPipe(Node<ItemPipeData> pipeNode, BlockPos pos) {
        if (!nextFilters.isEmpty()) {
            this.filters.addAll(nextFilters);
        }
        nextFilters.clear();
        ItemPipeProperties pipeProperties = pipeNode.data.properties;
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new ItemPipeProperties(minProperties.getPriority() + pipeProperties.getPriority(), Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
        return true;
    }

    @Override
    protected void checkNeighbour(Node<ItemPipeData> pipeNode, BlockPos pipePos, Direction faceToNeighbour) {
        if (pipeNode == null || (pipePos.equals(sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        if (getLevel().getBlockEntity(pipePos.relative(faceToNeighbour)) instanceof ItemPipeBlockEntity) {
            if (!isValidPipe(pipePos, faceToNeighbour)) {
                return;
            }
        }
        IItemTransfer handler = ItemTransferHelper.getItemTransfer(this.getLevel(), pipePos, faceToNeighbour.getOpposite());
        if (handler != null) {
            List<Predicate<ItemStack>> filters = new ArrayList<>(this.filters);
            List<Predicate<ItemStack>> moreFilters = nextFilters;
            if (moreFilters != null && !moreFilters.isEmpty()) {
                filters.addAll(moreFilters);
            }
            inventories.add(new ItemPipeNet.Inventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties, filters));
        }
    }

    protected boolean isValidPipe(BlockPos pipePos, Direction faceToNeighbour) {
        BlockEntity currentPipe = getLevel().getBlockEntity(pipePos);
        if (!(getLevel().getBlockEntity(pipePos.relative(faceToNeighbour)) instanceof ItemPipeBlockEntity neighbourPipeBE) || !(currentPipe instanceof ItemPipeBlockEntity currentPipeBE)) {
            return false;
        }
        CoverBehavior thisCover = currentPipeBE.getCoverContainer().getCoverAtSide(faceToNeighbour);
        CoverBehavior neighbourCover = neighbourPipeBE.getCoverContainer().getCoverAtSide(faceToNeighbour.getOpposite());
        List<Predicate<ItemStack>> filters = new ArrayList<>();
        /*if (thisCover instanceof CoverShutter) {
            filters.add(stack -> !thisCover.isValid() || !((CoverShutter) thisCover).isWorkingEnabled());
        } else*/
        if (thisCover instanceof ItemFilterCover filterCover && filterCover.getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            filters.add(filterCover.getItemFilter()::test);
        }
        /*if (neighbourCover instanceof CoverShutter) {
            filters.add(stack -> !neighbourCover.isValid() || !((CoverShutter) neighbourCover).isWorkingEnabled());
        } else */
        if (neighbourCover instanceof ItemFilterCover filterCover && filterCover.getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            filters.add(filterCover.getItemFilter()::test);
        }
        if (!filters.isEmpty()) {
            nextFilters.addAll(filters);
        }
        return true;
    }
}
