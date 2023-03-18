package com.lowdragmc.gtceu.client.fabric;

import com.lowdragmc.gtceu.client.TooltipsHandler;
import com.lowdragmc.gtceu.client.renderer.BlockHighLightRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;

/**
 * @author KilaBash
 * @date 2023/2/8
 * @implNote ClientProxyImpl
 */
@Environment(EnvType.CLIENT)
public class ClientProxyImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, blockOutlineContext) -> {
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult) {
                BlockHighLightRenderer.renderBlockHighLight(worldRenderContext.matrixStack(), worldRenderContext.camera(), hitResult, worldRenderContext.consumers(), worldRenderContext.tickDelta());
            }
            return true;
        });
        ItemTooltipCallback.EVENT.register(TooltipsHandler::appendTooltips);
    }
}
