package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.feature.IFrontFacingTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRenderingTrait;
import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

public class MachineCoverContainer extends MachineTrait
                                   implements IFrontFacingTrait, IRenderingTrait, ICoverable, ISyncManaged {

    public static final MachineTraitType<MachineCoverContainer> TYPE = new MachineTraitType<>(
            MachineCoverContainer.class);

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);
    @Getter
    private final MetaMachine machine;
    @SyncToClient
    @SaveField
    @RerenderOnChanged
    private @Nullable CoverBehavior up, down, north, south, west, east;

    public MachineCoverContainer(MetaMachine machine) {
        this.machine = machine;
    }

    @Override
    public MachineTraitType<?> getTraitType() {
        return TYPE;
    }

    @Override
    public IGregtechBlockEntity getHolder() {
        return machine;
    }

    @Override
    public void onMachineLoad() {
        onLoad();
    }

    @Override
    public void onMachineUnload() {
        onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        for (Direction direction : GTUtil.DIRECTIONS) {
            removeCover(direction, null);
        }
    }

    @Override
    public boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                           Set<GTToolType> toolTypes) {
        for (CoverBehavior cover : getCovers()) {
            if (cover.shouldRenderGrid(player, pos, state, held, toolTypes)) return true;
        }
        return false;
    }

    @Override
    public @Nullable ResourceTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                        Set<GTToolType> toolTypes, Direction side) {
        var cover = getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, pos, state, toolTypes, side);
        }
        return null;
    }

    @Override
    public boolean isValidFrontFace(Direction direction) {
        if (hasCover(direction)) {
            // noinspection DataFlowIssue
            var coverDefinition = getCoverAtSide(direction).coverDefinition;
            var behaviour = coverDefinition.createCoverBehavior(this, getFrontFacing());
            return behaviour.canAttach();
        }
        return true;
    }

    @Override
    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        onNeighborChanged(block, fromPos, isMoving);
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        ArrayList<VoxelShape> collisionList = new ArrayList<>();
        machine.addCollisionBoundingBox(collisionList);
        // noinspection RedundantIfStatement
        if (ICoverable.doesCoverCollide(side, collisionList, getCoverPlateThickness())) {
            // cover collision box overlaps with meta tile entity collision box
            return false;
        }

        return true;
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public Direction getFrontFacing() {
        return machine.getFrontFacing();
    }

    @Override
    public boolean shouldRenderBackSide() {
        return !machine.getBlockState().canOcclude();
    }

    @Override
    public @Nullable CoverBehavior getCoverAtSide(Direction side) {
        return switch (side) {
            case UP -> up;
            case SOUTH -> south;
            case WEST -> west;
            case DOWN -> down;
            case EAST -> east;
            case NORTH -> north;
        };
    }

    @Override
    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
        switch (side) {
            case UP -> up = coverBehavior;
            case SOUTH -> south = coverBehavior;
            case WEST -> west = coverBehavior;
            case DOWN -> down = coverBehavior;
            case EAST -> east = coverBehavior;
            case NORTH -> north = coverBehavior;
        }
        getSyncDataHolder().resyncAllFields();
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getFluidHandlerCap(side, useCoverCapability);
    }
}
