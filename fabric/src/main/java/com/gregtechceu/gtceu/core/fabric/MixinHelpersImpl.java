package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.common.data.GTFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.world.inventory.InventoryMenu;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        if (prop == null || !prop.hasFluidSupplier()) return;
        var fluids = GTFluids.MATERIAL_FLUID_FLOWING.get(prop);
        if (fluids != null) {
            FluidRenderHandlerRegistry.INSTANCE.register(fluids.get().getFirst(), fluids.get().getSecond(), new SimpleFluidRenderHandler(prop.getStillTexture(), prop.getFlowTexture(), material.getMaterialRGB()));
            ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> {
                registry.register(prop.getStillTexture());
                registry.register(prop.getFlowTexture());
            });
        }
    }

    public static void addFluidTexture(Material material, AlloyBlastProperty prop) {
        if (prop == null || prop.getFluid() == null) return;
        FluidRenderHandlerRegistry.INSTANCE.register(material.getHotFluid(), new SimpleFluidRenderHandler(GTFluids.AUTO_GENERATED_MOLTEN_TEXTURE, GTFluids.AUTO_GENERATED_MOLTEN_TEXTURE, material.getMaterialRGB()));
        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> {
            registry.register(GTFluids.AUTO_GENERATED_MOLTEN_TEXTURE);
            registry.register(GTFluids.AUTO_GENERATED_MOLTEN_TEXTURE);
        });
    }
}
