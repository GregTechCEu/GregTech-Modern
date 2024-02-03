package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class IntCircuitIngredient extends StrictNBTIngredient {
    public static final ResourceLocation TYPE = GTCEu.id("circuit");

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 32;

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static IntCircuitIngredient circuitInput(int configuration) {
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

    protected IntCircuitIngredient(int configuration) {
        super(IntCircuitBehaviour.stack(configuration));
        this.configuration = configuration;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return stack.is(GTItems.INTEGRATED_CIRCUIT.get()) && IntCircuitBehaviour.getCircuitConfiguration(stack) == this.configuration;
    }

    public IntCircuitIngredient copy() {
        return new IntCircuitIngredient(this.configuration);
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("configuration", configuration);
        return json;
    }

    @Override
    @Nonnull
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
            return new IntCircuitIngredient(configuration);
        }

        @Override
        public @NotNull IntCircuitIngredient parse(JsonObject json) {
            int configuration = json.get("configuration").getAsInt();
            return new IntCircuitIngredient(configuration);
        }

        @Override
        public void write(FriendlyByteBuf buffer, IntCircuitIngredient ingredient) {
            buffer.writeVarInt(ingredient.configuration);
        }
    };
}
