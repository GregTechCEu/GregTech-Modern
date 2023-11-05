package com.gregtechceu.gtceu.api.pipenet.longdistance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class LongDistancePipeBlock extends Block implements ILDNetworkPart {

    private final LongDistancePipeType pipeType;

    public LongDistancePipeBlock(BlockBehaviour.Properties properties, LongDistancePipeType pipeType) {
        super(properties);
        this.pipeType = pipeType;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        // first find all neighbouring networks
        List<LongDistanceNetwork> networks = findNetworks(level, pos);
        if (networks == null) return;

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

    @Nullable
    private List<LongDistanceNetwork> findNetworks(Level level, BlockPos pos) {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.MutableBlockPos offsetPos = new BlockPos.MutableBlockPos();
        for (Direction facing : Direction.values()) {
            offsetPos.set(pos).move(facing);
            BlockState neighborState = level.getBlockState(offsetPos);
            ILDNetworkPart networkPart = ILDNetworkPart.tryGet(level, offsetPos, neighborState);

            if (networkPart != null && networkPart.getPipeType() == getPipeType()) {
                LongDistanceNetwork network = LongDistanceNetwork.get(level, offsetPos);
                if (network == null) {
                    // if for some reason there is not a network at the neighbor, create one
                    network = networkPart.getPipeType().createNetwork(level);
                    network.recalculateNetwork(Collections.singleton(offsetPos.immutable()));
                    return null; // TODO find a better way to return from the caller here
                }
                if (!network.getPipeType().isValidPart(networkPart)) {
                    throw new IllegalStateException();
                }

                ILDEndpoint endpoint = ILDEndpoint.tryGet(level, offsetPos);
                // only count the network as connected if it's not an endpoint or the endpoints input or output face is connected
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

    @Override
    public @NotNull LongDistancePipeType getPipeType() {
        return pipeType;
    }
}