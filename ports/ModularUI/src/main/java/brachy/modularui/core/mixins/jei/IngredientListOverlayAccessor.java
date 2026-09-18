package brachy.modularui.core.mixins.jei;

import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = IngredientListOverlay.class, remap = false)
public interface IngredientListOverlayAccessor {

    @Accessor("searchField")
    GuiTextFieldFilter getSearchField();
}
