package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class GTMaterialAccessor extends CustomObjectAccessor<Material> {
    public GTMaterialAccessor() {
        super(Material.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, Material material) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        if(material != null) {
            serializedHolder.writeBoolean(true);
            serializedHolder.writeResourceLocation(material.getResourceLocation());
        } else {
            serializedHolder.writeBoolean(false);
        }
        return FriendlyBufPayload.of(serializedHolder);
    }


    @Override
    public Material deserialize(AccessorOp accessorOp, ITypedPayload<?> payload) {
        if (payload instanceof FriendlyBufPayload buffer && buffer.getPayload().readBoolean()) {
            var id = buffer.getPayload().readResourceLocation();
            return GTCEuAPI.materialManager.getMaterial(id.toString());
        }
        return null;
    }
}