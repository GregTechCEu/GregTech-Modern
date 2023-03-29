package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class ItemRecipeCapability extends RecipeCapability<Ingredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, SerializerIngredient.INSTANCE);
    }

    @Override
    public Ingredient copyInner(Ingredient content) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        serializer.toNetwork(buf, content);
        return serializer.fromNetwork(buf);
    }

    @Override
    public Ingredient copyWithModifier(Ingredient content, ContentModifier modifier) {
        Ingredient copy = copyInner(content);
        return copy instanceof SizedIngredient sizedIngredient ? SizedIngredient.create(copy, modifier.apply(sizedIngredient.getAmount()).intValue()) : SizedIngredient.create(copy, modifier.apply(1).intValue());
    }

}
