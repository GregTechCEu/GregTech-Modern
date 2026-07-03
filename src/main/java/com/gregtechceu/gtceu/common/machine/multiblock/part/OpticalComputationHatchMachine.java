package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MultiblockComputationPortTrait;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpticalComputationHatchMachine extends MultiblockPartMachine {

    @Getter
    private final boolean transmitter;

    @Getter
    protected MultiblockComputationPortTrait computationPort;

    public OpticalComputationHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
        this.computationPort = new MultiblockComputationPortTrait(this, transmitter, !transmitter);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        markComputationTopologyDirty();
    }

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        markComputationTopologyDirty();
    }

    private void markComputationTopologyDirty() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            ComputationNetworkManager.get(serverLevel).markTopologyDirty();
        }
    }
}
