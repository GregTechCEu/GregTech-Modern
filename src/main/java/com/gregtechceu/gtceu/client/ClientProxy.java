package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.compass.GTCompassUIConfig;
import com.gregtechceu.gtceu.api.gui.compass.GTRecipeViewCreator;
import com.gregtechceu.gtceu.api.gui.compass.MultiblockAction;
import com.gregtechceu.gtceu.client.renderer.blockentity.GTSignRenderer;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote ClientProxy
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        init();
    }

    public static void init() {
        RecipeComponent.registerRecipeViewCreator(new GTRecipeViewCreator());
        CompassManager.INSTANCE.registerUIConfig(GTCEu.MOD_ID, new GTCompassUIConfig());
        CompassManager.INSTANCE.registerAction("multiblock", MultiblockAction::new);
    }

    @SubscribeEvent
    public void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
        event.registerEntityRenderer(GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);

        event.registerBlockEntityRenderer(GTBlockEntities.GT_SIGN.get(), GTSignRenderer::new);
    }
}
