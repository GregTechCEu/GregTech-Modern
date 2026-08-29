package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class GTDimensionMarkers {

    static {
        REGISTRATE.resetCreativeModeTab();
    }

    private static final DeferredRegister<DimensionMarker> BASE_MC_DIMENSION_MARKERS = DeferredRegister.create(GTRegistries.Keys.DIMENSION_MARKER, "minecraft");

    public static final BlockEntry<Block> OVERWORLD_MARKER = createMarker("overworld");
    public static final BlockEntry<Block> NETHER_MARKER = createMarker("the_nether");
    public static final BlockEntry<Block> END_MARKER = createMarker("the_end");

    public static final Holder<DimensionMarker> OVERWORLD = BASE_MC_DIMENSION_MARKERS.register("overworld", () -> new DimensionMarker(0, () -> OVERWORLD_MARKER, null));
    public static final Holder<DimensionMarker> NETHER = BASE_MC_DIMENSION_MARKERS.register("nether", () -> new DimensionMarker(0, () -> NETHER_MARKER, null));
    public static final Holder<DimensionMarker> END = BASE_MC_DIMENSION_MARKERS.register("end", () -> new DimensionMarker(0, () -> END_MARKER, null));

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

    public static void init(IEventBus modBus) {
        BASE_MC_DIMENSION_MARKERS.register(modBus);
    }
}
