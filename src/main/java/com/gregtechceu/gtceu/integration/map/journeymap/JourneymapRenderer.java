package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;

import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.*;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.util.UIState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A map renderer for Journeymap, uses Journeymap's own tooltip rendering to fit existing theming better
 */
public class JourneymapRenderer extends GenericMapRenderer {

    private static final Map<String, MarkerOverlay> markers = new Object2ObjectOpenHashMap<>();

    public JourneymapRenderer() {
        super();
    }

    public JourneymapRenderer(Fullscreen gui) {
        super(gui);
    }

    @Override
    protected void renderTooltipInternal(List<Component> tooltip, GuiGraphics graphics, int mouseX, int mouseY) {
        ((Fullscreen) gui).renderWrappedToolTip(graphics, tooltip.stream().map(Component::getVisualOrderText).toList(),
                mouseX, mouseY, ((Fullscreen) gui).getFontRenderer());
    }

    @Override
    public boolean addMarker(String name, GeneratedVeinMetadata vein) {
        IClientAPI api = JourneyMapPlugin.getJmApi();
        if (!api.playerAccepts(GTCEu.MOD_ID, DisplayType.Image)) {
            return false;
        }
        MarkerOverlay marker = createMarker(name, vein);
        markers.put(name, marker);
        try {
            api.show(marker);
        } catch (Exception e) {
            // It never actually throws anything...
            GTCEu.LOGGER.error("Failed to enable marker with name {}", name, e);
        }
        return true;
    }

    @Override
    public boolean removeMarker(String name) {
        MarkerOverlay marker = markers.remove(name);
        if (marker == null) {
            return false;
        }
        IClientAPI api = JourneyMapPlugin.getJmApi();
        api.remove(marker);
        return true;
    }

    // TODO unhardcode
    private MarkerOverlay createMarker(String name, GeneratedVeinMetadata vein) {
        BlockPos center = vein.center();

        @SuppressWarnings("DataFlowIssue")
        MapImage image = new MapImage(createOreImage(vein));
        image.centerAnchors();

        MarkerOverlay overlay = new MarkerOverlay(GTCEu.MOD_ID, name, center, image);

        overlay.setDimension(Minecraft.getInstance().player.level().dimension());
        overlay.setLabel("")
                .setTitle(getTooltip(name, vein).stream().map(Component::getString).reduce("", (s1, s2) -> {
                    if (s1.isEmpty()) {
                        return s2;
                    }
                    if (s2.isEmpty()) {
                        return s1;
                    }
                    return String.join("\n", s1, s2);
                }))
                .setOverlayListener(new MarkerListener(overlay, vein, name));

        return overlay;
    }

    public List<Component> getTooltip(String name, GeneratedVeinMetadata vein) {
        final List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(name));

        for (var filler : vein.definition().veinGenerator().getAllEntries()) {
            filler.getKey().ifLeft(state -> {
                tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                        .append(state.getBlock().getName()));
            }).ifRight(material -> {
                tooltip.add(Component.literal(ConfigHolder.INSTANCE.compat.minimap.oreNamePrefix)
                        .append(TagPrefix.ore.getLocalizedName(material)));
            });
        }
        return tooltip;
    }

    // TODO unhardcode
    static NativeImage createOreImage(GeneratedVeinMetadata vein) {
        Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
        int materialARGB = firstMaterial.getMaterialARGB();

        ResourceLocation layer0 = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);
        TextureAtlasSprite baseTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer0);
        if (baseTexture == null) return null;

        int materialRGBA = GradientUtil.argbToRgba(materialARGB);

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/stone"));
        if (sprite == null) {
            return null;
        }

        int width = baseTexture.contents().width();
        int height = baseTexture.contents().height();

        NativeImage result = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int x = 0; x < result.getWidth(); ++x) {
            for (int y = 0; y < result.getHeight(); ++y) {
                int color = sprite.getPixelRGBA(0, x, y);
                result.setPixelRGBA(x, y, sprite.getPixelRGBA(0, x, y));
                result.blendPixel(x, y, baseTexture.getPixelRGBA(0, x, y));
                result.blendPixel(x, y, GradientUtil.argbToAbgr(GradientUtil
                        .multiplyBlendARGB(GradientUtil.abrgToRgba(color), materialRGBA)));
            }
        }
        if (firstMaterial.getMaterialSecondaryARGB() != -1) {
            int materialSecondaryRGBA = GradientUtil
                    .argbToRgba(firstMaterial.getMaterialSecondaryARGB());
            ResourceLocation layer1 = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), "layer2", true);
            if (layer1 == null) {
                return result;
            }
            TextureAtlasSprite image2 = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(layer1);

            for (int x = 0; x < result.getWidth(); ++x) {
                for (int y = 0; y < result.getHeight(); ++y) {
                    int color = image2.getPixelRGBA(0, x, y);
                    result.blendPixel(x, y, GradientUtil.argbToAbgr(GradientUtil
                            .multiplyBlendARGB(GradientUtil.abrgToRgba(color), materialSecondaryRGBA)));
                }
            }
        }

        return result;
    }

    /**
     * Listener for events on a MarkerOverlay instance.
     */
    private static class MarkerListener implements IOverlayListener {

        private final MarkerOverlay overlay;
        private GeneratedVeinMetadata vein;
        private final String label;

        private MarkerListener(MarkerOverlay overlay, GeneratedVeinMetadata vein, String label) {
            this.overlay = overlay;
            this.vein = vein;
            this.label = label;
        }

        @Override
        public void onActivate(UIState uiState) {
            refresh(uiState);
        }

        @Override
        public void onDeactivate(UIState uiState) {
            refresh(uiState);
        }

        @Override
        public void onMouseMove(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition) {
        }

        @Override
        public void onMouseOut(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
        }

        @Override
        public boolean onMouseClick(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition, int button, boolean doubleClick) {
            if (button == 0 && doubleClick) {
                Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
                int color = firstMaterial.getMaterialARGB();
                ResourceLocation texture = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);

                // TODO unhardcode
                BlockPos center = vein.center();
                WaypointManager.toggleWaypoint("ore_veins", label, color,
                        null, center.getX(), center.getY(), center.getZ(),
                        texture);
                return false;
            }
            return true;
        }

        @Override
        public void onOverlayMenuPopup(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, ModPopupMenu modPopupMenu) {
            modPopupMenu.addMenuItem("button.gtceu.mark_as_depleted.name", (b) -> {
                vein = vein.setDepleted(!vein.depleted());
            });
        }

        /**
         * Reset properties back to originals, scale display size to zoom level
         */
        private void refresh(UIState uiState) {
            float clampedScale = Math.max(uiState.zoom, ConfigHolder.INSTANCE.compat.minimap.oreScaleStop);
            double size = clampedScale * ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

            overlay.getIcon()
                    .setDisplayWidth(size)
                    .setDisplayHeight(size)
                    .setAnchorX(size / 2)
                    .setAnchorY(size);
            overlay.flagForRerender();
        }
    }
}
