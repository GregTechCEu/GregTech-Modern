package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardEmitter;
import com.gregtechceu.gtceu.common.pipelike.duct.*;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

public class DuctPipeBlockEntity extends PipeBlockEntity<DuctPipeType, DuctPipeProperties> {

    @Getter
    protected final EnumMap<Direction, DuctNetHandler> handlers = new EnumMap<>(Direction.class);
    // the DuctNetHandler can only be created on the server, so we have an empty placeholder for the client
    public final IHazardParticleContainer clientCapability = new DefaultDuctContainer();
    private WeakReference<DuctPipeNet> currentPipeNet = new WeakReference<>(null);
    @Getter
    protected DuctNetHandler defaultHandler;

    protected DuctPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static DuctPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new DuctPipeBlockEntity(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<DuctPipeBlockEntity> ductBlockEntityBlockEntityType) {}

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_HAZARD_CONTAINER) {
            if (getLevel().isClientSide())
                return GTCapability.CAPABILITY_HAZARD_CONTAINER.orEmpty(cap, LazyOptional.of(() -> clientCapability));

            if (handlers.isEmpty()) {
                initHandlers();
            }
            checkNetwork();
            return GTCapability.CAPABILITY_HAZARD_CONTAINER.orEmpty(cap,
                    LazyOptional.of(() -> handlers.getOrDefault(side, defaultHandler)));
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    public void initHandlers() {
        DuctPipeNet net = getDuctPipeNet();
        if (net == null) return;
        for (Direction facing : GTUtil.DIRECTIONS) {
            handlers.put(facing, new DuctNetHandler(net, this, facing));
        }
        defaultHandler = new DuctNetHandler(net, this, null);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            DuctPipeNet current = getDuctPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (DuctNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public DuctPipeNet getDuctPipeNet() {
        if (level == null || level.isClientSide) {
            return null;
        }
        DuctPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipePos())) {
            return currentPipeNet;
        }
        LevelDuctPipeNet worldNet = (LevelDuctPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getPipeLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof DuctPipeBlockEntity) {
                return false;
            }
            BlockPos relative = getBlockPos().relative(side);
            return GTCapabilityHelper.getHazardContainer(level, relative, side.getOpposite()) !=
                    null ||
                    (level.getBlockEntity(relative) instanceof IMachineBlockEntity machineBlockEntity &&
                            (machineBlockEntity.getMetaMachine() instanceof IEnvironmentalHazardCleaner ||
                                    machineBlockEntity.getMetaMachine() instanceof IEnvironmentalHazardEmitter));
        }
        return false;
    }

    private static class DefaultDuctContainer implements IHazardParticleContainer {

        @Override
        public boolean inputsHazard(Direction side, MedicalCondition condition) {
            return false;
        }

        @Override
        public float changeHazard(MedicalCondition condition, float differenceAmount) {
            return 0;
        }

        @Override
        public float getHazardStored(MedicalCondition condition) {
            return 0;
        }

        @Override
        public float getHazardCapacity(MedicalCondition condition) {
            return 0;
        }
    }
}
