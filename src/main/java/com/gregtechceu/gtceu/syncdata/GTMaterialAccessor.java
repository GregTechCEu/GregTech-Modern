package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;

import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;

public class GTMaterialAccessor extends CustomObjectAccessor<Material> {

    public GTMaterialAccessor() {
        super(Material.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, Material material) {
        if (accessorOp == AccessorOp.PERSISTED) {
            StringTag tag = StringTag.valueOf(material.getResourceLocation().toString());
            return NbtTagPayload.of(tag);
        } else {
            FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
            serializedHolder.writeUtf(material.getResourceLocation().toString());
            return FriendlyBufPayload.of(serializedHolder);
        }
    }

    @Override
    public Material deserialize(AccessorOp accessorOp, ITypedPayload<?> payload) {
        if (payload instanceof FriendlyBufPayload buffer) {
            var id = buffer.getPayload().readUtf();
            return GTCEuAPI.materialManager.getMaterial(id);
        } else if (payload instanceof NbtTagPayload nbt) {
            var id = nbt.getPayload().getAsString();
            return GTCEuAPI.materialManager.getMaterial(id);
        }
        return null;
    }
}
