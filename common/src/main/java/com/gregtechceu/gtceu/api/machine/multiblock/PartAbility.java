package com.gregtechceu.gtceu.api.machine.multiblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote MultiblockAbility
 * Fine, It's not really needed。It used to specify which blocks are available.
 * Only registered blocks can be used as part of gtceu's multiblock.
 */
public enum PartAbility {
    EXPORT_ITEMS("export_items"),
    IMPORT_ITEMS("import_items"),
    EXPORT_FLUIDS("export_fluids"),
    IMPORT_FLUIDS("import_fluids"),
    INPUT_ENERGY("input_energy"),
    OUTPUT_ENERGY("output_energy"),
    ROTOR_HOLDER("rotor_holder"),
    PUMP_FLUID_HATCH("pump_fluid_hatch"),
    STEAM("steam"),
    STEAM_IMPORT_ITEMS("steam_import_items"),
    STEAM_EXPORT_ITEMS("steam_export_items"),
    MAINTENANCE("maintenance"),
    MUFFLER("muffler"),
    TANK_VALVE("tank_valve"),
    PASSTHROUGH_HATCH("passthrough_hatch");

    /**
     * tier -> available blocks
     */
    private final Int2ObjectMap<Set<Block>> registry = new Int2ObjectOpenHashMap<>();

    @Getter
    private final String name;

    PartAbility(String name) {
        this.name = name;
    }

    public void register(int tier, Block block) {
        registry.computeIfAbsent(tier, T -> new HashSet<>()).add(block);
    }

    public Collection<Block> getAllBlocks() {
        return registry.values().stream().flatMap(Collection::stream).toList();
    }

    public Collection<Block> getBlocks(int... tiers) {
        return registry.int2ObjectEntrySet().stream()
                .filter(entry -> ArrayUtils.contains(tiers, entry.getIntKey()))
                .flatMap(entry -> entry.getValue().stream())
                .toList();
    }

    /**
     * [from, to]
     */
    public Collection<Block> getBlockRange(int from, int to) {
        return registry.int2ObjectEntrySet().stream()
                .filter(entry -> entry.getIntKey() <= to && entry.getIntKey() >= from)
                .flatMap(entry -> entry.getValue().stream())
                .toList();
    }

}
