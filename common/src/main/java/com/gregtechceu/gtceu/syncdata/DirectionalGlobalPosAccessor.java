package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.data.DirectionalGlobalPos;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;


public class DirectionalGlobalPosAccessor extends CustomObjectAccessor<DirectionalGlobalPos> {
    public DirectionalGlobalPosAccessor() {
        super(DirectionalGlobalPos.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, DirectionalGlobalPos value) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        serializedHolder.writeResourceKey(value.position().dimension());
        serializedHolder.writeBlockPos(value.position().pos());
        serializedHolder.writeEnum(value.direction());

        return FriendlyBufPayload.of(serializedHolder);
    }

    @Override
    public DirectionalGlobalPos deserialize(AccessorOp op, ITypedPayload<?> payload) {
        if (payload instanceof FriendlyBufPayload friendlyBufPayload) {
            FriendlyByteBuf buffer = friendlyBufPayload.getPayload();
            var dimension = buffer.readResourceKey(Registry.DIMENSION_REGISTRY);
            var blockPos = buffer.readBlockPos();
            var direction = buffer.readEnum(Direction.class);

            return new DirectionalGlobalPos(GlobalPos.of(dimension, blockPos), direction);
        }
        return null;
    }
}
