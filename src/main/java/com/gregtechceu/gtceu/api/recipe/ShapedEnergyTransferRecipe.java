package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.Map;

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


    public ShapedEnergyTransferRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer, registryAccess);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.getOrCreateTag().putLong("MaxCharge", maxCharge);
                        resultStack.getOrCreateTag().putLong("Charge", charge);
                        return resultStack;
                    }
                }
            }
        }
        return resultStack;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.getResultItem(registryAccess);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            IElectricItem electricItem = GTCapabilityHelper.getElectricItem(chargeStack);
            if (electricItem != null) {
                maxCharge += electricItem.getMaxCharge();
                charge += electricItem.getCharge();
                resultStack.getOrCreateTag().putLong("MaxCharge", maxCharge);
                resultStack.getOrCreateTag().putLong("Charge", charge);
                return resultStack;
            }
        }
        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<ShapedEnergyTransferRecipe> {
        public static final Codec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapedRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(val -> ((ShapedRecipeAccessor)val).getPattern()),
            Ingredient.CODEC.fieldOf("chargeIngredient").forGetter(val -> val.chargeIngredient),
            Codec.BOOL.fieldOf("overrideCharge").forGetter(val -> val.overrideCharge),
            Codec.BOOL.fieldOf("transferMaxCharge").forGetter(val -> val.transferMaxCharge),
            ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor)val).getResult()),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(val -> ((ShapedRecipeAccessor)val).getShowNotification())
        ).apply(instance, ShapedEnergyTransferRecipe::new));

        @Override
        public Codec<ShapedEnergyTransferRecipe> codec() {
            return CODEC;
        }

        @Override
        public ShapedEnergyTransferRecipe fromNetwork(FriendlyByteBuf buffer) {
            boolean overrideCharge = buffer.readBoolean();
            boolean transferMaxCharge = buffer.readBoolean();
            Ingredient chargeIngredient = Ingredient.fromNetwork(buffer);
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            boolean showNotification = buffer.readBoolean();
            return new ShapedEnergyTransferRecipe(group, category, pattern, chargeIngredient, overrideCharge, transferMaxCharge, result, showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedEnergyTransferRecipe recipe) {
            buffer.writeBoolean(recipe.isOverrideCharge());
            buffer.writeBoolean(recipe.isTransferMaxCharge());
            recipe.getChargeIngredient().toNetwork(buffer);
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ((ShapedRecipeAccessor)recipe).getPattern().toNetwork(buffer);
            buffer.writeItem(((ShapedRecipeAccessor)recipe).getResult());
            buffer.writeBoolean(((ShapedRecipeAccessor) recipe).getShowNotification());
        }
    }

}
