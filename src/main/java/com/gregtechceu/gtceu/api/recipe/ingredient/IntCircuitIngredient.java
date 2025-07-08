package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.core.mixins.forge.StrictNBTIngredientAccessor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntCircuitIngredient extends StrictNBTIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("circuit");

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 32;

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static IntCircuitIngredient of(int configuration) {
        if (configuration < CIRCUIT_MIN || configuration > CIRCUIT_MAX) {
            throw new IndexOutOfBoundsException("Circuit configuration " + configuration + " is out of range");
        }
        IntCircuitIngredient ingredient = INGREDIENTS[configuration];
        if (ingredient == null) {
            INGREDIENTS[configuration] = ingredient = new IntCircuitIngredient(configuration);
        }
        return ingredient;
    }

    private final int configuration;
    private ItemStack[] stacks;

    private IntCircuitIngredient(int configuration) {
        super(IntCircuitBehaviour.stack(configuration));
        this.configuration = configuration;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return stack.is(GTItems.PROGRAMMED_CIRCUIT.get()) &&
                IntCircuitBehaviour.getCircuitConfiguration(stack) == this.configuration;
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (stacks == null) {
            stacks = new ItemStack[] { ((StrictNBTIngredientAccessor) this).getStack() };
        }
        return stacks;
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("configuration", configuration);
        return json;
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static IntCircuitIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    public static final IIngredientSerializer<IntCircuitIngredient> SERIALIZER = new IIngredientSerializer<>() {

        @Override
        public @NotNull IntCircuitIngredient parse(FriendlyByteBuf buffer) {
            int configuration = buffer.readVarInt();
            return of(configuration);
        }

        @Override
        public @NotNull IntCircuitIngredient parse(JsonObject json) {
            int configuration = json.get("configuration").getAsInt();
            return of(configuration);
        }

        @Override
        public void write(FriendlyByteBuf buffer, IntCircuitIngredient ingredient) {
            buffer.writeVarInt(ingredient.configuration);
        }
    };
}
