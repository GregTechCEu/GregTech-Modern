package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.gregtechceu.gtceu.api.machines.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistanceNetwork;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class LongDistanceEndpointMachine extends MetaMachine implements ILDEndpoint, IDataInfoProvider {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LongDistanceEndpointMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @NotNull
    @Getter
    private final LongDistancePipeType pipeType;
    @Persisted @Getter @Setter
    private IO ioType = IO.NONE;
    private ILDEndpoint link;
    private boolean placed = false;

    public LongDistanceEndpointMachine(IMachineBlockEntity holder, LongDistancePipeType pipeType) {
        super(holder);
        this.pipeType = Objects.requireNonNull(pipeType);
    }

    public void updateNetwork() {
        if (this.getLevel().isClientSide) {
            return;
        }
        LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
        if (network != null) {
            // manually remove this endpoint from the network
            network.onRemoveEndpoint(this);
        }

        // find networks on input and output face
        List<LongDistanceNetwork> networks = findNetworks();
        if (networks.isEmpty()) {
            // no neighbours found, create new network
            network = this.pipeType.createNetwork(getLevel());
            network.onPlaceEndpoint(this);
            setIoType(IO.NONE);
        } else if (networks.size() == 1) {
            // one neighbour network found, attach self to neighbour network
            networks.get(0).onPlaceEndpoint(this);
        } else {
            // two neighbour networks found, configuration invalid
            setIoType(IO.NONE);
        }
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        this.placed = true;
        super.setFrontFacing(frontFacing);
        if (getLevel() != null && !getLevel().isClientSide) {
            updateNetwork();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.updateNetwork();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (this.getLevel().isClientSide) return;
        if (link != null) {
            // invalidate linked endpoint
            link.invalidateLink();
            invalidateLink();
        }
        setIoType(IO.NONE);
        LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
        // remove endpoint from network
        if (network != null) network.onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        if (!placed || getLevel().isClientSide) return;

        List<LongDistanceNetwork> networks = findNetworks();
        this.updateNetwork();
        LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
        if (network == null) {
            // shouldn't happen
            if (networks.isEmpty()) {
                // create new network since there are no neighbouring networks
                network = this.pipeType.createNetwork(getLevel());
                network.onPlaceEndpoint(this);
            } else if (networks.size() == 1) {
                // add to neighbour network
                networks.get(0).onPlaceEndpoint(this);
            }
        } else {
            if (networks.size() > 1) {
                // suddenly there are more than one neighbouring networks, invalidate
                onUnload();
            }
        }
        if (networks.size() != 1) {
            setIoType(IO.NONE);
        }
    }

    @Override
    public void notifyBlockUpdate() {
        super.notifyBlockUpdate();
        this.updateNetwork();
    }

    private List<LongDistanceNetwork> findNetworks() {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        LongDistanceNetwork network;
        // only check input and output side
        network = LongDistanceNetwork.get(getLevel(), getPos().relative(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            // found a network on the input face, therefore this is an output of the network
            networks.add(network);
            setIoType(IO.OUT);
        }
        network = LongDistanceNetwork.get(getLevel(), getPos().relative(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            // found a network on the output face, therefore this is an input of the network
            networks.add(network);
            setIoType(IO.IN);
        }
        return networks;
    }

    @Override
    public ILDEndpoint getLink() {
        if (link == null) {
            LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
            if (network != null && network.isValid()) {
                this.link = network.getOtherEndpoint(this);
            }
        } else if (this.link.isInValid()) {
            this.link.invalidateLink();
            this.link = null;
            LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
            if (network != null) {
                network.invalidateEndpoints();
                if (network.isValid()) {
                    this.link = network.getOtherEndpoint(this);
                }
            }
        }
        return this.link;
    }

    @Override
    public void invalidateLink() {
        this.link = null;
    }

    @Override
    public Direction getOutputFacing() {
        return getFrontFacing().getOpposite();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        List<Component> textComponents = new ArrayList<>();

        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            LongDistanceNetwork network = LongDistanceNetwork.get(getLevel(), getPos());
            if (network == null) {
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_no_network"));
            } else {
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_network_header"));
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_pipe_count", FormattingUtil.formatNumbers(network.getTotalSize())));
                ILDEndpoint in = network.getActiveInputIndex(), out = network.getActiveOutputIndex();
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_input_pos", Component.literal(in == null ? "none" : in.getPos().toString())));
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_output_pos", Component.literal(out == null ? "none" : out.getPos().toString())));
            }
            if (isInput()) {
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_input_endpoint"));
            }
            if (isOutput()) {
                textComponents.add(Component.translatable("block.gtceu.long_distance_item_pipeline_output_endpoint"));
            }
        }

        return textComponents;
    }
}