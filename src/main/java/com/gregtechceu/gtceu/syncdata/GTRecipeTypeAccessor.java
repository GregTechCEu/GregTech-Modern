package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;

public class GTRecipeTypeAccessor extends CustomObjectAccessor<GTRecipeType> {

    public GTRecipeTypeAccessor() {
        super(GTRecipeType.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, GTRecipeType recipeType, HolderLookup.Provider provider) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        serializedHolder.writeResourceLocation(recipeType.registryName);
        return FriendlyBufPayload.of(serializedHolder);
    }

    @Override
    public GTRecipeType deserialize(AccessorOp accessorOp, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        if (payload instanceof FriendlyBufPayload buffer) {
            var id = buffer.getPayload().readResourceLocation();
            return (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(id);
        }
        return null;
    }
}
