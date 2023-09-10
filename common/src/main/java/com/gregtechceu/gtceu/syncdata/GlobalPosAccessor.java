package com.gregtechceu.gtceu.syncdata;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;


public class GlobalPosAccessor extends CustomObjectAccessor<GlobalPos> {
    public GlobalPosAccessor() {
        super(GlobalPos.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, GlobalPos value) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        serializedHolder.writeResourceKey(value.dimension());
        serializedHolder.writeBlockPos(value.pos());

        return FriendlyBufPayload.of(serializedHolder);
    }

    @Override
    public GlobalPos deserialize(AccessorOp op, ITypedPayload<?> payload) {
        if (payload instanceof FriendlyBufPayload friendlyBufPayload) {
            FriendlyByteBuf buffer = friendlyBufPayload.getPayload();
            var dimension = buffer.readResourceKey(Registry.DIMENSION_REGISTRY);
            var blockPos = buffer.readBlockPos();

            return GlobalPos.of(dimension, blockPos);
        }
        return null;
    }
}
