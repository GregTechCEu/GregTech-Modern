package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.VectorUtil;
import com.gregtechceu.gtceu.utils.fakelevel.BaseSchemaRenderer;
import com.gregtechceu.gtceu.utils.fakelevel.ISchema;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import static net.minecraft.util.Mth.TWO_PI;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    private final BaseSchemaRenderer schema;
    private boolean enableRotation = true;
    private boolean enableTranslation = true;
    private boolean enableScaling = true;
    private float lastMouseX;
    private float lastMouseY;
    private float scale = 10f;
    private float pitch = GTMath.PI_QUART;
    private float yaw = 0;
    private final Vector3f offset = new Vector3f();

    public SchemaWidget(ISchema schema) {
        this(new BaseSchemaRenderer(schema));
    }

    public SchemaWidget(BaseSchemaRenderer schema) {
        this.schema = schema;
        schema.cameraFunc((camera, $schema) -> {
            Vector3f focus = VectorUtil.vec3fAdd(this.offset, null, $schema.getFocus());
            camera.setLookAtAndAngle(focus, scale, yaw, pitch);
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        this.schema.drawAtZero(context, getArea(), widgetTheme);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.enableScaling) {
            scale(delta / 120.0);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        this.lastMouseX = getContext().getMouseX();
        this.lastMouseY = getContext().getMouseY();
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int mX = getContext().getAbsMouseX();
        int mY = getContext().getAbsMouseY();
        float dx = (float) mX - lastMouseX;
        float dy = (float) mY - lastMouseY;
        if (button == 0 && this.enableRotation) {
            float moveScale = 0.03f;
            yaw = (yaw + dx * moveScale + TWO_PI) % TWO_PI;
            pitch = Mth.clamp(pitch + dy * moveScale, -TWO_PI / 4 + 0.001f, TWO_PI / 4 - 0.001f);
        } else if (button == 2 && this.enableTranslation) {
            // the idea is to construct a vector which points upwards from the camera pov (y-axis on screen)
            // this vector determines the amount of z offset from mouse movement in y
            float y = (float) Math.cos(pitch);
            float moveScale = 0.09f;
            // with this the offset can be moved by dy
            offset.add(0, dy * y * moveScale, 0);
            // to respect dx we need a new vector which is perpendicular on the previous vector (x-axis on screen)
            // y = 0 => mouse movement in x does not move y
            Vector3f look = this.schema.camera().getLookVec().normalize(); // direction camera is looking
            Vector3f right = look.cross(GTMath.UNIT_Y).normalize(); // right relative to screen
            Vector3f up = right.cross(look);
            this.offset.add(right.mul(-dx * moveScale)).add(up.mul(dy * moveScale));

        }
        this.lastMouseX = (float) mX;
        this.lastMouseY = (float) mY;
    }

    public SchemaWidget scale(double scale) {
        this.scale += scale;
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

        public LayerButton(ISchema schema, int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
            background(GTGuiTextures.BACKGROUND);
            overlay(IKey.dynamic(() -> currentLayer > Integer.MIN_VALUE ?
                    Component.literal(Integer.toString(currentLayer)) : Component.literal("ALL")).scale(0.5f));

            onMousePressed((mouseX, mouseY, button) -> {
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
                    return true;
                }
                return false;
            });
            schema.setRenderFilter(
                    (blockPos, blockInfo) -> currentLayer == Integer.MIN_VALUE || currentLayer >= blockPos.getY());
        }

        public LayerButton startLayer(int start) {
            this.currentLayer = start;
            return this;
        }
    }
}
