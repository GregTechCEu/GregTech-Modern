package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class NBTPredicateIngredient extends AbstractIngredient {
    // TODO: We *need* to be able to serialize the condition to text, no way around it :(
    // tear out Predicate<Tag>
    public static final ResourceLocation TYPE = GTCEu.id("nbt_predicate");
    public static final Predicate<Tag> ALWAYS_TRUE = (ignored) -> true;
    private final Predicate<Tag> predicate;
    private final ItemStack stack;

    protected NBTPredicateIngredient(ItemStack stack, Predicate<Tag> predicate) {
        super(Stream.of(new Ingredient.ItemValue(stack)));
        this.stack = stack;
        this.predicate = predicate;
    }

    protected NBTPredicateIngredient(ItemStack stack) {
        this(stack, ALWAYS_TRUE);
    }

    public static NBTPredicateIngredient of(ItemStack stack, Predicate<Tag> predicate) {
        return new NBTPredicateIngredient(stack, predicate);
    }

    public static NBTPredicateIngredient of(ItemStack stack) {
        return NBTPredicateIngredient.of(stack, ALWAYS_TRUE);
    }

    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        } else {
            return this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue() && predicate.test(this.stack.getOrCreateTag());
        }
    }

    public boolean isSimple() {
        return false;
    }

    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return NBTPredicateIngredient.Serializer.INSTANCE;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(this.stack.getItem()).toString());
        json.addProperty("count", this.stack.getCount());
        if (this.stack.hasTag()) {
            json.addProperty("nbt", this.stack.getTag().toString());
        }

        return json;
    }

    public static class Serializer implements IIngredientSerializer<NBTPredicateIngredient> {
        public static final NBTPredicateIngredient.Serializer INSTANCE = new NBTPredicateIngredient.Serializer();

        public @NotNull NBTPredicateIngredient parse(FriendlyByteBuf buffer) {
            return new NBTPredicateIngredient(buffer.readItem());
        }

        public @NotNull NBTPredicateIngredient parse(@NotNull JsonObject json) {
            return new NBTPredicateIngredient(CraftingHelper.getItemStack(json, true));
        }

        public void write(FriendlyByteBuf buffer, NBTPredicateIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
        }
    }
}
