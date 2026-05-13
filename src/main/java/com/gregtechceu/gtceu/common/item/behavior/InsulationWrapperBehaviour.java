package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.LevelFluidPipeNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
            player.displayClientMessage(Component.translatable("gtceu.insulation_wrapper.invalid_pipe"), true);
            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        LevelFluidPipeNet levelNet = LevelFluidPipeNet.getOrCreate(serverLevel);
        var net = levelNet.getNetFromPos(pos);
        if (net == null) return InteractionResult.FAIL;

        // get all pipes in the network
        List<BlockPos> toInsulate = new ArrayList<>();
        for (BlockPos pipePos : net.getAllNodes().keySet()) {
            if (level.getBlockEntity(pipePos) instanceof FluidPipeBlockEntity pipe) {
                if (!pipe.isInsulated()) {
                    toInsulate.add(pipePos);
                }
            }
        }

        int needed = toInsulate.size();

        if (!player.isCreative()) {
            // already fully insulated
            if (toInsulate.isEmpty()) {
                player.displayClientMessage(Component.translatable("gtceu.insulation_wrapper.already_insulated"), true);
                return InteractionResult.FAIL;
            }

            // count available wrappers
            int available = player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() == GTItems.ASBESTOS_INSULATION_WRAPPER.get(), 0, player.inventoryMenu.getCraftSlots());
            if (available == 0) {
                return InteractionResult.FAIL;
            }
            needed = Math.min(available, toInsulate.size());
        }

        // insulate all pipes
        for (int i = 0; i < needed; i++) {
            if (level.getBlockEntity(toInsulate.get(i)) instanceof FluidPipeBlockEntity pipe) {
                pipe.setInsulated(true);
            }
        }

        // clear needed amount from inventory
        if (!player.isCreative()){
            player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() == GTItems.ASBESTOS_INSULATION_WRAPPER.get(), needed, player.inventoryMenu.getCraftSlots());
        }

        // inform if its fully insulated or partially insulated; if partial, return uninsulated amount
        int remaining = toInsulate.size() - needed;
        if (remaining > 0) {
            player.displayClientMessage(Component.translatable("gtceu.insulation_wrapper.partial", needed, remaining), true);
        } else {
            player.displayClientMessage(Component.translatable("gtceu.insulation_wrapper.success", needed), true);
        }
        return InteractionResult.SUCCESS;
    }

}