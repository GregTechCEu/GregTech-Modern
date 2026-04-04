package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerScreenWrapper;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.handlers.fluid.EmptyFluidTank;
import brachy.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class GTEmiRecipe2<T extends Recipe<?>, W extends IWidget> implements EmiRecipe {

    @Getter
    protected final T recipe;
    protected final EmiRecipeCategory category;
    protected final MemoizedSupplier<ModularScreen> screen;

    @Getter
    public final List<EmiIngredient> inputs;
    @Getter
    public final List<EmiStack> outputs;
    @Getter
    public final List<EmiIngredient> catalysts;

    @Getter
    private final Bounds bounds;
    @Getter
    private final int displayWidth, displayHeight;

    public boolean allowRecipeTree = true;

    public GTEmiRecipe2(T recipe, EmiRecipeCategory category, Supplier<W> widgetSupplier) {
        this.recipe = recipe;
        this.category = category;

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = new ArrayList<>();

        W recipeWidget = widgetSupplier.get();
        this.displayWidth = recipeWidget.getArea().width;
        this.displayHeight = recipeWidget.getArea().height;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);

        this.screen = GTMemoizer.memoize(() -> {
            W widget = widgetSupplier.get();
            ModularPanel<?> panel = ModularPanel.defaultPanel(recipe.getId().toString(), widget.getArea().w(),
                    widget.getArea().h());
            panel.child(widget);
            return new ModularScreen(recipe.getId().getNamespace(), panel);
        }, Duration.ofSeconds(10));

        WidgetTree.foreachChildBFS(recipeWidget, widget -> {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                return true;
            }
            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                return true;
            }

            EmiStackConverter.Converter<?> converter = EmiStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                return true;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EmiIngredient ingredient = ((EmiStackConverter.Converter) converter).convertTo(provider);

            switch (role) {
                case INPUT -> inputs.add(ingredient);
                case OUTPUT -> {
                    if (ingredient.getEmiStacks().size() > 1) {
                        allowRecipeTree = false;
                    }
                    outputs.addAll(ingredient.getEmiStacks());
                }
                case CATALYST -> catalysts.add(ingredient);
            }
            return true;
        }, true);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new UIWrapperWidget());

        WidgetTree.foreachChildBFS(this.screen.get().getMainPanel(), widget -> {
            if (!(widget instanceof IngredientProvider<?> provider)) return true;

            RecipeSlotRole role = provider.recipeRole();
            if (role == RecipeSlotRole.RENDER_ONLY) {
                return true;
            }
            EmiStackConverter.Converter<?> converter = EmiStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                return true;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            EmiIngredient ingredient = ((EmiStackConverter.Converter) converter).convertTo(provider);
            Area widgetArea = widget.getArea();

            SlotWidget slotWidget = null;
            // Clear the MUI slots and add EMI slots based on them.
            if (provider instanceof ItemSlot itemSlot) {
                itemSlot.slot(RecipeScreenRenderingUtil.EMPTY_ITEM_HANDLER, 0)
                        .invisible();
            } else if (provider instanceof FluidSlot fluidSlot) {
                fluidSlot.syncHandler(EmptyFluidTank.INSTANCE)
                        .invisible();

                long capacity = Math.max(1, ingredient.getAmount());
                slotWidget = new TankWidget(ingredient, widgetArea.x, widgetArea.y, widgetArea.width, widgetArea.height,
                        capacity);
            }
            if (slotWidget == null) {
                slotWidget = new SlotWidget(ingredient, widgetArea.x, widgetArea.y);
            }

            slotWidget.customBackground(null, widgetArea.x, widgetArea.y, widgetArea.width, widgetArea.height)
                    .drawBack(false);

            if (role == RecipeSlotRole.CATALYST) {
                slotWidget.catalyst(true);
            } else if (role == RecipeSlotRole.OUTPUT) {
                slotWidget.recipeContext(this);
            }
            if (widget instanceof ITooltip<?> tooltip && tooltip.hasTooltip()) {
                if (tooltip.tooltip().getRichText() instanceof RichText richText) {
                    var textList = richText.getAsText();
                    for (FormattedText line : textList) {
                        slotWidget
                                .appendTooltip(() -> ClientTooltipComponent
                                        .create(Language.getInstance().getVisualOrder(line)));
                    }
                }
            }
            widgets.add(slotWidget);
            return true;
        }, true);
        widgets.add(new UIForegroundRenderWidget());
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.recipe.getId();
    }

    @Override
    public boolean supportsRecipeTree() {
        return this.allowRecipeTree && EmiRecipe.super.supportsRecipeTree();
    }

    public class UIWrapperWidget extends Widget {

        public UIWrapperWidget() {
            ModularScreen screen = GTEmiRecipe2.this.screen.get();
            screen.construct(new RecipeViewerScreenWrapper(screen));
        }

        @Override
        public Bounds getBounds() {
            return GTEmiRecipe2.this.bounds;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = GTEmiRecipe2.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            return screen.get().onMousePressed(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return screen.get().keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public class UIForegroundRenderWidget extends Widget {

        public UIForegroundRenderWidget() {}

        @Override
        public Bounds getBounds() {
            return GTEmiRecipe2.this.bounds;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = GTEmiRecipe2.this.screen.get();
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, mouseX, mouseY, partialTick);
        }
    }
}
