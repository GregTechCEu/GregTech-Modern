package com.gregtechceu.gtceu.api.mui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
@Getter
public class SidedPosGuiData extends PosGuiData {

    @NotNull
    private final Direction side;

    public SidedPosGuiData(@NotNull Player player, BlockPos pos, @NotNull Direction side) {
        super(player, pos);
        this.side = side;
    }
}
