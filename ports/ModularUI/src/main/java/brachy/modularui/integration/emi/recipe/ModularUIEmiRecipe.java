package brachy.modularui.integration.emi.recipe;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class ModularUIEmiRecipe implements EmiRecipe {

    private static final LoadingCache<ModularUIEmiRecipe, ModularScreen> SCREEN_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(1))
            .maximumSize(20)
            .build(new CacheLoader<>() {
                @Override
                public ModularScreen load(ModularUIEmiRecipe key) {
                    return key.createScreen();
                }
            });

    @Getter private final Identifier id;
    private final Supplier<IWidget> recipeUI;

    private boolean sizeCalculated = false;
    private Bounds bounds;
    private int displayWidth, displayHeight;

    public ModularUIEmiRecipe(Identifier recipeId, Supplier<IWidget> recipeUI) {
        this.id = recipeId;
        this.recipeUI = recipeUI;
    }

    public ModularUIEmiRecipe(Identifier recipeId, int width, int height, Supplier<IWidget> recipeUI) {
        this.id = recipeId;
        this.recipeUI = recipeUI;
        this.displayWidth = width;
        this.displayHeight = height;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);
        this.sizeCalculated = true;
    }

    /**
     * Calculates the size of the recipe, if not already done. This should be called in the constructor of sub-classes.
     * Otherwise, the size of ALL the recipes in the same category are calculated all at once, which can make the game for a few seconds.
     */
    protected void calculateSize() {
        if (this.sizeCalculated) return;
        this.sizeCalculated = true;
        IWidget ui = this.recipeUI.get();
        int w = ui.resizer().getFixedPixelWidth(), h = ui.resizer().getFixedPixelHeight();
        if (w < 0 || h < 0) {
            ModularScreen screen = createScreen(ui, this.id.getNamespace(), "emi_recipe_" + this.id.getPath());
            w = EmbedHandler.getEmbedWidth(screen);
            h = EmbedHandler.getEmbedHeight(screen);
        }
        this.displayWidth = w;
        this.displayHeight = h;
        this.bounds = new Bounds(0, 0, this.displayWidth, this.displayHeight);
    }

    public Bounds getBounds() {
        calculateSize();
        return bounds;
    }

    @Override
    public int getDisplayHeight() {
        calculateSize();
        return displayHeight;
    }

    @Override
    public int getDisplayWidth() {
        calculateSize();
        return displayWidth;
    }

    private ModularScreen createScreen() {
        return createScreen(this.recipeUI.get(), this.id.getNamespace(), "emi_recipe_" + this.id.getPath());
    }

    public ModularScreen createScreen(IWidget recipeUI, String owner, String name) {
        ModularPanel<?> panel;
        if (recipeUI instanceof ModularPanel<?> panel1) {
            panel = panel1;
        } else {
            panel = new ModularPanel<>(name);
            panel.coverChildren(60, 40)
                    .invisible()
                    .child(recipeUI);
        }
        if (getInputs() != null && getOutputs() != null) {
            panel = transform(panel);
        }
        ModularScreen screen = ModularScreen.createEmbed(owner, panel);
        screen.getContext().getUISettings().drawTooltipExternally(true);
        return screen;
    }

    public ModularPanel<?> transform(ModularPanel<?> panel) {
        Iterator<EmiIngredient> in = getInputs().iterator();
        Iterator<EmiStack> out = getOutputs().iterator();
        panel.visitTransformAllChildren(widget -> transformWidget(widget, in, out));
        return panel;
    }

    public IWidget transformWidget(IWidget widget, Iterator<EmiIngredient> in, Iterator<EmiStack> out) {
        if (!(widget instanceof EmiRecipeViewerSlot recipeViewerSlot)) return widget;

        if (recipeViewerSlot.recipeSlotRole() == RecipeSlotRole.OUTPUT) {
            recipeViewerSlot.getSlotWidget().recipeContext(this);
        }

        return recipeViewerSlot;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // emi complains when it cant find an output slot
        widgets.add(new SlotWidget(EmiStack.EMPTY, -1000, -1000).drawBack(false).recipeContext(this));
        widgets.add(new UIWrapperWidget(this));
    }

    public static class UIWrapperWidget extends Widget {

        private final ModularUIEmiRecipe recipe;

        public UIWrapperWidget(ModularUIEmiRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public Bounds getBounds() {
            return this.recipe.getBounds();
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            ModularScreen screen = SCREEN_CACHE.getUnchecked(this.recipe);
            EmbedHandler.drawEmbed(screen, graphics, partialTick, r -> !(r instanceof SizedButtonWidget));
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            ModularScreen screen = SCREEN_CACHE.getUnchecked(this.recipe);
            if (!screen.getContext().getUISettings().drawTooltipExternally()) {
                return super.getTooltip(mouseX, mouseY);
            }
            IWidget hovered = screen.getContext().getTopHovered();
            if (hovered instanceof ITooltip<?> tooltip && tooltip.getTooltip() != null) {
                RichTooltip richTooltip = tooltip.getTooltip();
                if (richTooltip.autoUpdate()) richTooltip.markDirty();
                richTooltip.isEmpty(); // causes the tooltip to rebuild if necessary
                IRichTextBuilder<?> richTextBuilder = richTooltip.getRichText();
                if (richTextBuilder instanceof RichText richText) {
                    // scuffed conversion, but it mostly works
                    return richText.getAsText().toClientTooltipComponents();
                }
                return List.of();
            }
            return List.of();
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            return SCREEN_CACHE.getUnchecked(this.recipe).mousePressed(button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return SCREEN_CACHE.getUnchecked(this.recipe).keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return SCREEN_CACHE.getUnchecked(this.recipe).mouseScrolled(scrollX, scrollY);
        }

        public boolean mouseDragged(int button, double dragX, double dragY) {
            return SCREEN_CACHE.getUnchecked(this.recipe).mouseDragged(button, dragX, dragY);
        }

        public boolean mouseReleased(int button) {
            return SCREEN_CACHE.getUnchecked(this.recipe).mouseReleased(button);
        }
    }
}
