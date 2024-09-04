package com.gregtechceu.gtceu.api.graphnet.pipenet.logic;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.utils.DimensionFacingPos;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class EdgeCoverReferenceLogic extends NetLogicEntry<EdgeCoverReferenceLogic, CompoundTag> {

    public static final NetLogicEntryType<EdgeCoverReferenceLogic> TYPE = new NetLogicEntryType<>("EdgeCoverReference", EdgeCoverReferenceLogic::new);

    @Nullable
    private WeakReference<CoverBehavior> coverSource;
    private DimensionFacingPos coverSourcePos;
    @Nullable
    private WeakReference<CoverBehavior> coverTarget;
    private DimensionFacingPos coverTargetPos;

    public EdgeCoverReferenceLogic() {
        super(TYPE);
    }

    @Contract("_,_ -> this")
    public EdgeCoverReferenceLogic coverSource(@NotNull DimensionFacingPos pos, @NotNull CoverBehavior cover) {
        this.coverSource = new WeakReference<>(cover);
        this.coverSourcePos = pos;
        return this;
    }

    @Contract("_,_ -> this")
    public EdgeCoverReferenceLogic coverTarget(@NotNull DimensionFacingPos pos, @NotNull CoverBehavior cover) {
        this.coverTarget = new WeakReference<>(cover);
        this.coverTargetPos = pos;
        return this;
    }

    private @Nullable CoverBehavior getSource() {
        if (coverSource == null) return null;
        CoverBehavior ref = coverSource.get();
        if (ref == null) {
            Level world = Platform.getMinecraftServer().getLevel(coverSourcePos.getDimension());
            if (world == null || !world.isLoaded(coverSourcePos.getPos())) return null;

            BlockEntity tile = world.getBlockEntity(coverSourcePos.getPos());
            if (tile instanceof PipeBlockEntity pipe) {
                CoverBehavior cover = pipe.getCoverHolder().getCoverAtSide(coverSourcePos.getFacing());
                if (cover != null) {
                    this.coverSource = new WeakReference<>(cover);
                    return cover;
                } else {
                    // the cover doesn't exist, which makes no sense since we have a reference to its location but
                    // whatever
                    this.coverSource = null;
                    return null;
                }
            } else {
                // the pipe doesn't exist, which makes no sense since the edge holding us exists but whatever
                this.coverSource = null;
                return null;
            }
        } else {
            return ref;
        }
    }

    private @Nullable CoverBehavior getTarget() {
        if (coverTarget == null) return null;
        CoverBehavior ref = coverTarget.get();
        if (ref == null) {
            Level world = Platform.getMinecraftServer().getLevel(coverTargetPos.getDimension());
            if (world == null || !world.isLoaded(coverTargetPos.getPos())) return null;

            BlockEntity tile = world.getBlockEntity(coverTargetPos.getPos());
            if (tile instanceof PipeBlockEntity pipe) {
                CoverBehavior cover = pipe.getCoverHolder().getCoverAtSide(coverTargetPos.getFacing());
                if (cover != null) {
                    this.coverTarget = new WeakReference<>(cover);
                    return cover;
                } else {
                    // the cover doesn't exist, which makes no sense since we have a reference to its location but
                    // whatever
                    this.coverTarget = null;
                    return null;
                }
            } else {
                // the pipe doesn't exist, which makes no sense since the edge holding us exists but whatever
                this.coverTarget = null;
                return null;
            }
        } else {
            return ref;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("SourcePos", coverSourcePos.getPos().asLong());
        tag.putByte("SourceFacing", (byte) coverSourcePos.getFacing().ordinal());
        tag.putString("SourceDim", coverSourcePos.getDimension().location().toString());
        tag.putLong("TargetPos", coverTargetPos.getPos().asLong());
        tag.putByte("TargetFacing", (byte) coverTargetPos.getFacing().ordinal());
        tag.putString("TargetDim", coverTargetPos.getDimension().location().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.coverSourcePos = new DimensionFacingPos(BlockPos.of(nbt.getLong("SourcePos")),
                GTUtil.DIRECTIONS[nbt.getByte("SourceFacing")],
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("SourceDim"))));
        this.coverSource = new WeakReference<>(null);
        this.coverTargetPos = new DimensionFacingPos(BlockPos.of(nbt.getLong("TargetPos")),
                GTUtil.DIRECTIONS[nbt.getByte("TargetFacing")],
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("TargetDim"))));
        this.coverTarget = coverSource;
    }

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {}

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {}
}
