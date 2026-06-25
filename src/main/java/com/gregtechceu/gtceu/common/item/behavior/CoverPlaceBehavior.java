package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record CoverPlaceBehavior(Supplier<CoverDefinition> coverDefinition) implements IInteractionItem {

    public CoverPlaceBehavior(CoverDefinition coverDefinition) {
        this(() -> coverDefinition);
    }

    public CoverPlaceBehavior(Holder<CoverDefinition> coverDefinitionHolder) {
        this(coverDefinitionHolder.value());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();
        var player = context.getPlayer();
        ICoverable coverable = GTCapabilityHelper.getCoverable(level, pos, face);
        if (coverable != null) {
            var coverSide = ICoverable.rayTraceCoverableSide(coverable, player);
            if (coverSide != null && coverable.getCoverAtSide(coverSide) == null &&
                    coverable.canPlaceCoverOnSide(coverDefinition.get(), coverSide)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    boolean result = coverable.placeCoverOnSide(coverSide, itemStack, coverDefinition.get(), serverPlayer);
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

    public static boolean isCoverBehaviorItem(ItemStack itemStack, @Nullable BooleanSupplier hasCoverSupplier,
                                              @Nullable Predicate<CoverDefinition> canPlaceCover) {
        Item item = itemStack.getItem();
        if (item instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof CoverPlaceBehavior placeBehavior) {
                    if (canPlaceCover == null || canPlaceCover.test(placeBehavior.coverDefinition.get())) {
                        return true;
                    }
                }
            }
        } else if (GTToolType.CROWBAR.matchTags.stream().anyMatch(itemStack::is) ||
                GTToolType.SOFT_MALLET.matchTags.stream().anyMatch(itemStack::is) ||
                GTToolType.SCREWDRIVER.matchTags.stream().anyMatch(itemStack::is)) {
                    return hasCoverSupplier == null || hasCoverSupplier.getAsBoolean();
                }
        return false;
    }
}
