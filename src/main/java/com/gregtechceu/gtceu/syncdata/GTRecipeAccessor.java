package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.accessor.CustomObjectAccessor;
import com.lowdragmc.lowdraglib.syncdata.payload.FriendlyBufPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

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
    public ITypedPayload<?> serialize(AccessorOp accessorOp, GTRecipe gtRecipe) {
        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        GTRecipeSerializer.SERIALIZER.toNetwork(serializedHolder, gtRecipe);
        return FriendlyBufPayload.of(serializedHolder);
    }

    @Override
    public GTRecipe deserialize(AccessorOp accessorOp, ITypedPayload<?> payload) {
        if (payload instanceof FriendlyBufPayload buffer) {
            return GTRecipeSerializer.SERIALIZER.fromNetwork(buffer.getPayload());
        }
        return null;
    }
}
