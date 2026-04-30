package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.gregtechceu.gtceu.utils.codec.StreamCodecUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public class ShapedEnergyTransferRecipe extends ShapedRecipe {

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge,
                                      ItemStack result, boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group), pattern,
                chargeIngredient, overrideCharge, transferMaxCharge, ItemStackTemplate.fromNonEmptyStack(result));
    }

    private ShapedEnergyTransferRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                       ShapedRecipePattern pattern, Ingredient chargeIngredient, boolean overrideCharge,
                                       boolean transferMaxCharge, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer);
        for (ItemStack chargeStack : chargeIngredient.items().map(holder -> new ItemStack(holder, 1)).toList()) {
            for (int i = 0; i < craftingContainer.size(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.set(GTDataComponents.ENERGY_CONTENT, new SimpleEnergyContent(maxCharge, charge));
                        return resultStack;
                    }
                }
            }
        }
        return resultStack;
    }

    public ItemStack getResultItem() {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = ((ShapedRecipeAccessor) this).getResult().create();
        for (ItemStack chargeStack : chargeIngredient.items().map(holder -> new ItemStack(holder, 1)).toList()) {
            IElectricItem electricItem = GTCapabilityHelper.getElectricItem(chargeStack);
            if (electricItem != null) {
                maxCharge += electricItem.getMaxCharge();
                charge += electricItem.getCharge();
                resultStack.set(GTDataComponents.ENERGY_CONTENT, new SimpleEnergyContent(maxCharge, charge));
                return resultStack;
            }
        }
        return resultStack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) GTRecipeSerializers.CRAFTING_SHAPED_ENERGY_TRANSFER
                .get();
    }

    public static class Serializer {

        public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                                .forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(val -> val.pattern),
                        Ingredient.CODEC.fieldOf("chargeIngredient")
                                .forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
                        Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
                        Codec.BOOL.fieldOf("transferMaxCharge")
                                .forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
                        ItemStack.CODEC.fieldOf("result")
                                .forGetter(val -> ((ShapedRecipeAccessor) val).getResult().create()),
                        Codec.BOOL.optionalFieldOf("show_notification", true)
                                .forGetter(ShapedRecipe::showNotification))
                        .apply(instance, ShapedEnergyTransferRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodecUtils
                .composite(
                        ByteBufCodecs.STRING_UTF8, ShapedRecipe::group,
                        CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                        ShapedRecipePattern.STREAM_CODEC, val -> val.pattern,
                        Ingredient.CONTENTS_STREAM_CODEC, ShapedEnergyTransferRecipe::getChargeIngredient,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isOverrideCharge,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isTransferMaxCharge,
                        ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getResult().create(),
                        ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                        ShapedEnergyTransferRecipe::new);
    }
}
