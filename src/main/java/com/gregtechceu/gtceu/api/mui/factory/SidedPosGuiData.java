package com.gregtechceu.gtceu.api.mui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class SidedPosGuiData extends PosGuiData {

    @Getter
    private final Direction side;

    public SidedPosGuiData(Player player, BlockPos pos, Direction side) {
        super(player, pos);
        this.side = side;
    }
}
