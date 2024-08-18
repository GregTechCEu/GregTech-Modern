package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class Content {

    public RecipeCapability<?> capability;
    @Getter
    public Object content;
    public int chance;
    public int maxChance;
    public int tierChanceBoost;
    @Nullable
    public String slotName;
    @Nullable
    public String uiName;

    public Content(Object content, int chance, int maxChance, int tierChanceBoost, @Nullable String slotName,
                   @Nullable String uiName) {
        this.content = content;
        this.chance = chance;
        this.maxChance = maxChance;
        this.tierChanceBoost = fixBoost(tierChanceBoost);
        this.slotName = slotName == null || slotName.isEmpty() ? null : slotName;
        this.uiName = uiName == null || uiName.isEmpty() ? null : uiName;
    }

    public static <T> Codec<Content> codec(RecipeCapability<T> capability) {
        return RecordCodecBuilder.create(instance -> instance.group(
                        capability.serializer.codec().fieldOf("content").forGetter(val -> capability.of(val.content)),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("chance", 0).forGetter(val -> val.chance),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_chance", 0).forGetter(val -> val.maxChance),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("tierChanceBoost", 0)
                                .forGetter(val -> val.tierChanceBoost),
                        Codec.STRING.optionalFieldOf("slotName", "").forGetter(val -> val.slotName != null ? val.slotName : ""),
                        Codec.STRING.optionalFieldOf("uiName", "").forGetter(val -> val.uiName != null ? val.uiName : ""))
                .apply(instance, Content::new));
    }

    public Content copy(RecipeCapability<?> capability, @Nullable ContentModifier modifier) {
        if (modifier == null || chance == 0) {
            return new Content(capability.copyContent(content), chance, maxChance, tierChanceBoost, slotName, uiName);
        } else {
            return new Content(capability.copyContent(content, modifier), chance, maxChance, tierChanceBoost, slotName,
                    uiName);
        }
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
        return Math.round(chanceBoost / error);
    }

    public IGuiTexture createOverlay(boolean perTick) {
        return new IGuiTexture() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(graphics, x, y, width, height);
                drawRangeAmount(graphics, x, y, width, height);
                drawFluidAmount(graphics, x, y, width, height);
                if (perTick) {
                    drawTick(graphics, x, y, width, height);
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public void drawRangeAmount(GuiGraphics graphics, float x, float y, int width, int height) {
        //@formatter:off
        if (content instanceof SizedIngredient sized &&
                sized.ingredient().getCustomIngredient() instanceof IntProviderIngredient ingredient) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.pose().scale(0.5f, 0.5f, 1);
            int min = ingredient.getCountProvider().getMinValue();
            int max = ingredient.getCountProvider().getMaxValue();
            String s = String.format("%s-%s", min, max);
            int color = 0xFFFFFF;
            Font fontRenderer = Minecraft.getInstance().font;
            // 5 == max num of characters that fit in a slot at 0.5x render size
            if (s.length() > 5) {
                s = "X-Y";
                color = 0xEE0000;
            }
            graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 21),
                    (int) ((y + (height / 3f) + 6) * 2), color, true);
            graphics.pose().popPose();
        }
        //@formatter:on
    }

    @OnlyIn(Dist.CLIENT)
    public void drawFluidAmount(GuiGraphics graphics, float x, float y, int width, int height) {
        if (content instanceof SizedFluidIngredient ingredient) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.pose().scale(0.5f, 0.5f, 1);
            long amount = ingredient.ingredient().hasNoFluids() ? 0 : ingredient.amount();
            String s;
            if (amount >= 1000) {
                amount /= 1000;
                s = amount + "B";
            } else {
                s = amount + "mB";
            }
            Font fontRenderer = Minecraft.getInstance().font;
            graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 21),
                    (int) ((y + (height / 3f) + 6) * 2), 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void drawChance(GuiGraphics graphics, float x, float y, int width, int height) {
        if (chance == ChanceLogic.getMaxChancedValue()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        float chance = 100 * (float) this.chance / maxChance;
        String percent = FormattingUtil.formatPercent(chance);

        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_0_short") :
                percent + "%";
        int color = chance == 0 ? 0xFF0000 : 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height), color, true);
        graphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void drawTick(GuiGraphics graphics, float x, float y, int width, int height) {
        graphics.pose().pushPose();
        RenderSystem.disableDepthTest();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        String s = LocalizationUtils.format("gtceu.gui.content.tips.per_tick_short");
        int color = 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height + (chance == ChanceLogic.getMaxChancedValue() ? 0 : 10)),
                color);
        graphics.pose().popPose();
    }

    @Override
    public String toString() {
        return "Content{" +
                "content=" + content +
                ", chance=" + chance +
                ", maxChance=" + maxChance +
                ", tierChanceBoost=" + tierChanceBoost +
                ", slotName='" + slotName + '\'' +
                ", uiName='" + uiName + '\'' +
                '}';
    }
}
