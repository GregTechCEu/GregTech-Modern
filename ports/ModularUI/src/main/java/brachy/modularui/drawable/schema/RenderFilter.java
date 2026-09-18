package brachy.modularui.drawable.schema;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface RenderFilter {

    RenderFilter ALL = (pos, state) -> true;

    boolean shouldRender(BlockPos pos, BlockState state);
}
