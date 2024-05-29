package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MetaMachineItem
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetaMachineItem extends BlockItem implements IItemRendererProvider {

    public MetaMachineItem(IMachineBlock block, Properties properties) {
        super(block.self(), properties);
    }

    public MachineDefinition getDefinition() {
        return ((IMachineBlock) getBlock()).getDefinition();
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return ((IMachineBlock) getBlock()).getDefinition().getRenderer();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        boolean superVal = super.placeBlock(context, state);

        if (!level.isClientSide) {
            BlockPos possiblePipe = pos.offset(side.getOpposite().getNormal());
            Block block = level.getBlockState(possiblePipe).getBlock();
            if (block instanceof PipeBlock<?, ?, ?>) {
                IPipeNode pipeTile = ((PipeBlock<?, ?, ?>) block).getPipeTile(level, possiblePipe);
                if (pipeTile != null && ((PipeBlock<?, ?, ?>) block).canPipeConnectToBlock(pipeTile, side.getOpposite(),
                        level.getBlockEntity(pos))) {
                    pipeTile.setConnection(side, true, false);
                }
            }
        }
        return superVal;
    }
}
