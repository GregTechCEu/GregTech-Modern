package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.utils.GradientUtil;

import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.*;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.util.UIState;
import lombok.Getter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.awt.geom.Point2D;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A map renderer for Journeymap, uses Journeymap's own tooltip rendering to fit existing theming better
 */
public class JourneymapRenderer extends GenericMapRenderer {

    protected static final ResourceLocation STONE = new ResourceLocation("block/stone");

    @Getter
    private static final Map<String, Overlay> markers = new Object2ObjectOpenHashMap<>();

    public JourneymapRenderer() {
        super();
    }

    @Override
    public boolean addMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos, ProspectorMode.FluidInfo fluid) {
        IClientAPI api = JourneyMapPlugin.getJmApi();
        if (!api.playerAccepts(GTCEu.MOD_ID, DisplayType.Image)) {
            return false;
        }
        PolygonOverlay marker = createMarker(name, id, dim, pos, fluid);
        markers.put(id, marker);
        if (this.doShowLayer("bedrock_fluids")) {
            try {
                api.show(marker);
            } catch (Exception e) {
                // It never actually throws anything...
                GTCEu.LOGGER.error("Failed to enable marker with name {}", name, e);
            }
        }
        return true;
    }

    @Override
    public boolean addMarker(String name, String id, GeneratedVeinMetadata vein) {
        IClientAPI api = JourneyMapPlugin.getJmApi();
        if (!api.playerAccepts(GTCEu.MOD_ID, DisplayType.Image)) {
            return false;
        }
        MarkerOverlay marker = createMarker(name, id, vein);
        markers.put(id, marker);
        if (this.doShowLayer("ore_veins")) {
            try {
                api.show(marker);
            } catch (Exception e) {
                // It never actually throws anything...
                GTCEu.LOGGER.error("Failed to enable marker with name {}", name, e);
            }
        }
        return true;
    }

    @Override
    public boolean removeMarker(String id) {
        Overlay marker = markers.remove(id);
        if (marker == null) {
            return false;
        }
        IClientAPI api = JourneyMapPlugin.getJmApi();
        api.remove(marker);
        return true;
    }

    @Override
    public boolean doShowLayer(String name) {
        return JourneyMapPlugin.getOptions().showLayer(name);
    }

    @Override
    public void setLayerActive(String name, boolean active) {
        JourneyMapPlugin.getOptions().toggleLayer(name, active);
    }

    private MarkerOverlay createMarker(String name, String id, GeneratedVeinMetadata vein) {
        BlockPos center = vein.center();

        @SuppressWarnings("DataFlowIssue")
        MapImage image = new MapImage(createOreImage(vein));
        image.centerAnchors()
                .setDisplayWidth(ConfigHolder.INSTANCE.compat.minimap.oreIconSize)
                .setDisplayHeight(ConfigHolder.INSTANCE.compat.minimap.oreIconSize);

        MarkerOverlay overlay = new MarkerOverlay(GTCEu.MOD_ID, id, center, image);

        overlay.setDimension(Minecraft.getInstance().player.level().dimension());
        overlay.setLabel("")
                .setTitle(OreRenderLayer.getTooltip(name, vein).stream().map(Component::getString).reduce("", (s1, s2) -> {
                    if (s1.isEmpty()) {
                        return s2;
                    }
                    if (s2.isEmpty()) {
                        return s1;
                    }
                    return String.join("\n", s1, s2);
                }))
                .setOverlayListener(new MarkerListener(vein, name));

        return overlay;
    }

    private static NativeImage createOreImage(GeneratedVeinMetadata vein) {
        Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
        int materialABGR = GradientUtil.argbToAbgr(firstMaterial.getMaterialARGB());

        ResourceLocation layer0 = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);
        TextureAtlasSprite baseTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(layer0);
        if (baseTexture == null) return null;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(STONE);
        if (sprite == null) {
            return null;
        }

        // remember to ignore this AutoCloseable!! if you don't, you'll delete the texture from memory!!
        int width = baseTexture.contents().width();
        int height = baseTexture.contents().height();

        NativeImage result = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int x = 0; x < result.getWidth(); ++x) {
            for (int y = 0; y < result.getHeight(); ++y) {
                int color = sprite.getPixelRGBA(0, x, y);
                result.setPixelRGBA(x, y, color);
                result.blendPixel(x, y, GradientUtil
                        .multiplyBlendWithAlpha(baseTexture.getPixelRGBA(0, x, y), materialABGR));
            }
        }
        if (firstMaterial.getMaterialSecondaryARGB() != -1) {
            int materialSecondaryABGR = GradientUtil.argbToAbgr(firstMaterial.getMaterialSecondaryARGB());
            ResourceLocation layer1 = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(),
                    "layer2", true);
            if (layer1 == null) {
                return result;
            }
            TextureAtlasSprite image2 = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(layer1);

            for (int x = 0; x < result.getWidth(); ++x) {
                for (int y = 0; y < result.getHeight(); ++y) {
                    int color = image2.getPixelRGBA(0, x, y);
                    result.blendPixel(x, y, GradientUtil
                            .multiplyBlendWithAlpha(color, materialSecondaryABGR));
                }
            }
        }
        // always set alpha to 1
        result.applyToAllPixels(color -> color | 0xFF000000);

        return result;
    }

    private PolygonOverlay createMarker(String name, String id, ResourceKey<Level> dim, ChunkPos pos, ProspectorMode.FluidInfo vein) {
        ResourceLocation texture = IClientFluidTypeExtensions.of(vein.fluid()).getStillTexture();
        ShapeProperties shapeProps = new ShapeProperties()
                .setStrokeWidth(2)
                .setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                .setFillColor(IClientFluidTypeExtensions.of(vein.fluid()).getTintColor())
                .setFillOpacity(.4f)
                .setImageLocation(texture);

        MapPolygon polygon = PolygonHelper.createChunkPolygon(pos.x, 0, pos.z);
        var overlay = new PolygonOverlay(GTCEu.MOD_ID, id,
                Minecraft.getInstance().player.level().dimension(),
                shapeProps, polygon);

        overlay.setDimension(Minecraft.getInstance().player.level().dimension());
        overlay.setLabel("")

                .setTitle(FluidRenderLayer.getTooltip(vein).stream().map(Component::getString).reduce("", (s1, s2) -> {
                    if (s1.isEmpty()) {
                        return s2;
                    }
                    if (s2.isEmpty()) {
                        return s1;
                    }
                    return String.join("\n", s1, s2);
                }))
                .setOverlayListener(new MarkerListener(pos, vein, name));

        return overlay;
    }

    /**
     * Listener for events on a MarkerOverlay instance.
     */
    @ParametersAreNonnullByDefault
    private static class MarkerListener implements IOverlayListener {

        private final GeneratedVeinMetadata oreVein;
        private final String label;

        private final ChunkPos fluidCenterPos;
        private final ProspectorMode.FluidInfo fluidInfo;

        private MarkerListener(GeneratedVeinMetadata oreVein, String label) {
            this.oreVein = oreVein;
            this.label = label;

            this.fluidCenterPos = null;
            this.fluidInfo = null;
        }

        private MarkerListener(ChunkPos fluidCenterPos, ProspectorMode.FluidInfo fluidInfo, String label) {
            this.fluidCenterPos = fluidCenterPos;
            this.fluidInfo = fluidInfo;
            this.label = label;
            this.oreVein = null;
        }

        @Override
        public void onActivate(UIState uiState) {}

        @Override
        public void onDeactivate(UIState uiState) {}

        @Override
        public void onMouseMove(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition) {}

        @Override
        public void onMouseOut(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {}

        @Override
        public boolean onMouseClick(UIState uiState, Point2D.Double mousePosition, BlockPos blockPosition, int button,
                                    boolean doubleClick) {
            if (button == 0 && doubleClick) {
                if (oreVein != null) {
                    Material firstMaterial = oreVein.definition().veinGenerator().getAllMaterials().get(0);
                    int color = firstMaterial.getMaterialARGB();

                    BlockPos center = oreVein.center();
                    WaypointManager.toggleWaypoint("ore_veins", label, color,
                            null, center.getX(), center.getY(), center.getZ());
                } else if (fluidCenterPos != null && fluidInfo != null) {
                    int color = IClientFluidTypeExtensions.of(fluidInfo.fluid()).getTintColor();

                    BlockPos center = fluidCenterPos.getMiddleBlockPosition(0);
                    WaypointManager.toggleWaypoint("ore_veins", label, color,
                            null, center.getX(), center.getY(), center.getZ());
                }
                return false;
            }
            return true;
        }

        @Override
        public void onOverlayMenuPopup(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition,
                                       ModPopupMenu modPopupMenu) {
            modPopupMenu.addMenuItem("button.gtceu.mark_as_depleted.name", (b) -> {
                if (oreVein != null) {
                    oreVein.depleted(!oreVein.depleted());
                } else if (fluidInfo != null) {
                    fluidInfo.left(0);
                }
            });
            modPopupMenu.addMenuItem("button.gtceu.toggle_waypoint.name", (b) -> {
                if (oreVein != null) {
                    Material firstMaterial = oreVein.definition().veinGenerator().getAllMaterials().get(0);
                    int color = firstMaterial.getMaterialARGB();
                    BlockPos center = oreVein.center();
                    WaypointManager.toggleWaypoint("ore_veins", label, color,
                            null, center.getX(), center.getY(), center.getZ());
                } else if (fluidCenterPos != null && fluidInfo != null) {
                    int color = IClientFluidTypeExtensions.of(fluidInfo.fluid()).getTintColor();

                    BlockPos center = fluidCenterPos.getMiddleBlockPosition(0);
                    WaypointManager.toggleWaypoint("ore_veins", label, color,
                            null, center.getX(), center.getY(), center.getZ());
                }
            });
        }
    }
}
