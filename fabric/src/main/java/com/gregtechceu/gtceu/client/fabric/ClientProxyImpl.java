package com.gregtechceu.gtceu.client.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientCommands;
import com.gregtechceu.gtceu.client.TooltipHelper;
import com.gregtechceu.gtceu.client.renderer.BlockHighLightRenderer;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

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
        // register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            List<LiteralArgumentBuilder<FabricClientCommandSource>> commands = ClientCommands.createClientCommands();
            commands.forEach(dispatcher::register);
        });

        ClientTickEvents.END_CLIENT_TICK.register(listener -> {
            TooltipHelper.onClientTick();
            GTValues.CLIENT_TIME++;
        });

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for (TagPrefix tagPrefix : TagPrefix.values()) {
                for (Material material : GTRegistries.MATERIALS.values()) {
                    if (tagPrefix.doGenerateItem(material)) {
                        GTCEu.LOGGER.info("registering item model for " + tagPrefix + " of " + material);
                        MaterialIconSet iconSet = material.getMaterialIconSet();
                        MaterialIconType type = tagPrefix.materialIconType();
                        ResourceLocation model = GTCEu.id(String.format("item/material_sets/%s/%s", iconSet.name, type.name()));
                        if (Minecraft.getInstance().getResourceManager().getResource(GTCEu.id(String.format("models/item/material_sets/%s/%s.json", iconSet.name, type.name()))).isPresent()) {
                            out.accept(model);
                        }
                        TagPrefixItemRenderer.MODELS.get(type, iconSet).setModelLocation(model);
                    }
                }
            }
        });
    }
}
