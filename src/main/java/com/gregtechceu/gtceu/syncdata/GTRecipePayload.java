package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

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
    public Tag serializeNBT(HolderLookup.Provider provider) {
        return StringTag.valueOf(payload.id.toString());
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        RecipeManager recipeManager = Platform.getMinecraftServer().getRecipeManager();
        if (tag instanceof StringTag stringTag) {
            RecipeHolder<?> holder = recipeManager.byKey(ResourceLocation.parse(stringTag.getAsString())).orElse(null);
            this.payload = holder == null ? null : (GTRecipe) holder.value();
        } else if (tag instanceof ByteArrayTag byteArray) {
            ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(byteArray.getAsByteArray());
            FriendlyByteBuf buf = new FriendlyByteBuf(copiedDataBuffer);
            RecipeHolder<?> holder = recipeManager.byKey(buf.readResourceLocation()).orElse(null);
            this.payload = holder == null ? null : (GTRecipe) holder.value();
            buf.release();
        }
    }

    @Override
    public void writePayload(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.payload.id);
    }

    @Override
    public void readPayload(RegistryFriendlyByteBuf buf) {
        RecipeManager recipeManager;
        if (!Platform.isClient()) {
            recipeManager = Platform.getMinecraftServer().getRecipeManager();
        } else {
            recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        }
        RecipeHolder<?> holder = recipeManager.byKey(buf.readResourceLocation()).orElse(null);
        this.payload = holder == null ? null : (GTRecipe) holder.value();
    }
}
