package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
@SuppressWarnings("unused")
public class GTDimensionMarker {

    static {
        GTRegistries.DIMENSION_MARKERS.unfreeze();
    }

    public static final DimensionMarker OVERWORLD = createAndRegister(Level.OVERWORLD.location(), 0, () -> GTBlocks.BATTERY_EMPTY_TIER_I, "flat_world_preset.minecraft.overworld");
    public static final DimensionMarker NETHER = createAndRegister(Level.NETHER.location(), 0, () -> GTBlocks.BATTERY_EMPTY_TIER_II, "advancements.nether.root.title");
    public static final DimensionMarker END = createAndRegister(Level.END.location(), 0, () -> GTBlocks.BATTERY_EMPTY_TIER_III, "advancements.end.root.title");

    public static DimensionMarker createAndRegister(ResourceLocation dim, int tier, ResourceLocation itemKey, @Nullable String overrideName) {
        DimensionMarker marker = new DimensionMarker(tier, itemKey, overrideName);
        marker.register(dim);
        return marker;
    }

    public static DimensionMarker createAndRegister(ResourceLocation dim, int tier, Supplier<ItemLike> supplier, @Nullable String overrideName) {
        DimensionMarker marker = new DimensionMarker(tier, supplier, overrideName);
        marker.register(dim);
        return marker;
    }

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerDimensionMarker);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.DIMENSION_MARKERS, DimensionMarker.class));
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.DIMENSION_MARKERS.getRegistryName());
        }
        GTRegistries.DIMENSION_MARKERS.freeze();
    }
}
