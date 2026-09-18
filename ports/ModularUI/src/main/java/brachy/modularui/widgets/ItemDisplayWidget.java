package brachy.modularui.widgets;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.value.IValue;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.ObjectValue;
import brachy.modularui.widget.Widget;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ItemDisplayWidget extends Widget<ItemDisplayWidget> implements IngredientProvider<ItemStack> {

    @Getter private IValue<ItemStack> value;
    @Getter private boolean displayAmount = false;
    @Getter private RecipeSlotRole recipeRole = RecipeSlotRole.RENDER_ONLY;

    public ItemDisplayWidget() {
        size(18);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isValueOfType(ItemStack.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castValueNullable(ItemStack.class);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getItemSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        ItemStack item = value.getValue();
        if (!item.isEmpty()) {
            GuiDraw.drawItem(context.getGraphics(), item, 1, 1, 16, 16, context.getCurrentDrawingZ());
            if (this.displayAmount) {
                GuiDraw.drawStandardSlotAmountText(context, item.getCount(), null, getArea(), 0);
            }
        }
    }

    public ItemDisplayWidget item(IValue<ItemStack> itemSupplier) {
        setSyncOrValue(ISyncOrValue.orEmpty(itemSupplier));
        return this;
    }

    public ItemDisplayWidget item(ItemStack itemStack) {
        return item(new ObjectValue<>(ItemStack.class, itemStack));
    }

    public ItemDisplayWidget displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
        return this;
    }

    @Override
    public EntryList<ItemStack> getIngredients() {
        return ItemStackList.of(value.getValue());
    }

    public ItemDisplayWidget recipeSlotRole(RecipeSlotRole recipeRole) {
        this.recipeRole = recipeRole;
        return this;
    }

    @Override
    public @NotNull Class<ItemStack> ingredientClass() {
        return ItemStack.class;
    }
}
