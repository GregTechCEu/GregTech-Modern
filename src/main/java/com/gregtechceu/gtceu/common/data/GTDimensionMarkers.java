package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class GTDimensionMarkers {

    static {
        REGISTRATE.creativeModeTab(() -> null);
    }

    private static final DeferredRegister<DimensionMarker> DIMENSION_MARKER = DeferredRegister.create(GTRegistries.Keys.DIMENSION_MARKER, "gtceu");

    public static final BlockEntry<Block> OVERWORLD_MARKER = createMarker("overworld");
    public static final BlockEntry<Block> NETHER_MARKER = createMarker("the_nether");
    public static final BlockEntry<Block> END_MARKER = createMarker("the_end");

    public static final RegistryObject<DimensionMarker> OVERWORLD = DIMENSION_MARKER.register("overworld", () -> new DimensionMarker(Level.OVERWORLD, 0,
            () -> OVERWORLD_MARKER, null));

    public static final RegistryObject<DimensionMarker> NETHER = DIMENSION_MARKER.register("nether", () -> new DimensionMarker(Level.NETHER, 0,
            () -> NETHER_MARKER, null));

    public static final RegistryObject<DimensionMarker> END =  DIMENSION_MARKER.register("end", () -> new DimensionMarker(Level.END, 0,
            () -> END_MARKER, null));

    private static BlockEntry<Block> createMarker(String name) {
        return REGISTRATE.block("%s_marker".formatted(name), Block::new)
                .lang(FormattingUtil.toEnglishName(name))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cube(ctx.getName(),
                        prov.modLoc("block/dim_markers/%s/down".formatted(name)),
                        prov.modLoc("block/dim_markers/%s/up".formatted(name)),
                        prov.modLoc("block/dim_markers/%s/north".formatted(name)),
                        prov.modLoc("block/dim_markers/%s/south".formatted(name)),
                        prov.modLoc("block/dim_markers/%s/east".formatted(name)),
                        prov.modLoc("block/dim_markers/%s/west".formatted(name)))
                        .texture("particle", "#north")
                        .guiLight(BlockModel.GuiLight.FRONT)))
                .simpleItem()
                .register();
    }

    public static void init(IEventBus bus) {
        DIMENSION_MARKER.register(bus);
    }
}
