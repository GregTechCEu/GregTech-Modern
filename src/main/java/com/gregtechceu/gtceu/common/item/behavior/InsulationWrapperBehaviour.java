package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import java.util.ArrayList;
import java.util.List;

public class InsulationWrapperBehaviour implements IInteractionItem {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();

        if (player == null || level.isClientSide || !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockState(pos).getBlock() instanceof FluidPipeBlock)) {
            player.displayClientMessage(Component.translatable("item.gtceu.insulation_wrapper.message.invalid_pipe"),
                    true);
            return InteractionResult.FAIL;
        }

        if (!(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity first)) return InteractionResult.FAIL;

        // count available wrappers in inventory
        int available = player.isCreative() ? Integer.MAX_VALUE : player.getInventory().clearOrCountMatchingItems(
                itemStack -> itemStack.getItem() == GTItems.INSULATION_WRAPPER.get(),
                0, player.inventoryMenu.getCraftSlots());

        if (!player.isCreative() && available == 0) {
            return InteractionResult.FAIL;
        }

        // get all pipes in the network with amount of available wrappers as the limit, should function similar to spray
        // cans
        var collected = BreadthFirstBlockSearch.conditionalSearch(IPipeNode.class, first, level, IPipeNode::getBlockPos,
                (parent, child, dir) -> parent == null ||
                        (parent.isConnected(dir) && child.isConnected(dir.getOpposite())),
                available, Integer.MAX_VALUE);

        List<BlockPos> toInsulate = new ArrayList<>();
        for (var node : collected) {
            if (node instanceof FluidPipeBlockEntity pipe && !pipe.isInsulated()) {
                toInsulate.add(pipe.getBlockPos());
            }
        }

        if (toInsulate.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("item.gtceu.insulation_wrapper.message.already_insulated"), true);
            return InteractionResult.FAIL;
        }

        int needed = toInsulate.size();

        // insulate all pipes
        for (BlockPos blockPos : toInsulate) {
            if (level.getBlockEntity(blockPos) instanceof FluidPipeBlockEntity pipe) {
                pipe.setInsulated(true);
            }
        }

        // clear needed amount from inventory
        if (!player.isCreative()) {
            player.getInventory().clearOrCountMatchingItems(
                    itemStack -> itemStack.getItem() == GTItems.INSULATION_WRAPPER.get(),
                    needed, player.inventoryMenu.getCraftSlots());
        }

        // it always insulates pipes matching amount of available wrappers so partial insulation message is no longer
        // possible
        player.displayClientMessage(Component.translatable(
                "item.gtceu.insulation_wrapper.message.success", needed), true);
        return InteractionResult.SUCCESS;
    }
}
