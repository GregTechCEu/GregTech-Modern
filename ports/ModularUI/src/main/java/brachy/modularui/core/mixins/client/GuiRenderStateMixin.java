package brachy.modularui.core.mixins.client;

import brachy.modularui.drawable.GuiStencil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Retains clipping when text, items and previews are expanded after screen extraction. */
@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin {
    @Unique private final Map<Object, Integer> modularui$clips = new IdentityHashMap<>();

    @WrapMethod(method = "addGuiElement")
    private void modularui$element(GuiElementRenderState state, Operation<Void> original) {
        if (!GuiStencil.capture(state)) original.call(GuiStencil.clip(state));
    }

    @Inject(method = "addText", at = @At("HEAD"))
    private void modularui$text(GuiTextRenderState state, CallbackInfo ci) {
        modularui$clips.put(state, GuiStencil.currentBit());
    }

    @Inject(method = "addItem", at = @At("HEAD"))
    private void modularui$item(GuiItemRenderState state, CallbackInfo ci) {
        modularui$clips.put(state, GuiStencil.currentBit());
    }

    @Inject(method = "addPicturesInPictureState", at = @At("HEAD"))
    private void modularui$preview(PictureInPictureRenderState state, CallbackInfo ci) {
        modularui$clips.put(state, GuiStencil.currentBit());
    }

    @ModifyVariable(method = {"forEachText", "forEachItem", "forEachPictureInPicture"}, at = @At("HEAD"), argsOnly = true)
    private Consumer<?> modularui$restoreClip(Consumer<?> consumer) {
        return GuiStencil.scoped(consumer, modularui$clips);
    }

    @ModifyVariable(method = "addGlyphToCurrentLayer", at = @At("HEAD"), argsOnly = true)
    private GuiElementRenderState modularui$glyph(GuiElementRenderState state) {
        return GuiStencil.clip(state);
    }

    @ModifyArg(method = "addBlitToCurrentLayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState$Node;addGuiElement(Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;)V"), index = 0)
    private GuiElementRenderState modularui$blit(GuiElementRenderState state) {
        return GuiStencil.clip(state);
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void modularui$clear(CallbackInfo ci) {
        modularui$clips.clear();
    }
}
