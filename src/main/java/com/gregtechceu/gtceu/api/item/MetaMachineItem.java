package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.core.MixinHelpers;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

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
                if (pipeTile != null && ((PipeBlock<?, ?, ?>) block).canPipeConnectToBlock(pipeTile,
                        side.getOpposite(), level, pos)) {
                    pipeTile.setConnection(side, true, false);
                }
            }
        }
        return superVal;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player,
                                                 ItemStack stack, BlockState state) {
        if (!level.isClientSide) {
            CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            if (!customData.isEmpty()) {
                BlockEntity blockentity = level.getBlockEntity(pos);
                if (blockentity instanceof IMachineBlockEntity holder) {
                    if (!(holder.getMetaMachine() instanceof IDropSaveMachine)) {
                        return false;
                    }
                    if (!blockentity.onlyOpCanSetNbt() || player != null && player.canUseGameMasterBlocks()) {
                        MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.set(level.registryAccess());
                        boolean result = customData.loadInto(blockentity, level.registryAccess());
                        MixinHelpers.CURRENT_BE_SAVE_LOAD_REGISTRIES.remove();
                        return result;
                    }

                    return false;
                }
            }
        }
        return false;
    }
}
