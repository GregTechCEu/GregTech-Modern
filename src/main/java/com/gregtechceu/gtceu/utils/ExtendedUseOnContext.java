package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class ExtendedUseOnContext extends UseOnContext {

    @Getter
    private final Direction gridSide;
    @Getter
    @Unmodifiable
    private final Set<GTToolType> toolType;

    public ExtendedUseOnContext(Player player, InteractionHand hand, BlockHitResult hitResult) {
        super(player, hand, hitResult);
        gridSide = ICoverable.determineGridSideHit(hitResult);
        toolType = ToolHelper.getToolTypes(getItemInHand());
    }

    @Override
    public @UnknownNullability Player getPlayer() {
        return super.getPlayer();
    }
}
