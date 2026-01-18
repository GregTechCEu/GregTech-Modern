package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.datafixers.util.Pair;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A machine trait that provides special interaction behaviour.
 */
@ParametersAreNonnullByDefault
public interface IInteractionTrait extends ITraitFeature {

    /// Called when a player interacts with a machine without a tool.
    default InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                    BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    /// Called when a player interacts with a machine with a tool.
    default Pair<GTToolType, InteractionResult> onToolClick(Set<GTToolType> toolType,
                                                            Player player, InteractionHand hand, Direction gridSide,
                                                            BlockHitResult hitResult) {
        return Pair.of(null, InteractionResult.PASS);
    }
}
