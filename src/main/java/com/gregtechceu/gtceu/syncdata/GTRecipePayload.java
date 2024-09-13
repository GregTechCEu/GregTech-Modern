package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

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
        return StringTag.valueOf(payload.id.toString());
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof StringTag stringTag) {
            RecipeManager recipeManager = Platform.getMinecraftServer().getRecipeManager();
            payload = (GTRecipe) recipeManager.byKey(new ResourceLocation(stringTag.getAsString())).orElse(null);
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.payload.id);
    }

    @Override
    public void readPayload(FriendlyByteBuf buf) {
        RecipeManager recipeManager;
        if (!Platform.isClient()) {
            recipeManager = Platform.getMinecraftServer().getRecipeManager();
        } else {
            recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        }
        this.payload = (GTRecipe) recipeManager.byKey(buf.readResourceLocation()).orElse(null);
    }
}
