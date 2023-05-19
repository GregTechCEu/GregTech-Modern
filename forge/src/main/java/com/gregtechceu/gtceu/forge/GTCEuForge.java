package com.gregtechceu.gtceu.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.forge.ClientProxyImpl;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.common.forge.CommonProxyImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(GTCEu.MOD_ID)
public class GTCEuForge {
    public GTCEuForge() {
        GTCEu.init();
        DistExecutor.unsafeRunForDist(() -> ClientProxyImpl::new, () -> CommonProxyImpl::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            for (Material material : GTRegistries.MATERIALS.values()) {
                if (tagPrefix.doGenerateItem(material)) {
                    GTCEu.LOGGER.info("registering item model for " + tagPrefix + " of " + material);
                    MaterialIconSet iconSet = material.getMaterialIconSet();
                    MaterialIconType type = tagPrefix.materialIconType();
                    ResourceLocation model = GTCEu.id(String.format("item/material_sets/%s/%s", iconSet.name, type.name()));
                    if (Minecraft.getInstance().getResourceManager().getResource(GTCEu.id(String.format("models/item/material_sets/%s/%s.json", iconSet.name, type.name()))).isPresent()) {
                        event.register(model);
                    }
                    TagPrefixItemRenderer.MODELS.get(type, iconSet).setModelLocation(model);
                }
            }
        }
    }
}
