package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PortableScannerBehavior implements IInteractionItem, IAddInformation {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        if (level.isClientSide() && !level.isEmptyBlock(pos) && player != null) {
            List<Component> info = getScannerInfo(player, level, pos);

            // TODO: Check power

            for (var line : info) {
                player.sendSystemMessage(line);
            }
        }

        GTSoundEntries.PORTABLE_SCANNER.play(level, null, player.position(), 1.0f, 1.0f);

        return InteractionResult.PASS;
    }

    public List<Component> getScannerInfo(Player player, Level level, BlockPos pos) {
        List<Component> list = new ArrayList<>();

        list.add(Component.translatable("hello"));

        return list;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

    }
}
