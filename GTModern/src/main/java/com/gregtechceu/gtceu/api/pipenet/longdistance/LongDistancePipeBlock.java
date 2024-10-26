package com.gregtechceu.gtceu.api.pipenet.longdistance;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LongDistancePipeBlock extends Block implements ILDNetworkPart {

    @Getter
    private final LongDistancePipeType pipeType;

    public LongDistancePipeBlock(BlockBehaviour.Properties properties, LongDistancePipeType pipeType) {
        super(properties);
        this.pipeType = pipeType;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        // first find all neighbouring networks
        List<LongDistanceNetwork> networks = findNetworks(level, pos);
        if (networks.isEmpty()) {
            // create network
            LongDistanceNetwork network = this.pipeType.createNetwork(level);
            network.onPlacePipe(pos);
        } else if (networks.size() == 1) {
            // add to connected network
            networks.get(0).onPlacePipe(pos);
        } else {
            // merge all connected networks together
            LongDistanceNetwork main = networks.get(0);
            main.onPlacePipe(pos);
            networks.remove(0);
            for (LongDistanceNetwork network : networks) {
                main.mergePipeNet(network);
            }
        }
    }

    public List<LongDistanceNetwork> findNetworks(Level level, BlockPos pos) {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.MutableBlockPos offsetPos = new BlockPos.MutableBlockPos();
        for (Direction facing : GTUtil.DIRECTIONS) {
            offsetPos.set(pos).move(facing);
            LongDistanceNetwork network = LongDistanceNetwork.get(level, offsetPos);
            if (network != null && pipeType == network.getPipeType()) {
                ILDEndpoint endpoint = ILDEndpoint.tryGet(level, offsetPos);
                // only count the network as connected if it's not an endpoint or the endpoints input or output face is
                // connected
                if (endpoint == null || endpoint.getFrontFacing().getAxis() == facing.getAxis()) {
                    networks.add(network);
                }
            }
        }
        return networks;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level.isClientSide()) return;
        LongDistanceNetwork network = LongDistanceNetwork.get(level, pos);
        if (network != null) {
            network.onRemovePipe(pos);
        }
    }
}
