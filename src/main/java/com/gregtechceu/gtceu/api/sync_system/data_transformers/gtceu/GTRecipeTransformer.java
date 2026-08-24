package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTRecipeTransformer implements ValueTransformer<GTRecipe> {

    @Override
    public Tag serializeNBT(GTRecipe value, ValueTransformer.TransformerContext<GTRecipe> context) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", value.id.toString());
        tag.put("recipe",
                GTRecipeSerializer.CODEC.encode(value, context.nbtOps(), context.nbtOps().mapBuilder())
                        .build(new CompoundTag()).getOrThrow());
        tag.putInt("ocLevel", value.ocLevel);
        return tag;
    }

    @Override
    public @Nullable GTRecipe deserializeNBT(Tag tag, ValueTransformer.TransformerContext<GTRecipe> context) {
        if (tag instanceof CompoundTag comp && comp.isEmpty()) return null;
        GTRecipe result = null;
        if (tag instanceof CompoundTag compoundTag) {
            var recipeTag = compoundTag.get("recipe");
            result = GTRecipeSerializer.CODEC
                    .decode(context.nbtOps(), context.nbtOps().getMap(Objects.requireNonNull(recipeTag)).getOrThrow())
                    .result().orElse(null);
            if (result != null) {
                result.id = ResourceLocation.parse(compoundTag.getString("id"));
                result.ocLevel = compoundTag.getInt("ocLevel");
            }
        }
        return result;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, GTRecipe value, TransformerContext<GTRecipe> context) {
        buf.writeResourceLocation(value.id);
        GTRecipeSerializer.SERIALIZER.toNetwork(buf, value);
    }

    @Override
    public @Nullable GTRecipe readFromPacket(FriendlyByteBuf buf, TransformerContext<GTRecipe> context) {
        ResourceLocation id = buf.readResourceLocation();
        return GTRecipeSerializer.SERIALIZER.fromNetwork(id, buf);
    }
}
