package brachy.modularui.integration.rei.recipe;

import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerScreenWrapper;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.handlers.fluid.EmptyFluidTank;
import brachy.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import brachy.modularui.integration.rei.REIStackConverter;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.utils.memoization.MemoizedSupplier;
import brachy.modularui.utils.memoization.Memoizer;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import lombok.Getter;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Experimental
public class ModularUIREIDisplay implements Display {

    private final Identifier recipeId;
    protected final MemoizedSupplier<ModularScreen> screen;

    @Getter
    protected final List<EntryIngredient> inputEntries;
    @Getter
    protected final List<EntryIngredient> outputEntries;
    protected final List<EntryIngredient> catalysts;
    @Getter
    protected final CategoryIdentifier<?> categoryIdentifier;

    public ModularUIREIDisplay(Identifier recipeId, Supplier<IWidget> widgetSupplier, CategoryIdentifier<?> category) {
        this.recipeId = recipeId;

        this.inputEntries = new ArrayList<>();
        this.outputEntries = new ArrayList<>();
        this.catalysts = new ArrayList<>();
        this.categoryIdentifier = category;

        this.screen = Memoizer.memoize(() -> {
            IWidget widget = widgetSupplier.get();
            ModularPanel<?> panel = ModularPanel.defaultPanel(recipeId.toString(), widget.getArea().w(), widget.getArea().h());
            panel.child(widget);
            return new ModularScreen(recipeId.getNamespace(), panel);
        }, Duration.ofSeconds(10));

        WidgetTree.foreachChildBFS(widgetSupplier.get(), widget -> {
            if (!(widget instanceof IngredientProvider<?> provider)) return true;

            RecipeSlotRole role = provider.getRecipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) return true;

            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) return true;

            @SuppressWarnings({"rawtypes", "unchecked"})
            EntryIngredient ingredient = ((REIStackConverter.Converter) converter).convertTo(provider);

            switch (role) {
                case INPUT -> inputEntries.add(ingredient);
                case OUTPUT -> outputEntries.add(ingredient);
                case CATALYST -> catalysts.add(ingredient);
            }
            return true;
        }, true);
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.ofNullable(recipeId);
    }

    public List<Widget> createWidgets(Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new UIWrapperWidget());

        WidgetTree.foreachChildBFS(this.screen.get().getMainPanel(), widget -> {
            if (!(widget instanceof IngredientProvider<?> provider)) return true;

            RecipeSlotRole role = provider.getRecipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) return true;

            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) return true;

            @SuppressWarnings({"rawtypes", "unchecked"})
            EntryIngredient ingredient = ((REIStackConverter.Converter) converter).convertTo(provider);
            Area area = widget.getArea();

            EntryWidget entryWidget = new EntryWidget(new Rectangle(area.x(), area.y(), area.w(), area.h()));
            // Clear the MUI slots and add EMI slots based on them.
            if (provider instanceof ItemSlot itemSlot) {
                itemSlot.slot(RecipeScreenRenderingUtil.EMPTY_ITEM_HANDLER, 0).invisible();
            } else if (provider instanceof FluidSlot fluidSlot) {
                fluidSlot.syncHandler(EmptyFluidTank.INSTANCE).invisible();
            }

            entryWidget.background(false)
                    .entries(ingredient.castAsList());
            if (role == RecipeSlotRole.INPUT) {
                entryWidget.markIsInput();
            } else if (role == RecipeSlotRole.OUTPUT) {
                entryWidget.markIsOutput();
            } else {
                entryWidget.unmarkInputOrOutput();
            }
            if (widget instanceof ITooltip<?> tooltip && tooltip.hasTooltip()) {
                if (tooltip.tooltip().getRichText() instanceof RichText richText) {
                    var textList = richText.getAsText();
                    entryWidget.tooltipProcessor(text -> {
                        textList.forEach(line -> line.map(t -> text.add((Component) t), text::add));
                        return text;
                    });
                }
            }
            widgets.add(entryWidget);
            return true;
        }, true);
        widgets.add(new UIForegroundRenderWidget());
        return widgets;
    }

    @Override
    public List<EntryIngredient> getRequiredEntries() {
        var required = new ArrayList<>(catalysts);
        required.addAll(inputEntries);
        return required;
    }

    public class UIWrapperWidget extends Widget {

        public UIWrapperWidget() {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            screen.construct(new RecipeViewerScreenWrapper(screen));
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public @Nullable Tooltip getTooltip(TooltipContext context) {
            return null;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            //screen.get().mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return screen.get().mousePressed(button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return screen.get().mouseReleased(button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return screen.get().mouseDragged(button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
            return screen.get().mouseScrolled(deltaX, deltaY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return screen.get().charTyped(codePoint, modifiers);
        }
    }

    public class UIForegroundRenderWidget extends Widget {

        public UIForegroundRenderWidget() {}

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = ModularUIREIDisplay.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }
}
