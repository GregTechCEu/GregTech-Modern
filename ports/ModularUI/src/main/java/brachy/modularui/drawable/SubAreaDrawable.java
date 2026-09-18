package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.util.Mth;
import com.mojang.serialization.Codec;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class SubAreaDrawable extends DelegateDrawable {

    public static final MutableObjectCodec<SubAreaDrawable> CODEC = MutableObjectCodec.drawableBuilder(SubAreaDrawable::new)
            .add("drawable", SubAreaDrawable::drawable, SubAreaDrawable::getWrappedDrawable, IDrawable.CODEC)
            .addOpt("u0", SubAreaDrawable::u0, SubAreaDrawable::getU0, Codec.FLOAT, 0f).alias("uStart")
            .addOpt("v0", SubAreaDrawable::v0, SubAreaDrawable::getV0, Codec.FLOAT, 0f).alias("vStart")
            .addOpt("u1", SubAreaDrawable::u1, SubAreaDrawable::getU1, Codec.FLOAT, 1f).alias("uEnd")
            .addOpt("v1", SubAreaDrawable::v1, SubAreaDrawable::getV1, Codec.FLOAT, 1f).alias("vEnd")
            .build();

    @Getter
    private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;

    private SubAreaDrawable() {
        super(IDrawable.EMPTY);
    }

    public SubAreaDrawable(@Nullable IDrawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        context.getStencil().push(x + this.u0 * width, y + this.v0 * height, (this.u1 - this.u0) * width, (this.v1 - this.v0) * height);
        super.draw(context, x, y, width, height, widgetTheme);
        context.getStencil().pop();
    }

    @Override
    public IDrawable getSubArea(float u0, float v0, float u1, float v1) {
        return new SubAreaDrawable(getWrappedDrawable()).uv(lerpU(u0), lerpV(v0), lerpU(u1), lerpV(v1));
    }

    protected final float lerpU(float u) {
        return Interpolations.lerp(this.u0, this.u1, u);
    }

    protected final float lerpV(float v) {
        return Interpolations.lerp(this.v0, this.v1, v);
    }

    public SubAreaDrawable u0(float u) {
        this.u0 = Mth.clamp(u, 0f, 1f);
        return this;
    }

    public SubAreaDrawable u1(float u) {
        this.u1 = Mth.clamp(u, 0f, 1f);
        return this;
    }

    public SubAreaDrawable v0(float v) {
        this.v0 = Mth.clamp(v, 0f, 1f);
        return this;
    }

    public SubAreaDrawable v1(float v) {
        this.v1 = Mth.clamp(v, 0f, 1f);
        return this;
    }

    public SubAreaDrawable uv(float u0, float v0, float u1, float v1) {
        return u0(u0).v0(v0).u1(u1).v1(v1);
    }

    public SubAreaDrawable u(float u0, float u1) {
        return u0(u0).u1(u1);
    }

    public SubAreaDrawable v(float v0, float v1) {
        return v0(v0).v1(v1);
    }

    public SubAreaDrawable drawable(@Nullable IDrawable drawable) {
        setDrawable(drawable);
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SubAreaDrawable that)) return false;
        if (!super.equals(o)) return false;

        return Float.compare(u0, that.u0) == 0 && Float.compare(v0, that.v0) == 0 &&
                Float.compare(u1, that.u1) == 0 && Float.compare(v1, that.v1) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Float.hashCode(u0);
        result = 31 * result + Float.hashCode(v0);
        result = 31 * result + Float.hashCode(u1);
        result = 31 * result + Float.hashCode(v1);
        return result;
    }
}
