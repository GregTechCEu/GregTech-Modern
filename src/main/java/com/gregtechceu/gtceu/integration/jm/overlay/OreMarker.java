package com.gregtechceu.gtceu.integration.jm.overlay;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.MarkerOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.TextProperties;
import lombok.Getter;
import mezz.jei.common.config.IClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Thanks for good people from <a href="https://github.com/frank89722/JourneyMapIntegration">Journey Map Integration</a> for figuring out
 * all of JM stuff. And for licensing it under MIT.
 */

public class OreMarker {
    private IClientAPI jmApi;
    private final Minecraft mc = Minecraft.getInstance();

    private IClientConfig clientConfig;


    private Map<BlockPos, MarkerOverlay> markers = new HashMap<>();

    @Getter
    private boolean activated = true;

    @Getter
    private final String buttonLabel = "Ore vein overaly"; //Change this so it can be localized

    public void init(IClientAPI jmApi, IClientConfig clientConfig){
        this.jmApi = jmApi;
        this.clientConfig = clientConfig;
    }

    public void createMapMarker(BlockPos cords, ResourceLocation markerPicture, ResourceLocation veinID){
        final var icon = new MapImage(markerPicture, 32, 32) // maybe look in to adjusting the size based on the provided texture?
            .setAnchorX(0)
            .setAnchorY(0)
            .setDisplayHeight(32)
            .setDisplayWidth(32)
            .setColor(3); //look in to per vein color

        final var textProperties = new TextProperties()
            .setBackgroundOpacity(0.3f)
            .setOpacity(1.0f);

        final MarkerOverlay markerOverlay = new MarkerOverlay(GTCEu.MOD_ID, "ore_vein_" + cords, cords, icon);
        markerOverlay.setLabel(I18n.get(veinID.toLanguageKey().replace("gtceu.", "gtceu.jei.ore_vein."))) // Again **magic** to use the jei Vein names
                     .setTextProperties(textProperties);

        markers.put(cords, markerOverlay);
    }

    public void createMarkersWhenMappingStarted() {
        if(mc.level == null){return;}
        createMapMarker(new BlockPos(0, 0, 0), new ResourceLocation("minecraft:iron_ore"), new ResourceLocation(GTCEu.MOD_ID, "diamond_vein"));
    }

    public void onJMEvent(ClientEvent event){
        switch (event.type){
            case MAPPING_STARTED -> {
                createMarkersWhenMappingStarted();
                GTCEu.LOGGER.info("Journey map opened, adding ore vein overlays");
            }
            case MAPPING_STOPPED -> markers.clear();
        }
    }

}
