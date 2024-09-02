package com.gregtechceu.gtceu.integration.map.layer.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OreRenderLayer extends MapRenderLayer {

    protected static final ResourceLocation STONE = new ResourceLocation("textures/block/stone.png");
    protected List<GeneratedVeinMetadata> visibleVeins = new ArrayList<>();
    protected List<GeneratedVeinMetadata> hoveredVeins = new ArrayList<>();

    // should be shared between all renderer instances
    protected static GeneratedVeinMetadata waypointVein;

    public OreRenderLayer(String key) {
        super(key);
    }

    @Override
    public void render(PoseStack poseStack, GuiGraphics graphics, double cameraX, double cameraZ, float scale) {
        float clampedScale = Math.max(scale, ConfigHolder.INSTANCE.compat.minimap.oreScaleStop);
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        for (GeneratedVeinMetadata vein : visibleVeins) {
            poseStack.pushPose();

            // -> scale = pixels, origin = center of block vein is in
            poseStack.translate(vein.center().getX() + 0.5, vein.center().getZ() + 0.5, 0);
            poseStack.scale(1 / clampedScale, 1 / clampedScale, 1);

            Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
            int materialARGB = firstMaterial.getMaterialARGB();
            float[] colors = DrawUtil.floats(materialARGB);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            ResourceLocation texture = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(),
                    true).withPrefix("textures/").withSuffix(".png");
            graphics.blit(STONE, -iconSize / 2, -iconSize / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);

            RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1);
            graphics.blit(texture, -iconSize / 2, -iconSize / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);

            RenderSystem.setShaderColor(1, 1, 1, 1);
            int borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(materialARGB | 0xFF000000);
            if ((borderColor & 0xFF000000) != 0) {
                int thickness = iconSize / 16;
                graphics.fill(-iconSize / 2, -iconSize / 2, iconSize, thickness, borderColor);
                graphics.fill(-iconSize / 2, -iconSize / 2 - thickness, iconSize, thickness, borderColor);
                graphics.fill(-iconSize / 2, -iconSize / 2, thickness, iconSize, borderColor);
                graphics.fill(-iconSize / 2 - thickness, -iconSize / 2, thickness, iconSize, borderColor);
            }

            if (vein == waypointVein) {
                int thickness = iconSize / 8;
                int color = 0xFFFFD700;

                graphics.fill(-thickness - iconSize / 2, -thickness - iconSize / 2, thickness + iconSize, thickness,
                        color);
                graphics.fill(iconSize / 2, -thickness - iconSize / 2, thickness, thickness + iconSize, color);
                graphics.fill(-thickness - iconSize / 2, -iconSize / 2, thickness, thickness + iconSize, color);
                graphics.fill(-iconSize / 2, iconSize / 2, thickness + iconSize, thickness, color);
            }

            poseStack.popPose();
        }
    }

    @Override
    public void updateVisibleArea(ResourceKey<Level> dimension, int[] visibleBounds) {
        visibleVeins = GTClientCache.instance.getVeinsInArea(dimension, visibleBounds);
    }

    @Override
    public void updateHovered(int mouseX, int mouseY, double cameraX, double cameraZ, double scale) {
        hoveredVeins.clear();
        double clampedScale = Math.max(scale, ConfigHolder.INSTANCE.compat.minimap.oreScaleStop);
        double iconRadius = ConfigHolder.INSTANCE.compat.minimap.oreIconSize / 2.0 * (scale / clampedScale);
        Minecraft mc = Minecraft.getInstance();
        mouseX = mouseX - mc.getWindow().getScreenWidth() / 2;
        mouseY = mouseY - mc.getWindow().getScreenHeight() / 2;
        for (GeneratedVeinMetadata vein : visibleVeins) {
            double scaledVeinX = (vein.center().getX() + 0.5 - cameraX) * scale;
            double scaledVeinZ = (vein.center().getZ() + 0.5 - cameraZ) * scale;
            if (mouseX > scaledVeinX - iconRadius && mouseX < scaledVeinX + iconRadius &&
                    mouseY > scaledVeinZ - iconRadius && mouseY < scaledVeinZ + iconRadius) {
                hoveredVeins.add(vein);
            }
        }
        // topmost vein first
        Collections.reverse(hoveredVeins);
    }

    public static Component getName(GeneratedVeinMetadata vein) {
        //noinspection ConstantValue IDK, it crashed
        if (vein == null || vein.definition() == null) {
            return Component.translatable("gtceu.minimap.ore_vein.depleted");
        }
        return Component.translatable("gtceu.jei.ore_vein." +
                GTOreVeinWidget.getOreName(vein.definition()));
    }

    @Override
    public List<Component> getTooltip() {
        final List<Component> tooltip = new ArrayList<>();
        for (GeneratedVeinMetadata vein : hoveredVeins) {
            tooltip.add(getName(vein));

            for (var filler : vein.definition().veinGenerator().getAllEntries()) {
                filler.getKey().ifLeft(state -> {
                    tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                            .append(state.getBlock().getName()));
                }).ifRight(material -> {
                    tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                            .append(TagPrefix.ore.getLocalizedName(material)));
                });
            }
        }
        return tooltip;
    }

    @Override
    public boolean onActionKey() {
        if (hoveredVeins.isEmpty()) return false;
        // hoveredVeins.get(0).depleted = !hoveredVeins.get(0).depleted;
        return true;
    }

    @Override
    public boolean onDoubleClick() {
        if (hoveredVeins.isEmpty()) return false;
        GeneratedVeinMetadata vein = hoveredVeins.get(0);
        Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
        int color = firstMaterial.getMaterialARGB();
        ResourceLocation texture = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);
        waypointVein = toggleWaypoint(getName(vein).getString(), color, null,
                vein.center().getX(), vein.center().getY(), vein.center().getZ(), texture) ? vein : null;
        return true;
    }
}
