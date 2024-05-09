package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.guis.compass.GTCompassUIConfig;
import com.gregtechceu.gtceu.api.guis.compass.GTRecipeViewCreator;
import com.gregtechceu.gtceu.api.guis.compass.MultiblockAction;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.data.entities.GTEntityTypes;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote ClientProxy
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy {
    public ClientProxy(IEventBus modBus) {
        init();
        modBus.addListener(ClientProxy::onRegisterEntityRenderers);
    }

    public static void init() {
        RecipeComponent.registerRecipeViewCreator(new GTRecipeViewCreator());
        CompassManager.INSTANCE.registerUIConfig(GTCEu.MOD_ID, new GTCompassUIConfig());
        CompassManager.INSTANCE.registerAction("multiblock", MultiblockAction::new);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);
    }
}
