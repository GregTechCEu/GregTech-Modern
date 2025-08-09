package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class CPacketToolRightClick implements GTNetwork.INetPacket {

    private Vec3 hitLocation;
    private BlockPos hitPos;
    private Direction hitDirection;
    private final boolean miss;
    private boolean isHitInside;

    public CPacketToolRightClick(BlockHitResult blockHitResult) {
        hitLocation = blockHitResult.getLocation();
        hitPos = blockHitResult.getBlockPos();
        hitDirection = blockHitResult.getDirection();
        isHitInside = blockHitResult.isInside();
        miss = blockHitResult.getType() == HitResult.Type.MISS;
    }

    public CPacketToolRightClick(FriendlyByteBuf buffer) {
        miss = buffer.readBoolean();
        if (miss) return;
        hitLocation = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        hitPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        hitDirection = Direction.from3DDataValue(buffer.readInt());
        isHitInside = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(miss);
        if (miss) return;
        buffer.writeDouble(hitLocation.x);
        buffer.writeDouble(hitLocation.y);
        buffer.writeDouble(hitLocation.z);
        buffer.writeInt(hitPos.getX());
        buffer.writeInt(hitPos.getY());
        buffer.writeInt(hitPos.getZ());
        buffer.writeInt(hitDirection.get3DDataValue());
        buffer.writeBoolean(isHitInside);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (miss || hitDirection == null) return;
        ServerPlayer player = context.getSender();
        if (player == null) return;
        ServerLevel level = player.serverLevel();
        level.getBlockState(hitPos).use(level, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(hitLocation, hitDirection, hitPos, isHitInside));
    }
}
