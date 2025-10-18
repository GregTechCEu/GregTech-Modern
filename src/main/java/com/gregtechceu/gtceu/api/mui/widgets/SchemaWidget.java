package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.schema.ISchema;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.schemarenderer.BaseSchemaRenderer;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SchemaWidget extends Widget<SchemaWidget> implements Interactable {

    private final BaseSchemaRenderer schema;
    private boolean enableRotation = true;
    private boolean enableTranslation = true;
    private boolean enableScaling = true;
    private float scale = 10f;
    private float pitch = GTMath.QUART_PI;
    private float yaw = 0;
    private final Vector3f offset = new Vector3f();

    public SchemaWidget(ISchema schema) {
        this(new BaseSchemaRenderer(schema));
    }

    public SchemaWidget(BaseSchemaRenderer schemaRenderer) {
        this.schema = schemaRenderer;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        Vec3 f = this.schema.schema().getFocus();
        this.schema.camera().setLookAtAndAngle((float) (f.x + this.offset.x), (float) (f.y + this.offset.y),
                (float) (f.z + this.offset.z), scale, yaw, pitch);
        this.schema.drawAtZero(context, getArea(), widgetTheme);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.enableScaling) {
            incrementScale((float) (-delta / 12.0f));
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float dx = (float) dragX;
        float dy = (float) dragY;
        if (button == InputConstants.MOUSE_BUTTON_LEFT && this.enableRotation) {
            float moveScale = 0.03f;
            yaw(this.yaw + dx * moveScale);
            pitch(this.pitch + dy * moveScale);
        } else if (button == InputConstants.MOUSE_BUTTON_MIDDLE && this.enableTranslation) {
            float moveScale = 0.09f;
            Vector3f look = this.schema.camera().getLookVec().normalize(); // direction camera is looking
            Vector3f right = look.cross(GTMath.UNIT_Y, new Vector3f()).normalize(); // right relative to screen
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

        public LayerButton(ISchema schema, int minLayer, int maxLayer) {
            this.minLayer = minLayer;
            this.maxLayer = maxLayer;
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
