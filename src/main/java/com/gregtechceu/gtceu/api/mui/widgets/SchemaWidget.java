package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.drawable.SchemaRenderer;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.utils.VectorUtil;
import com.gregtechceu.gtceu.utils.fakelevel.ISchema;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.util.Mth.PI;
import static net.minecraft.util.Mth.TWO_PI;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    private final SchemaRenderer schema;
    private boolean enableRotation = true;
    private boolean enableTranslation = true;
    private boolean enableScaling = true;
    private float lastMouseX;
    private float lastMouseY;
    private double scale = 10;
    private float pitch = (float) (Math.PI / 4f);
    private float yaw = (float) (Math.PI / 4f);
    private final Vector3f offset = new Vector3f();

    public SchemaWidget(ISchema schema) {
        this(new SchemaRenderer(schema));
    }

    public SchemaWidget(SchemaRenderer schema) {
        this.schema = schema;
        schema.cameraFunc((camera, $schema) -> {
            Vector3f focus = VectorUtil.vec3fAdd(this.offset, null, $schema.getFocus());
            camera.setLookAt(focus, scale, yaw, pitch);
        });
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
        float dx = (float) mouseX - lastMouseX;
        float dy = (float) mouseY - lastMouseY;
        if (mouseX == 0 && this.enableRotation) {
            float moveScale = 0.025f;
            yaw = (yaw + dx * moveScale + TWO_PI) % TWO_PI;
            pitch = Mth.clamp(pitch + dy * moveScale, -TWO_PI / 4 + 0.001f, TWO_PI / 4 - 0.001f);
        } else if (mouseX == 2 && this.enableTranslation) {
            // the idea is to construct a vector which points upwards from the camera pov (y-axis on screen)
            // this vector determines the amount of z offset from mouse movement in y
            float y = (float) Math.cos(pitch);
            float moveScale = 0.06f;
            // with this the offset can be moved by dy
            offset.add(0, dy * y * moveScale, 0);
            // to respect dx we need a new vector which is perpendicular on the previous vector (x-axis on screen)
            // y = 0 => mouse movement in x does not move y
            float phi = (yaw + PI / 2) % TWO_PI;
            float x = (float) Math.cos(phi);
            float z = (float) Math.sin(phi);
            offset.add(dx * x * moveScale, 0, dx * z * moveScale);
        }
        this.lastMouseX = (float) mouseX;
        this.lastMouseY = (float) mouseY;
    }

    public SchemaWidget scale(double scale) {
        this.scale += scale;
        return this;
    }

    public SchemaWidget pitch(float pitch) {
        this.pitch += pitch;
        return this;
    }

    public SchemaWidget yaw(float yaw) {
        this.yaw += yaw;
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

    @Override
    public @Nullable IDrawable getOverlay() {
        return schema;
    }

    public static class LayerButton extends ButtonWidget<LayerButton> {

        private final int minLayer;
        private final int maxLayer;
        private int currentLayer = Integer.MIN_VALUE;

        public LayerButton(ISchema schema, int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
            background(GuiTextures.MC_BACKGROUND);
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
