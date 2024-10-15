package com.gregtechceu.gtceu.integration.map.xaeros;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.GenericMapRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.screens.Screen;

import java.util.Map;

public class XaerosRenderer extends GenericMapRenderer {

    public static final Map<String, OreVeinElement> elements = new Object2ObjectOpenHashMap<>();

    public XaerosRenderer() {
        super();
    }

    public XaerosRenderer(Screen gui) {
        super(gui);
    }

    @Override
    public boolean addMarker(String name, GeneratedVeinMetadata vein) {
        elements.put(name, new OreVeinElement(vein, name));
        return true;
    }

    @Override
    public boolean removeMarker(String name) {
        OreVeinElement marker = elements.remove(name);
        return marker != null;
    }

}
