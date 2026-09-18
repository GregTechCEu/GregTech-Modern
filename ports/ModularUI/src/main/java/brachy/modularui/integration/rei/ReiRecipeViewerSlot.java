package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ReiRecipeViewerSlot extends RecipeViewerSlotWidget<ReiRecipeViewerSlot> {

    public ReiRecipeViewerSlot() {
        throw new NotImplementedException();
    }

    @Override
    public ReiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        return getThis();
    }

    @Override
    public <T> ReiRecipeViewerSlot value(EntryList<T> entryList) {
        return getThis();
    }

    @Override
    public ReiRecipeViewerSlot chance(float chance) {
        return getThis();
    }
}
