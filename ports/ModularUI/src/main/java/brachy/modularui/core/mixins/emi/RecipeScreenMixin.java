package brachy.modularui.core.mixins.emi;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;

import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipeScreen.class, remap = false)
public class RecipeScreenMixin {

    @Shadow private List<WidgetGroup> currentPage;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), remap = true, cancellable = true)
    private void modularui$mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        for (WidgetGroup group : currentPage) {
            for (Widget widget : group.widgets) {
                if (widget instanceof ModularUIEmiRecipe.UIWrapperWidget wrapperWidget) {
                    if (wrapperWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), remap = true, cancellable = true)
    private void modularui$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (WidgetGroup group : currentPage) {
            for (Widget widget : group.widgets) {
                if (widget instanceof ModularUIEmiRecipe.UIWrapperWidget wrapperWidget) {
                    if (wrapperWidget.mouseReleased(button)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), remap = true, cancellable = true)
    private void modularui$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        for (WidgetGroup group : currentPage) {
            for (Widget widget : group.widgets) {
                if (widget instanceof ModularUIEmiRecipe.UIWrapperWidget wrapperWidget) {
                    if (wrapperWidget.mouseDragged(button, deltaX, deltaY)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

}
