package brachy.modularui.core.mixins.client;

import brachy.modularui.ModularUIConfig;
import brachy.modularui.drawable.text.FontRenderHelper;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin {
/*
    @Shadow(remap = false)
    private ItemStack tooltipStack;

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void mui$replaceWithRichTooltip(Font font, List<Component> tooltipLines,
                                            Optional<TooltipComponent> visualComponent, int mouseX, int mouseY,
                                            CallbackInfo ci) {
        // funny cast to get from List<Component> to List<FormattedText>
        // I just don't want to copy the list unnecessarily
        // this is safe because Component extends FormattedText
        // noinspection unchecked
        mui$drawRichTooltip(font, (List<FormattedText>) (List<?>) tooltipLines, visualComponent, mouseX, mouseY, ci);
    }

    @Inject(
            method = {
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;II)V",
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"
            },
            at = @At(value = "HEAD"),
            cancellable = true)
    private void mui$replaceWithRichTooltip(CallbackInfo ci,
                                            @Local(argsOnly = true) Font font,
                                            @Local(argsOnly = true) List<FormattedCharSequence> tooltipLines,
                                            @Local(ordinal = 0, argsOnly = true) int mouseX,
                                            @Local(ordinal = 1, argsOnly = true) int mouseY) {
        List<FormattedText> text = tooltipLines.stream()
                .map(FontRenderHelper::getComponentFromCharSequence)
                .collect(Collectors.toList());
        mui$drawRichTooltip(font, text, Optional.empty(), mouseX, mouseY, ci);
    }

    @Inject(method = "renderComponentTooltip*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;gatherTooltipComponents(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;IIILnet/minecraft/client/gui/Font;)Ljava/util/List;",
                    remap = false),
            cancellable = true)
    private void mui$replaceWithRichTooltip2(CallbackInfo ci,
                                             @Local(argsOnly = true) Font font,
                                             @Local(argsOnly = true) List<FormattedText> tooltipLines,
                                             @Local(ordinal = 0, argsOnly = true) int mouseX,
                                             @Local(ordinal = 1, argsOnly = true) int mouseY) {
        mui$drawRichTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY, ci);
    }

    @Unique
    private void mui$drawRichTooltip(Font font, List<FormattedText> textLines,
                                     Optional<TooltipComponent> tooltipComponent, int mouseX, int mouseY,
                                     CallbackInfo ci) {
        if (!ModularUIConfig.replaceVanillaTooltips() || textLines.isEmpty()) {
            return;
        }

        RichTooltip tooltip = new RichTooltip();
        tooltip.parent(area -> RichTooltip.findIngredientArea(area, mouseX, mouseY));
        // Other positions don't really work due to the lack of GuiContext in non-modular uis
        tooltip.add(textLines.getFirst()).newLine();
        // vanilla inserts the bundle tooltip here so we should do it as the 2nd item too
        tooltipComponent.ifPresent(tooltip::addLine);

        if (!this.tooltipStack.isEmpty()) {
            tooltip.spaceLine();
        }
        for (int i = 1, n = textLines.size(); i < n; i++) {
            tooltip.add(textLines.get(i)).newLine();
        }

        GuiContext context = GuiContext.getDefault();
        GuiGraphicsExtractor lastGraphics = context.getGraphics();

        context.setOverrideFont(font);
        context.setGraphics((GuiGraphicsExtractor) (Object) this);
        tooltip.draw(context, this.tooltipStack);

        context.setGraphics(lastGraphics);
        // the override font is removed at the end of RichTooltip#draw; no need to duplicate that here

        // Cancel vanilla tooltip rendering
        ci.cancel();
    }
*/
}
