package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipes.GTRecipe;
import com.gregtechceu.gtceu.api.recipes.GTRecipeSerializer;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote GTRecipeAccessor
 */
public class GTRecipeAccessor extends CustomObjectAccessor<GTRecipe> {

    public GTRecipeAccessor() {
        super(GTRecipe.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp accessorOp, GTRecipe gtRecipe, HolderLookup.Provider provider) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        gtRecipe.toNetwork(new RegistryFriendlyByteBuf(serializedHolder, (RegistryAccess) provider));
        return FriendlyBufPayload.of(serializedHolder);
    }

    @Override
    public GTRecipe deserialize(AccessorOp accessorOp, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        if (payload instanceof FriendlyBufPayload buffer) {
            return GTRecipeSerializer.SERIALIZER.fromNetwork(new RegistryFriendlyByteBuf(buffer.getPayload(), (RegistryAccess) provider));
        }
        return null;
    }
}
