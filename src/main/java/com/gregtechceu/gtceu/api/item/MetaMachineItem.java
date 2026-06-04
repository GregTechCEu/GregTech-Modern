package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.renderer.ItemWithBERModelRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetaMachineItem extends BlockItem {

    public MetaMachineItem(MetaMachineBlock block, Properties properties) {
        super(block, properties);
    }

    public MachineDefinition getDefinition() {
        return ((MetaMachineBlock) getBlock()).getDefinition();
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean didPlace = super.placeBlock(context, state);

        if (context.getLevel().isClientSide()) return didPlace;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        if (level.getBlockEntity(pos.relative(side.getOpposite())) instanceof PipeBlockEntity<?> pipeBE) {
            pipeBE.tryConnectToAdjacent(side, false);
        }

        return didPlace;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemWithBERModelRenderer.INSTANCE;
            }
        });
    }
}
