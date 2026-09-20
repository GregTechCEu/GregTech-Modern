package com.gregtechceu.gtceu.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/** A non-mirrored shaped recipe that preserves explicit empty border cells. */
public class StrictShapedRecipe extends NormalCraftingRecipe {
    private static final MapCodec<ShapedRecipePattern.Data> PATTERN_CODEC =
            ShapedRecipePattern.Data.MAP_CODEC.validate(StrictShapedRecipe::validatePattern);
    public static final MapCodec<StrictShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            PATTERN_CODEC.forGetter(recipe -> recipe.patternData),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("matchSize", false).forGetter(recipe -> recipe.matchSize))
            .apply(instance, StrictShapedRecipe::new));
    // Encode the untrimmed data, not vanilla's trimmed ShapedRecipePattern.
    public static final StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(MAP_CODEC.codec());
    public static final RecipeSerializer<StrictShapedRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ShapedRecipePattern.Data patternData;
    private final List<Optional<Ingredient>> ingredients;
    private final ItemStackTemplate result;
    private final boolean matchSize;

    public StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                              ShapedRecipePattern.Data patternData, ItemStackTemplate result, boolean matchSize) {
        super(commonInfo, bookInfo);
        validatePattern(patternData).getOrThrow();
        this.patternData = new ShapedRecipePattern.Data(
                java.util.Map.copyOf(patternData.key()), List.copyOf(patternData.pattern()));
        this.result = result;
        this.matchSize = matchSize;
        var ingredients = new ArrayList<Optional<Ingredient>>();
        for (String row : patternData.pattern()) {
            for (int x = 0; x < row.length(); x++) {
                ingredients.add(Optional.ofNullable(patternData.key().get(row.charAt(x))));
            }
        }
        this.ingredients = List.copyOf(ingredients);
    }

    private static DataResult<ShapedRecipePattern.Data> validatePattern(ShapedRecipePattern.Data data) {
        if (data.pattern().isEmpty() || data.pattern().size() > ShapedRecipePattern.getMaxHeight()) {
            return DataResult.error(() -> "Invalid strict pattern height");
        }
        int width = data.pattern().getFirst().length();
        if (width == 0 || width > ShapedRecipePattern.getMaxWidth() || data.key().containsKey(' ')) {
            return DataResult.error(() -> "Invalid strict pattern width or reserved space key");
        }
        var unused = new HashSet<>(data.key().keySet());
        boolean hasIngredient = false;
        for (String row : data.pattern()) {
            if (row.length() != width) return DataResult.error(() -> "Strict pattern rows must have equal width");
            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                if (symbol == ' ') continue;
                if (!data.key().containsKey(symbol)) {
                    return DataResult.error(() -> "Undefined strict pattern symbol: " + symbol);
                }
                hasIngredient = true;
                unused.remove(symbol);
            }
        }
        if (!hasIngredient) return DataResult.error(() -> "Strict pattern must contain an ingredient");
        if (!unused.isEmpty()) return DataResult.error(() -> "Unused strict pattern symbols: " + unused);
        return DataResult.success(data);
    }

    public int getWidth() {
        return patternData.pattern().getFirst().length();
    }

    public int getHeight() {
        return patternData.pattern().size();
    }

    public boolean isMatchSize() {
        return matchSize;
    }

    public List<Optional<Ingredient>> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.isEmpty()) return false;
        var origin = input instanceof CraftingGridOrigin.Access access ? access.gtceu$getGridOrigin() : null;
        // Unknown physical size cannot safely satisfy an exact-grid-size recipe.
        if (origin == null && matchSize) return false;
        if (origin == null) origin = new CraftingGridOrigin(input.width(), input.height(), 0, 0);
        return matchesGrid(input, origin);
    }

    /** Also usable by crafting adapters that retain their original grid explicitly. */
    public boolean matchesGrid(CraftingInput input, CraftingGridOrigin origin) {
        if (input.isEmpty() || origin.left() < 0 || origin.top() < 0 ||
                origin.left() + input.width() > origin.width() ||
                origin.top() + input.height() > origin.height()) return false;
        if (matchSize && (origin.width() != getWidth() || origin.height() != getHeight())) return false;
        for (int offsetX = 0; offsetX <= origin.width() - getWidth(); offsetX++) {
            for (int offsetY = 0; offsetY <= origin.height() - getHeight(); offsetY++) {
                if (matchesAt(input, origin, offsetX, offsetY)) return true;
            }
        }
        return false;
    }

    private boolean matchesAt(CraftingInput input, CraftingGridOrigin origin, int offsetX, int offsetY) {
        for (int y = 0; y < origin.height(); y++) {
            for (int x = 0; x < origin.width(); x++) {
                int recipeX = x - offsetX;
                int recipeY = y - offsetY;
                var expected = recipeX >= 0 && recipeY >= 0 && recipeX < getWidth() && recipeY < getHeight()
                        ? ingredients.get(recipeX + recipeY * getWidth()) : Optional.<Ingredient>empty();
                int inputX = x - origin.left();
                int inputY = y - origin.top();
                var actual = inputX >= 0 && inputY >= 0 && inputX < input.width() && inputY < input.height()
                        ? input.getItem(inputX, inputY) : ItemStack.EMPTY;
                if (!Ingredient.testOptionalIngredient(expected, actual)) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(ingredients);
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(getWidth(), getHeight(),
                ingredients.stream().map(value -> value.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
                new SlotDisplay.ItemStackSlotDisplay(result), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public RecipeSerializer<StrictShapedRecipe> getSerializer() {
        return SERIALIZER;
    }
}
