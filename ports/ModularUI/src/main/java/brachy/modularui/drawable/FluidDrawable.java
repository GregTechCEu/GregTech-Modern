package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.widget.Widget;

import net.minecraft.util.Util;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@ToString
@Accessors(fluent = true, chain = true)
public class FluidDrawable implements IDrawable {

    public static final MutableObjectCodec<FluidDrawable> CODEC = MutableObjectCodec.drawableBuilder(FluidDrawable::new)
            .add("fluids", FluidDrawable::fluids, FluidDrawable::getFluidList, CodecUtil.listLike(FluidStack.CODEC)).alias("fluid")
            .addOpt("cycleTime", FluidDrawable::cycleTime, FluidDrawable::cycleTime, Codec.INT, 1000)
            .build();

    private FluidStack[] fluids = new FluidStack[0];
    @Getter
    @Setter
    private int cycleTime;

    public FluidDrawable() {
        this(new FluidStack[0]);
    }

    public FluidDrawable(FluidStack... fluid) {
        fluids(fluid);
    }

    public FluidDrawable(FluidStack fluid) {
        fluid(fluid);
    }

    public FluidDrawable(Fluid fluid) {
        fluid(fluid);
    }

    public FluidDrawable(Fluid fluid, int amount) {
        fluid(fluid, amount);
    }

    public FluidDrawable(Fluid fluid, int amount, DataComponentPatch componentPatch) {
        fluid(fluid, amount, componentPatch);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.fluids.length == 0) return;
        FluidStack fluid = this.fluids.length == 1 ? this.fluids[0] :
                this.fluids[(int) (Util.getMillis() % (this.cycleTime * this.fluids.length)) / this.cycleTime];
        GuiDraw.drawFluidTexture(context.getGraphics(), fluid, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    public List<FluidStack> getFluidList() {
        return Arrays.asList(this.fluids);
    }

    public FluidStack[] getFluids() {
        return this.fluids;
    }

    public FluidDrawable fluids(Collection<FluidStack> fluids) {
        return fluids(fluids.toArray(FluidStack[]::new));
    }

    public FluidDrawable fluids(FluidStack... fluids) {
        this.fluids = fluids;
        return this;
    }

    public FluidDrawable fluid(FluidStack fluid) {
        if (this.fluids.length != 1) {
            this.fluids = new FluidStack[1];
        }
        this.fluids[0] = fluid;
        return this;
    }

    public FluidDrawable fluid(Fluid fluid) {
        return fluid(fluid, 1, DataComponentPatch.EMPTY);
    }

    public FluidDrawable fluid(Fluid fluid, int amount) {
        return fluid(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidDrawable fluid(Fluid fluid, int amount, DataComponentPatch componentPatch) {
        FluidStack fluidStack = new FluidStack(fluid, amount);
        fluidStack.applyComponents(componentPatch);
        return fluid(fluidStack);
    }

    public FluidDrawable addFluid(FluidStack fluid) {
        this.fluids = ArrayUtils.add(this.fluids, fluid);
        return this;
    }

    public FluidDrawable addFluid(Fluid fluid) {
        return addFluid(fluid, 1, DataComponentPatch.EMPTY);
    }

    public FluidDrawable addFluid(Fluid fluid, int amount) {
        return addFluid(fluid, amount, DataComponentPatch.EMPTY);
    }

    public FluidDrawable addFluid(Fluid fluid, int amount, DataComponentPatch componentPatch) {
        FluidStack fluidStack = new FluidStack(fluid, amount);
        fluidStack.applyComponents(componentPatch);
        return addFluid(fluidStack);
    }

    @Override
    public String getTypeName() {
        return "fluid";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof FluidDrawable that)) return false;
        if (this.cycleTime != that.cycleTime || this.fluids.length != that.fluids.length) return false;
        for (int i = 0; i < this.fluids.length; i++) {
            var f1 = this.fluids[i];
            var f2 = that.fluids[i];
            if ((f1 == null || f2 == null) && f1 != f2) return false;
            if (!FluidStack.isSameFluidSameComponents(f1, f2)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.fluids);
        result = 31 * result + this.cycleTime;
        return result;
    }
}
