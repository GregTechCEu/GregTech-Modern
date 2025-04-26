package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class DataItemBehavior implements IInteractionItem, IAddInformation {

    public static final DataItemBehavior INSTANCE = new DataItemBehavior();

    protected DataItemBehavior() {}

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        ResearchManager.ResearchItem researchData = stack.get(GTDataComponents.RESEARCH_ITEM);
        if (researchData == null) {
            BlockPos pos = stack.get(GTDataComponents.DATA_COPY_POS);
            if (pos != null) {
                tooltipComponents.add(Component.translatable("gtceu.tooltip.proxy_bind",
                        makePosPart(pos.getX()), makePosPart(pos.getY()), makePosPart(pos.getZ())));
            }
        }
    }

    private static Component makePosPart(int coordinate) {
        return Component.literal(Integer.toString(coordinate)).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            if (!MachineOwner.canOpenOwnerMachine(context.getPlayer(), machine)) {
                return InteractionResult.FAIL;
            }
            if (machine instanceof IDataStickInteractable interactable) {
                if (context.isSecondaryUseActive()) {
                    if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                        return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                    }
                } else {
                    return interactable.onDataStickUse(context.getPlayer(), itemStack);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
