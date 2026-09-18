package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class JeiRecipeViewerSlot extends RecipeViewerSlotWidget<JeiRecipeViewerSlot> {

    public JeiRecipeViewerSlot() {
        throw new NotImplementedException();
    }

    @Override
    public JeiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        return getThis();
    }

    @Override
    public <T> JeiRecipeViewerSlot value(EntryList<T> entryList) {
        return getThis();
    }

    @Override
    public JeiRecipeViewerSlot chance(float chance) {
        return getThis();
    }
}
