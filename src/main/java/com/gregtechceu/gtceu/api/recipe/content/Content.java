package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import brachy.modularui.screen.RichTooltip;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public record Content(Object content, int chance, int maxChance, int tierChanceBoost) {

    public Content(Object content, int chance, int maxChance, int tierChanceBoost) {
        this.content = content;
        this.chance = chance;
        this.maxChance = maxChance;
        this.tierChanceBoost = fixBoost(tierChanceBoost);
    }

    public static <T> Codec<Content> codec(RecipeCapability<T> capability) {
        return RecordCodecBuilder.create(instance -> instance.group(
                capability.serializer.codec().fieldOf("content").forGetter(val -> capability.of(val.content)),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("chance", ChanceLogic.getMaxChancedValue())
                        .forGetter(val -> val.chance),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("maxChance", ChanceLogic.getMaxChancedValue())
                        .forGetter(val -> val.maxChance),
                Codec.INT.optionalFieldOf("tierChanceBoost", 0)
                        .forGetter(val -> val.tierChanceBoost))
                .apply(instance, Content::new));
    }

    /**
     * Directly copies a Content.
     */
    public Content copy(RecipeCapability<?> capability) {
        return new Content(capability.copyContent(content), chance, maxChance, tierChanceBoost);
    }

    /**
     * Applies a {@link ContentModifier} to a Content. Does not apply the Modifier if the Content has a Chance.
     */
    public Content copy(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
        if (modifier == ContentModifier.IDENTITY || chance < maxChance) {
            return copy(capability);
        } else {
            return new Content(capability.copyContent(content, modifier), chance, maxChance, tierChanceBoost);
        }
    }

    /**
     * Applies a {@link ContentModifier} to a Content. Even if the content has a Chance.
     */
    public Content copyChanced(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
        if (modifier == ContentModifier.IDENTITY) {
            return copy(capability);
        } else {
            return new Content(capability.copyContent(content, modifier), chance, maxChance, tierChanceBoost);
        }
    }

    public boolean isChanced() {
        return chance > 0 && chance < maxChance;
    }

    /**
     * Attempts to fix and round the given chance boost due to potential differences
     * between the max chance and {@link ChanceLogic#getMaxChancedValue()}.
     * <br />
     * The worst case would be {@code 5,001 / 10,000} , meaning the boost would
     * have to be halved to have the intended effect.
     *
     * @param chanceBoost the chance boost to be fixed
     * @return the fixed chance boost
     */
    private int fixBoost(int chanceBoost) {
        float error = (float) ChanceLogic.getMaxChancedValue() / maxChance;
        int fixed = Math.round(Math.abs(chanceBoost) / error);
        return chanceBoost < 0 ? -fixed : fixed;
    }

    public static void addChanceTooltips(RichTooltip tooltip, Content content, ChanceLogic logic, int recipeTier,
                                         int chanceTier, ChanceBoostFunction function) {
        if (content.chance() < ChanceLogic.getMaxChancedValue()) {
            int boostedChance = function.getBoostedChance(content, recipeTier, chanceTier);
            if (boostedChance == 0) {
                tooltip.addLine(Component.translatable("gtceu.gui.content.chance_nc"));
            } else {
                float baseChanceFloat = 100f * content.chance() / content.maxChance();
                if (content.tierChanceBoost() != 0) {
                    float boostedChanceFloat = 100f * boostedChance / content.maxChance();

                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltip.addLine(Component.translatable("gtceu.gui.content.chance_base_logic",
                                FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltip.addLine(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_base",
                                        baseChanceFloat));
                    }

                    String key = "gtceu.gui.content.chance_tier_boost_" +
                            ((content.tierChanceBoost() > 0) ? "plus" : "minus");
                    tooltip.addLine(FormattingUtil.formatPercentage2Places(key,
                            Math.abs(100f * content.tierChanceBoost() / content.maxChance())));

                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltip.addLine(Component.translatable("gtceu.gui.content.chance_boosted_logic",
                                FormattingUtil.formatNumber2Places(boostedChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltip.addLine(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_boosted",
                                        boostedChanceFloat));
                    }
                } else {
                    if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
                        tooltip.addLine(Component.translatable("gtceu.gui.content.chance_no_boost_logic",
                                FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation())
                                .withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltip.addLine(
                                FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_no_boost",
                                        baseChanceFloat));
                    }
                }
            }
        }
    }
}
