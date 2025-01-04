package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.config.ConfigHolder;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.common.minimap.write.MinimapWriter;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.map.WorldMapSession;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;

public class XaerosMapPlugin {

    public static boolean isActive = false;

    public static final Object2BooleanMap<String> OPTIONS = new Object2BooleanOpenHashMap<>();

    public static void init() {
        isActive = true;
    }

    public static void toggleOption(String name, boolean active) {
        OPTIONS.put(name, active);

        MinimapWriter write = BuiltInHudModules.MINIMAP.getCurrentSession().getProcessor().getMinimapWriter();
        DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
        if (dimHighlightHandler != null) {
            dimHighlightHandler.requestRefresh();
        }

        if (ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration &&
                GTCEu.isModLoaded(GTValues.MODID_XAEROS_WORLDMAP)) {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            MapWorld world = session.getMapProcessor().getMapWorld();
            for (MapDimension mapDim : world.getDimensionsList()) {
                mapDim.getHighlightHandler().clearCachedHashes();
            }
        }
    }

    public static boolean getOptionValue(String name) {
        return OPTIONS.getBoolean(name);
    }
}
