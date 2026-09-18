package brachy.modularui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class SidedPosGuiData extends PosGuiData {

    @Getter
    private final @NotNull Direction side;

    public SidedPosGuiData(@NotNull Player player, BlockPos pos, @NotNull Direction side) {
        super(player, pos);
        this.side = side;
    }
}
