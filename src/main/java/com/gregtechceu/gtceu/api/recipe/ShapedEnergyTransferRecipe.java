package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.StreamCodecUtils;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

/**
 * @author Irgendwer01
 * @date 2023/11/4
 * @implNote ShapedEnergyTransferRecipe
 */
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ShapedEnergyTransferRecipe extends ShapedRecipe {

    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER = new Serializer();

    @Getter
    private final Ingredient chargeIngredient;
    @Getter
    private final boolean transferMaxCharge;
    @Getter
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge,
                                      ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider provider) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer, provider);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
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

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.getResultItem(provider);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
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

    public static class Serializer implements RecipeSerializer<ShapedEnergyTransferRecipe> {

        public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                                .forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(val -> ((ShapedRecipeAccessor) val).getPattern()),
                        Ingredient.CODEC.fieldOf("chargeIngredient")
                                .forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
                        Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
                        Codec.BOOL.fieldOf("transferMaxCharge")
                                .forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
                        ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult()),
                        Codec.BOOL.optionalFieldOf("show_notification", true)
                                .forGetter(val -> ((ShapedRecipeAccessor) val).getShowNotification()))
                        .apply(instance, ShapedEnergyTransferRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodecUtils
                .composite(
                        ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
                        CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
                        ShapedRecipePattern.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getPattern(),
                        Ingredient.CONTENTS_STREAM_CODEC, ShapedEnergyTransferRecipe::getChargeIngredient,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isOverrideCharge,
                        ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isTransferMaxCharge,
                        ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor) val).getResult(),
                        ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
                        ShapedEnergyTransferRecipe::new);

        @Override
        public MapCodec<ShapedEnergyTransferRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
