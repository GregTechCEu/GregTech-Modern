package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/6/23
 * @implNote PipeBlockItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PipeBlockItem extends BlockItem {
    @Nullable
    public static BlockPlaceContext LAST_CONTEXT = null;

    public PipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!context.replacingClickedOnBlock()) {
            var realPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            var baseNode = getBlock().getPileTile(context.getLevel(), realPos);
            if (baseNode != null) {
                var sideAttach = ICoverable.traceCoverSide(new BlockHitResult(context.getClickLocation(), context.getClickedFace(), realPos, false));
                if (sideAttach != null && context.getLevel().isEmptyBlock(realPos.relative(sideAttach))) {
                    context = new BlockPlaceContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(context.getClickLocation(), sideAttach, realPos, false));
                }
            }
        }

        LAST_CONTEXT = context;
        var result = super.place(context);
        LAST_CONTEXT = null;
        return result;
    }
}
