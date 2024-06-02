package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class Content {

    public RecipeCapability<?> capability;
    @Getter
    public Object content;
    public float chance;
    public float tierChanceBoost;
    @Nullable
    public String slotName;
    @Nullable
    public String uiName;

    public Content(Object content, float chance, float tierChanceBoost, @Nullable String slotName,
                   @Nullable String uiName) {
        this.content = content;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        this.slotName = slotName == null || slotName.isEmpty() ? null : slotName;
        this.uiName = uiName == null || uiName.isEmpty() ? null : uiName;
    }

    public static <T> Codec<Content> codec(RecipeCapability<T> capability) {
        return RecordCodecBuilder.create(instance -> instance.group(
                capability.serializer.codec().fieldOf("content").forGetter(val -> capability.of(val.content)),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 0.0f).forGetter(val -> val.chance),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("tierChanceBoost", 0.0f)
                        .forGetter(val -> val.tierChanceBoost),
                Codec.STRING.optionalFieldOf("slotName", "").forGetter(val -> val.slotName != null ? val.slotName : ""),
                Codec.STRING.optionalFieldOf("uiName", "").forGetter(val -> val.uiName != null ? val.uiName : ""))
                .apply(instance, Content::new));
    }

    public Content copy(RecipeCapability<?> capability, @Nullable ContentModifier modifier) {
        if (modifier == null || chance == 0) {
            return new Content(capability.copyContent(content), chance, tierChanceBoost, slotName, uiName);
        } else {
            return new Content(capability.copyContent(content, modifier), chance, tierChanceBoost, slotName, uiName);
        }
    }

    public IGuiTexture createOverlay(boolean perTick) {
        return new IGuiTexture() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(graphics, x, y, width, height);
                if (LDLib.isEmiLoaded()) {
                    drawEmiAmount(graphics, x, y, width, height);
                }
                if (perTick) {
                    drawTick(graphics, x, y, width, height);
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public void drawEmiAmount(GuiGraphics graphics, float x, float y, int width, int height) {
        if (content instanceof SizedFluidIngredient ingredient) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.pose().scale(0.5f, 0.5f, 1);
            long amount = ingredient.ingredient().hasNoFluids() ? 0 : ingredient.getFluids()[0].getAmount();
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
        if (chance == 1) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_0_short") :
                String.format("%.2f", chance * 100) + "%";
        int color = chance == 0 ? 0xff0000 : 0xFFFF00;
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
                (int) ((y + (height / 3f) + 6) * 2 - height + (chance == 1 ? 0 : 10)), color);
        graphics.pose().popPose();
    }
}
