package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.mui.drawable.text.FontRenderHelper;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow(remap = false)
    private ItemStack tooltipStack;

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void gtceu$renderResearchItemContent(@Nullable LivingEntity entity, @Nullable Level level,
                                                 ItemStack stack, int x, int y, int seed, int z,
                                                 Operation<Void> original) {
        if (!RenderUtil.renderResearchItemContent((GuiGraphics) (Object) this, original,
                entity, level, stack, x, y, z, seed)) {
            original.call(entity, level, stack, x, y, seed, z);
        }
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void gtceu$replaceWithRichTooltip(Font font, List<Component> tooltipLines,
                                              Optional<TooltipComponent> visualComponent, int mouseX, int mouseY,
                                              CallbackInfo ci) {
        // funny cast to get from List<Component> to List<FormattedText>
        // I just don't want to copy the list unnecessarily
        // this is safe because Component extends FormattedText
        // noinspection unchecked
        gtceu$drawRichTooltip(font, (List<FormattedText>) (List<?>) tooltipLines, visualComponent, mouseX, mouseY, ci);
    }

    @Inject(
            method = {
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;II)V",
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"
            },
            at = @At(value = "HEAD"),
            cancellable = true)
    private void gtceu$replaceWithRichTooltip(CallbackInfo ci,
                                              @Local(argsOnly = true) Font font,
                                              @Local(argsOnly = true) List<FormattedCharSequence> tooltipLines,
                                              @Local(ordinal = 0, argsOnly = true) int mouseX,
                                              @Local(ordinal = 1, argsOnly = true) int mouseY) {
        List<FormattedText> text = tooltipLines.stream()
                .map(FontRenderHelper::getComponentFromCharSequence)
                .collect(Collectors.toList());
        gtceu$drawRichTooltip(font, text, Optional.empty(), mouseX, mouseY, ci);
    }

    @Inject(method = "renderComponentTooltip*",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraftforge/client/ForgeHooksClient;gatherTooltipComponents(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;IIILnet/minecraft/client/gui/Font;)Ljava/util/List;",
                     remap = false),
            cancellable = true)
    private void gtceu$replaceWithRichTooltip2(CallbackInfo ci,
                                               @Local(argsOnly = true) Font font,
                                               @Local(argsOnly = true) List<FormattedText> tooltipLines,
                                               @Local(ordinal = 0, argsOnly = true) int mouseX,
                                               @Local(ordinal = 1, argsOnly = true) int mouseY) {
        gtceu$drawRichTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY, ci);
    }

    @Unique
    private void gtceu$drawRichTooltip(Font font, List<FormattedText> textLines,
                                       Optional<TooltipComponent> tooltipComponent, int mouseX, int mouseY,
                                       CallbackInfo ci) {
        if (!ConfigHolder.INSTANCE.client.ui.replaceVanillaTooltips || textLines.isEmpty()) {
            return;
        }

        RichTooltip tooltip = new RichTooltip();
        tooltip.parent(area -> RichTooltip.findIngredientArea(area, mouseX, mouseY));
        // Other positions don't really work due to the lack of GuiContext in non-modular uis
        tooltip.add(textLines.get(0)).newLine();
        // vanilla inserts the bundle tooltip here so we need to do it as the 2nd item too
        tooltipComponent.ifPresent(tooltip::addLine);

        if (!this.tooltipStack.isEmpty()) {
            tooltip.spaceLine();
        }
        for (int i = 1, n = textLines.size(); i < n; i++) {
            tooltip.add(textLines.get(i)).newLine();
        }

        GuiContext context = GuiContext.getDefault();
        GuiGraphics lastGraphics = context.getGraphics();

        context.setOverrideFont(font);
        context.setGraphics((GuiGraphics) (Object) this);
        tooltip.draw(context, this.tooltipStack);

        context.setGraphics(lastGraphics);
        // the override font is removed at the end of RichTooltip#draw; no need to duplicate that here

        // Cancel vanilla tooltip rendering
        ci.cancel();
    }
}
