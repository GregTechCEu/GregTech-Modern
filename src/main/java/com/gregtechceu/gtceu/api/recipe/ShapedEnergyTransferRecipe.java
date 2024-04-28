package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.components.ToolCharge;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.mojang.datafixers.util.Function8;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.function.Function;

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
    public ItemStack assemble(CraftingContainer craftingContainer, HolderLookup.Provider provider) {
        long maxCharge = 0L;
        long charge = 0L;
        ItemStack resultStack = super.assemble(craftingContainer, provider);
        for (ItemStack chargeStack : chargeIngredient.getItems()) {
            for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
                if (ItemStack.isSameItem(craftingContainer.getItem(i), chargeStack)) {
                    ItemStack stack = craftingContainer.getItem(i);
                    IElectricItem electricItem = stack.get(GTDataComponents.ELECTRIC_ITEM);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                        resultStack.set(GTDataComponents.TOOL_CHARGE, new ToolCharge(maxCharge, charge));
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
            IElectricItem electricItem = chargeStack.get(GTDataComponents.ELECTRIC_ITEM);
            if (electricItem != null) {
                maxCharge += electricItem.getMaxCharge();
                charge += electricItem.getCharge();
                resultStack.set(GTDataComponents.TOOL_CHARGE, new ToolCharge(maxCharge, charge));
                return resultStack;
            }
        }
        return resultStack;
    }

    public static class Serializer implements RecipeSerializer<ShapedEnergyTransferRecipe> {
        public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(val -> ((ShapedRecipeAccessor)val).getPattern()),
            Ingredient.CODEC.fieldOf("chargeIngredient").forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
            Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
            Codec.BOOL.fieldOf("transferMaxCharge").forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
            ItemStack.CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor)val).getResult()),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(val -> ((ShapedRecipeAccessor)val).getShowNotification())
        ).apply(instance, ShapedEnergyTransferRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = composite(
            ByteBufCodecs.STRING_UTF8, ShapedRecipe::getGroup,
            CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
            ShapedRecipePattern.STREAM_CODEC, val -> ((ShapedRecipeAccessor)val).getPattern(),
            Ingredient.CONTENTS_STREAM_CODEC, ShapedEnergyTransferRecipe::getChargeIngredient,
            ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isOverrideCharge,
            ByteBufCodecs.BOOL, ShapedEnergyTransferRecipe::isTransferMaxCharge,
            ItemStack.STREAM_CODEC, val -> ((ShapedRecipeAccessor)val).getResult(),
            ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
            ShapedEnergyTransferRecipe::new
        );

        @Override
        public MapCodec<ShapedEnergyTransferRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> codec1,
        final Function<C, T1> getter1,
        final StreamCodec<? super B, T2> codec2,
        final Function<C, T2> getter2,
        final StreamCodec<? super B, T3> codec3,
        final Function<C, T3> getter3,
        final StreamCodec<? super B, T4> codec4,
        final Function<C, T4> getter4,
        final StreamCodec<? super B, T5> codec5,
        final Function<C, T5> getter5,
        final StreamCodec<? super B, T6> codec6,
        final Function<C, T6> getter6,
        final StreamCodec<? super B, T7> codec7,
        final Function<C, T7> getter7,
        final StreamCodec<? super B, T8> codec8,
        final Function<C, T8> getter8,
        final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> p_331335_) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                return p_331335_.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
            }
        };
    }

}
