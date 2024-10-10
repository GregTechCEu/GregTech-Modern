package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote GTRecipePayload
 */
public class GTRecipePayload extends ObjectTypedPayload<GTRecipe> {

    @Nullable
    @Override
    public Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", payload.id.toString());
        tag.put("recipe",
                GTRecipeSerializer.CODEC.encodeStart(NbtOps.INSTANCE, payload).result().orElse(new CompoundTag()));
        return tag;
    }

    @Override
    public void deserializeNBT(Tag tag) {
        RecipeManager recipeManager = Platform.getMinecraftServer().getRecipeManager();
        if (tag instanceof CompoundTag compoundTag) {
            payload = GTRecipeSerializer.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("recipe")).result().orElse(null);
            if (payload != null) {
                payload.id = new ResourceLocation(compoundTag.getString("id"));
            }
        } else if (tag instanceof StringTag stringTag) { // Backwards Compatibility
            var recipe = recipeManager.byKey(new ResourceLocation(stringTag.getAsString())).orElse(null);
            if (recipe instanceof GTRecipe gtRecipe) {
                payload = gtRecipe;
            } else if (recipe instanceof SmeltingRecipe smeltingRecipe) {
                payload = GTRecipeTypes.FURNACE_RECIPES.toGTrecipe(new ResourceLocation(stringTag.getAsString()),
                        smeltingRecipe);
            } else {
                payload = null;
            }
        } else if (tag instanceof ByteArrayTag byteArray) { // Backwards Compatibility
            ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(byteArray.getAsByteArray());
            FriendlyByteBuf buf = new FriendlyByteBuf(copiedDataBuffer);
            payload = (GTRecipe) recipeManager.byKey(buf.readResourceLocation()).orElse(null);
            buf.release();
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.payload.id);
        GTRecipeSerializer.SERIALIZER.toNetwork(buf, this.payload);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        var id = buf.readResourceLocation();
        if (buf.isReadable()) {
            this.payload = GTRecipeSerializer.SERIALIZER.fromNetwork(id, buf);
        } else { // Backwards Compatibility
            RecipeManager recipeManager;
            if (!Platform.isClient()) {
                recipeManager = Platform.getMinecraftServer().getRecipeManager();
            } else {
                recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
            }
            this.payload = (GTRecipe) recipeManager.byKey(id).orElse(null);
        }
    }
}
