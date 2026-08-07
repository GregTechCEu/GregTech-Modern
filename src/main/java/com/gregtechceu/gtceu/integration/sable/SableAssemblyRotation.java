package com.gregtechceu.gtceu.integration.sable;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;

public final class SableAssemblyRotation {

    private static final ThreadLocal<Rotation> CURRENT = new ThreadLocal<>();

    private SableAssemblyRotation() {}

    public static void set(Rotation rotation) {
        CURRENT.set(rotation);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Rotation current() {
        Rotation rotation = CURRENT.get();
        return rotation == null ? Rotation.NONE : rotation;
    }

    public static void rotatePipe(PipeBlockEntity<?, ?> pipe, Rotation rotation, HolderLookup.Provider registries) {
        if (rotation == Rotation.NONE) {
            return;
        }
        int connections = pipe.getConnections();
        int blocked = pipe.getBlockedConnections();
        rotateCovers(pipe.getCoverContainer(), rotation, registries);
        pipe.setConnections(rotateMask(connections, rotation));
        pipe.setBlockedConnections(rotateMask(blocked, rotation));
    }

    public static void rotateCovers(ICoverable covers, Rotation rotation, HolderLookup.Provider registries) {
        if (rotation == Rotation.NONE) {
            return;
        }
        CoverBehavior[] bySide = new CoverBehavior[GTUtil.DIRECTIONS.length];
        boolean any = false;
        for (Direction side : GTUtil.DIRECTIONS) {
            CoverBehavior cover = covers.getCoverAtSide(side);
            if (cover != null) {
                bySide[side.ordinal()] = cover;
                any = true;
            }
        }
        if (!any) {
            return;
        }
        for (Direction side : GTUtil.DIRECTIONS) {
            covers.setCoverAtSide(null, side);
        }
        for (Direction oldSide : GTUtil.DIRECTIONS) {
            CoverBehavior old = bySide[oldSide.ordinal()];
            if (old == null) {
                continue;
            }
            Direction newSide = rotation.rotate(oldSide);
            CoverBehavior moved = old.coverDefinition.createCoverBehavior(covers, newSide);
            if (moved == null) {
                continue;
            }
            CompoundTag data = old.getSyncDataHolder().serializeNBT(registries, false, false);
            moved.getSyncDataHolder().deserializeNBT(registries, data, false);
            covers.setCoverAtSide(moved, newSide);
        }
    }

    private static int rotateMask(int mask, Rotation rotation) {
        int rotated = 0;
        for (Direction side : GTUtil.DIRECTIONS) {
            if ((mask & (1 << side.ordinal())) != 0) {
                rotated |= 1 << rotation.rotate(side).ordinal();
            }
        }
        return rotated;
    }
}
