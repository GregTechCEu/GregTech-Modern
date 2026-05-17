package com.gregtechceu.gtceu.common.item.behavior;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack terminal = context.getItemInHand();
        var tag = terminal.getOrCreateTag();

        if (context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller) {
            if (player.isShiftKeyDown()) {
                if (!controller.getDefaultPatternState().isFormed()) {
                    if (!level.isClientSide) {
                        var patterns = controller.getStructurePatterns();
                        for (var pattern : patterns.values()) {
                            pattern.autobuild(patterns, controller, tag, context);
                        }
                        // controller.createStructurePattern().autoBuild(context.getPlayer(),
                        // controller.getMultiblockState());
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        ItemStack terminal = context.getItemInHand();
        if (terminal.hasTag()) {
            terminal.setTag(null);
        }
        var tag = terminal.getOrCreateTag();

        if (context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        if (MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller) {
            if (!player.isShiftKeyDown()) {
                if (!controller.getDefaultPatternState().isFormed()) {
                    if (!level.isClientSide) {
                        var patterns = controller.getStructurePatterns();
                        for (var entry : patterns.entrySet()) {
                            var pattern = entry.getValue();
                            pattern.retrievePatternInformation(entry.getKey(), controller, tag);
                        }
                        player.sendSystemMessage(Component.translatable("gtceu.autobuild.predicates_retrieved"));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);

        if (!heldItem.is(GTItems.TERMINAL.get())) return InteractionResultHolder.pass(heldItem);

        if (!player.isShiftKeyDown()) {

        } else {
            if (heldItem.hasTag()) {
                // clear the tag :3
                heldItem.setTag(null);
            }
        }

        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
