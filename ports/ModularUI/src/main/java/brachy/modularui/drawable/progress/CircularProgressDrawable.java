package brachy.modularui.drawable.progress;

import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import lombok.Getter;
import org.joml.Matrix4f;

/**
 * A progress texture which translates the progress into a circular angle. This works with any {@link brachy.modularui.api.drawable.IDrawable}.
 */
public class CircularProgressDrawable extends AbstractProgressDrawable<CircularProgressDrawable> {

    @Getter private Direction direction = Direction.CW;

    @Override
    protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        final float p = progress;
        float angle = p * Mth.TWO_PI;
        context.getStencil().push(() -> {
            ShaderInstance lastShader = RenderSystem.getShader();
            RenderSystem.setShader(GameRenderer::getPositionShader);
            Matrix4f pose = context.graphicsPose().last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            float wHalf = width / 2f, hHalf = height / 2f;
            float xc = x + wHalf, yc = y + hHalf;
            buffer.addVertex(pose, xc, yc, 0); // center

            if (this.direction == Direction.CW) {
                if (p > 7 / 8f) {
                    float tan = (float) Math.tan(angle - Mth.TWO_PI);
                    buffer.addVertex(pose, x + wHalf + tan * wHalf, y, 0);
                    buffer.addVertex(pose, x, y, 0); // top left
                }
                if (p > 5 / 8f) {
                    if (p < 7 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.PI - Mth.HALF_PI);
                        buffer.addVertex(pose, x, y + hHalf - tan * hHalf, 0);
                    }
                    buffer.addVertex(pose, x, y + height, 0); // bottom left
                }
                if (p > 3 / 8f) {
                    if (p < 5 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.PI);
                        buffer.addVertex(pose, x + wHalf - tan * wHalf, y + width, 0);
                    }
                    buffer.addVertex(pose, x + width, y + height, 0); // bottom right
                }
                if (p > 1 / 8f) {
                    if (p < 3 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.HALF_PI);
                        buffer.addVertex(pose, x + width, y + hHalf + tan * hHalf, 0);
                    }
                    buffer.addVertex(pose, x + width, y, 0); // top right
                } else {
                    float tan = (float) Math.tan(angle);
                    buffer.addVertex(pose, x + wHalf + tan * wHalf, y, 0);
                }
                buffer.addVertex(pose, xc, y, 0); // top center, starting angle
            } else {
                buffer.addVertex(pose, xc, y, 0); // top center, starting angle
                if (p < 1 / 8f) {
                    float tan = (float) Math.tan(angle);
                    buffer.addVertex(pose, x + wHalf - tan * wHalf, y, 0);
                }
                if (p > 1 / 8f) {
                    buffer.addVertex(pose, x, y, 0); // top left
                    if (p < 3 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.PI - Mth.HALF_PI);
                        buffer.addVertex(pose, x, y + hHalf + tan * hHalf, 0);
                    }
                }
                if (p > 3 / 8f) {
                    buffer.addVertex(pose, x, y + height, 0); // bottom left
                    if (p < 5 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.PI);
                        buffer.addVertex(pose, x + wHalf + tan * wHalf, y + width, 0);
                    }
                }
                if (p > 5 / 8f) {
                    buffer.addVertex(pose, x + width, y + height, 0); // bottom right
                    if (p < 7 / 8f) {
                        float tan = (float) Math.tan(angle - Mth.HALF_PI);
                        buffer.addVertex(pose, x + width, y + hHalf - tan * hHalf, 0);
                    }
                }
                if (p > 7 / 8f) {
                    buffer.addVertex(pose, x + width, y, 0); // top right
                    float tan = (float) Math.tan(angle);
                    buffer.addVertex(pose, x + wHalf - tan * wHalf, y, 0);
                }
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            RenderSystem.setShader(() -> lastShader);

        }, x, y, width, height);
    }

    public CircularProgressDrawable direction(Direction direction) {
        this.direction = direction == null ? Direction.CW : direction;
        return this;
    }

    public CircularProgressDrawable clockwise() {
        return direction(Direction.CW);
    }

    public CircularProgressDrawable counterClockwise() {
        return direction(Direction.CCW);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircularProgressDrawable that)) return false;
        if (!super.equals(o)) return false;

        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public enum Direction {
        CW, CCW
    }
}
