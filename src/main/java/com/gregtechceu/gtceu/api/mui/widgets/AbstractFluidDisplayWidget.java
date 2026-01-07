package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.integration.xei.entry.EntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.math.SIPrefix;

import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFluidDisplayWidget<W extends AbstractFluidDisplayWidget<W>> extends Widget<W>
                                                implements IngredientProvider<FluidStack> {

    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";

    private final Box contentPadding = new Box().all(1);
    private String unit = UNIT_BUCKET;
    private SIPrefix baseUnitPrefix = SIPrefix.Milli;
    @Getter
    private boolean flipLighterThanAir = true;

    protected AbstractFluidDisplayWidget() {
        size(18);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        FluidStack fluid = getFluidStack();
        if (fluid == null) return;
        int x = this.contentPadding.left();
        int y = this.contentPadding.top();
        int w = getArea().width - this.contentPadding.horizontal();
        int h = getArea().height - this.contentPadding.vertical();
        float c = getCapacity();
        if (c > 0 && fluid.getAmount() > 0) {
            int newH = (int) GTMath.rescaleLinear(fluid.getAmount(), 0, c, 1, h);
            if (!this.flipLighterThanAir || !fluid.getFluid().getFluidType().isLighterThanAir()) y += h - newH;
            h = newH;
        }
        GuiDraw.drawFluidTexture(context.getGraphics(), fluid, x, y, w, h, context.getCurrentDrawingZ());
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
        FluidStack fluid = getFluidStack();
        if (fluid != null && displayAmountText()) {
            String s = FormattingUtil.formatNumberReadable2F(getBaseUnitAmount(fluid.getAmount()), false) +
                    getBaseUnit();
            // mc doesn't consider the 1px border in item slots for amount text, but it looks weird when it touches the
            // left border, so
            // we only apply padding there
            GuiDraw.drawScaledAlignedTextInBox(context, s, this.contentPadding.left(), 0,
                    getArea().width - this.contentPadding.left(), getArea().height, Alignment.BottomRight);
        }
    }

    protected abstract boolean displayAmountText();

    @Nullable
    protected abstract FluidStack getFluidStack();

    /**
     * Return a positive value if the fluid should be drawn partly depending on the amount filled.
     *
     * @return capacity in milli buckets or zero/negative if fluid should always be drawn full
     */
    protected int getCapacity() {
        return 0;
    }

    public double getBaseUnitAmount(double amount) {
        return amount * getBaseUnitSiPrefix().factor;
    }

    public final String getUnit() {
        return getBaseUnitSiPrefix().stringSymbol + getBaseUnit();
    }

    public String getBaseUnit() {
        return this.unit;
    }

    public SIPrefix getBaseUnitSiPrefix() {
        return this.baseUnitPrefix;
    }

    public W contentPadding(int left, int right, int top, int bottom) {
        this.contentPadding.all(left, right, top, bottom);
        return getThis();
    }

    public W contentPadding(int horizontal, int vertical) {
        this.contentPadding.all(horizontal, vertical);
        return getThis();
    }

    public W contentPadding(int all) {
        this.contentPadding.all(all);
        return getThis();
    }

    public W contentPaddingLeft(int val) {
        this.contentPadding.left(val);
        return getThis();
    }

    public W contentPaddingRight(int val) {
        this.contentPadding.right(val);
        return getThis();
    }

    public W contentPaddingTop(int val) {
        this.contentPadding.top(val);
        return getThis();
    }

    public W contentPaddingBottom(int val) {
        this.contentPadding.bottom(val);
        return getThis();
    }

    public W fluidUnit(String baseUnitSymbol, SIPrefix baseUnitPrefix) {
        this.unit = baseUnitSymbol;
        this.baseUnitPrefix = baseUnitPrefix;
        return getThis();
    }

    /**
     * Determines if a partially filled fluid should be drawn from the top instead of the bottom if the current fluid is
     * lighter than air.
     * When the full fluid is drawn (when {@link #getCapacity()} returns 0) this does nothing.
     */
    public W flipLighterThanAir(boolean flipLighterThanAir) {
        this.flipLighterThanAir = flipLighterThanAir;
        return getThis();
    }

    @Override
    public @NotNull Class<FluidStack> ingredientClass() {
        return FluidStack.class;
    }

    public EntryList<FluidStack> getIngredients() {
        return FluidStackList.of(getFluidStack());
    }
}
