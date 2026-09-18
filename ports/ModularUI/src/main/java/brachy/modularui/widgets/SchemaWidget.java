package brachy.modularui.widgets;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.ISchema;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.math.MathUtils;
import brachy.modularui.widget.Widget;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.InputConstants;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    @Getter private final BaseSchemaRenderer schemaRenderer;
    @Getter private boolean enableRotation = true;
    @Getter private boolean enableTranslation = true;
    @Getter private boolean enableScaling = true;
    @Getter private float scale = 10f;
    @Getter private float pitch = MathUtils.PI_QUART;
    @Getter private float yaw = 0;
    @Getter private final Vector3f offset = new Vector3f();

    public SchemaWidget(ISchema schema) {
        this(new BaseSchemaRenderer(schema));
    }

    public SchemaWidget(BaseSchemaRenderer schemaRenderer) {
        this.schemaRenderer = schemaRenderer;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.schemaRenderer.dispose();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        Vector3fc f = this.schemaRenderer.schema().getFocus();
        this.schemaRenderer.camera().setLookAtAndAngle(f.x() + this.offset.x, f.y() + this.offset.y,
                f.z() + this.offset.z, scale, yaw, pitch);
        this.schemaRenderer.drawAtZeroPadded(context, getArea(), widgetTheme.theme());
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        if (this.enableScaling) {
            incrementScale((float) (-scrollY / 12.0f));
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int button, double dragX, double dragY) {
        float dx = (float) dragX;
        float dy = (float) dragY;
        if (button == InputConstants.MOUSE_BUTTON_LEFT && this.enableRotation) {
            float moveScale = 0.03f;
            yaw(this.yaw + dx * moveScale);
            pitch(this.pitch + dy * moveScale);
        } else if (button == InputConstants.MOUSE_BUTTON_MIDDLE && this.enableTranslation) {
            float moveScale = 0.09f;
            Vector3f look = this.schemaRenderer.camera().getLookVec().normalize(); // direction camera is looking
            Vector3f right = look.cross(MathUtils.UNIT_Y, new Vector3f()).normalize(); // right relative to screen
            Vector3f up = right.cross(look, new Vector3f()); // up relative to screen
            this.offset.sub(right.mul(dx * moveScale)).add(up.mul(dy * moveScale));
        }
    }

    public void incrementScale(float amount) {
        this.scale += amount;
        this.scale = Math.max(this.scale, 0.001f);
    }

    public SchemaWidget scale(float scale) {
        this.scale = scale;
        return this;
    }

    public SchemaWidget pitch(float pitch) {
        // clamp pitch to [-180,180] degrees up/down
        this.pitch = Mth.clamp(pitch, -Mth.HALF_PI + 0.001f, Mth.HALF_PI - 0.001f);
        return this;
    }

    public SchemaWidget yaw(float yaw) {
        this.yaw = (yaw + Mth.TWO_PI) % Mth.TWO_PI;
        return this;
    }

    public SchemaWidget offset(float x, float y, float z) {
        this.offset.set(x, y, z);
        return this;
    }

    public SchemaWidget enableDragRotation(boolean enable) {
        this.enableRotation = enable;
        return this;
    }

    public SchemaWidget enableDragTranslation(boolean enable) {
        this.enableTranslation = enable;
        return this;
    }

    public SchemaWidget enableScrollScaling(boolean enable) {
        this.enableScaling = enable;
        return this;
    }

    public SchemaWidget enableInteraction(boolean rotation, boolean translation, boolean scaling) {
        return enableDragRotation(rotation)
                .enableDragTranslation(translation)
                .enableScrollScaling(scaling);
    }

    public SchemaWidget enableAllInteraction(boolean enable) {
        return enableInteraction(enable, enable, enable);
    }

    public static class LayerButton extends ButtonWidget<LayerButton> {

        private final int minLayer;
        private final int maxLayer;
        private int currentLayer = Integer.MIN_VALUE;

        public LayerButton(BaseSchemaRenderer schema, int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
            overlay(Text.dynamic(() -> currentLayer > Integer.MIN_VALUE ?
                    Component.literal(Integer.toString(currentLayer)) : Component.literal("ALL")).scale(0.5f));

            onMousePressed((context, button) -> {
                if (button == 0 || button == 1) {
                    if (button == 0) {
                        if (currentLayer == Integer.MIN_VALUE) {
                            currentLayer = minLayer;
                        } else {
                            currentLayer++;
                        }
                    } else {
                        if (currentLayer == Integer.MIN_VALUE) {
                            currentLayer = maxLayer;
                        } else {
                            currentLayer--;
                        }
                    }
                    if (currentLayer > maxLayer || currentLayer < minLayer) {
                        currentLayer = Integer.MIN_VALUE;
                    }
                    schema.notifyRecompile();
                    return true;
                }
                return false;
            });
            schema.updateRenderFilter((blockPos, blockInfo) -> {
                return currentLayer == Integer.MIN_VALUE || currentLayer >= blockPos.getY();
            });
        }

        public LayerButton startLayer(int start) {
            this.currentLayer = start;
            return this;
        }
    }
}
