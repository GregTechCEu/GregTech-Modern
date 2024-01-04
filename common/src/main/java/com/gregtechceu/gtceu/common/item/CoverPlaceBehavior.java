package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote CoverBehavior
 */
public record CoverPlaceBehavior(CoverDefinition coverDefinition) implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();
        var player = context.getPlayer();
        ICoverable coverable = GTCapabilityHelper.getCoverable(level, pos, face);
        if (coverable != null) {
            var coverSide = ICoverable.rayTraceCoverableSide(coverable, player);
            if (coverSide != null && coverable.getCoverAtSide(coverSide) == null && coverable.canPlaceCoverOnSide(coverDefinition, coverSide)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    boolean result = coverable.placeCoverOnSide(coverSide, itemStack, coverDefinition, serverPlayer);
                    if (result && !player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return result ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static boolean isCoverBehaviorItem(ItemStack itemStack, @Nullable BooleanSupplier hasCoverSupplier, @Nullable Predicate<CoverDefinition> canPlaceCover) {
        Item item = itemStack.getItem();
        if (item instanceof ComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof CoverPlaceBehavior placeBehavior) {
                    if (canPlaceCover == null || canPlaceCover.test(placeBehavior.coverDefinition)) {
                        return true;
                    }
                }
            }
        } else if (GTToolType.CROWBAR.itemTags.stream().anyMatch(itemStack::is) || GTToolType.SOFT_MALLET.itemTags.stream().anyMatch(itemStack::is)) {
            return hasCoverSupplier == null || hasCoverSupplier.getAsBoolean();
        }
        return false;
    }

}
