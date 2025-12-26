package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.server.ServerLifecycleHooks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTRecipeTransformer implements IValueTransformer<GTRecipe> {

    private static RecipeManager getRecipeManager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && Thread.currentThread() == server.getRunningThread()) {
            return server.getRecipeManager();
        } else {
            return Objects.requireNonNull(Minecraft.getInstance().getConnection()).getRecipeManager();
        }
    }

    @Override
    public Tag serializeNBT(GTRecipe value, ISyncManaged holder) {
        CompoundTag tag = new CompoundTag();
        if (value == null) return tag;
        tag.putString("id", value.id.toString());
        tag.put("recipe",
                GTRecipeSerializer.CODEC.encodeStart(NbtOps.INSTANCE, value).result().orElse(new CompoundTag()));
        tag.putInt("parallels", value.parallels);
        tag.putInt("ocLevel", value.ocLevel);
        return tag;
    }

    @Override
    public GTRecipe deserializeNBT(Tag tag, ISyncManaged holder, @Nullable GTRecipe currentVal) {
        if (tag instanceof CompoundTag comp && comp.isEmpty()) return null;
        RecipeManager recipeManager = getRecipeManager();
        GTRecipe result = null;
        if (tag instanceof CompoundTag compoundTag) {
            result = GTRecipeSerializer.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("recipe")).result().orElse(null);
            if (result != null) {
                result.id = new ResourceLocation(compoundTag.getString("id"));
                result.parallels = compoundTag.contains("parallels") ? compoundTag.getInt("parallels") : 1;
                result.ocLevel = compoundTag.getInt("ocLevel");
            }
        } else if (tag instanceof StringTag stringTag) { // Backwards Compatibility
            var recipe = recipeManager.byKey(new ResourceLocation(stringTag.getAsString())).orElse(null);
            if (recipe instanceof GTRecipe gtRecipe) {
                result = gtRecipe;
            } else if (recipe instanceof SmeltingRecipe smeltingRecipe) {
                result = GTRecipeTypes.FURNACE_RECIPES.toGTrecipe(new ResourceLocation(stringTag.getAsString()),
                        smeltingRecipe);
            }
        } else if (tag instanceof ByteArrayTag byteArray) { // Backwards Compatibility
            ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(byteArray.getAsByteArray());
            FriendlyByteBuf buf = new FriendlyByteBuf(copiedDataBuffer);
            result = (GTRecipe) recipeManager.byKey(buf.readResourceLocation()).orElse(null);
            buf.release();
        }
        return result;
    }
}
