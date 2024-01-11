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
public class PartAbility {
    public static final PartAbility EXPORT_ITEMS = new PartAbility("export_items");
    public static final PartAbility IMPORT_ITEMS = new PartAbility("import_items");
    public static final PartAbility EXPORT_FLUIDS = new PartAbility("export_fluids");
    public static final PartAbility IMPORT_FLUIDS = new PartAbility("import_fluids");
    
    public static final PartAbility EXPORT_FLUIDS_1X = new PartAbility("export_fluids_1x");
    public static final PartAbility IMPORT_FLUIDS_1X = new PartAbility("import_fluids_1x");
    public static final PartAbility EXPORT_FLUIDS_4X = new PartAbility("export_fluids_4x");
    public static final PartAbility IMPORT_FLUIDS_4X = new PartAbility("import_fluids_4x");
    public static final PartAbility EXPORT_FLUIDS_9X = new PartAbility("export_fluids_9x");
    public static final PartAbility IMPORT_FLUIDS_9X = new PartAbility("import_fluids_9x");
    
    public static final PartAbility INPUT_ENERGY = new PartAbility("input_energy");
    public static final PartAbility OUTPUT_ENERGY = new PartAbility("output_energy");
    public static final PartAbility SUBSTATION_INPUT_ENERGY = new PartAbility("substation_input_energy");
    public static final PartAbility SUBSTATION_OUTPUT_ENERGY = new PartAbility("substation_output_energy");
    public static final PartAbility INPUT_KINETIC = new PartAbility("input_kinetic");
    public static final PartAbility OUTPUT_KINETIC = new PartAbility("output_kinetic");
    public static final PartAbility ROTOR_HOLDER = new PartAbility("rotor_holder");
    public static final PartAbility PUMP_FLUID_HATCH = new PartAbility("pump_fluid_hatch");
    public static final PartAbility STEAM = new PartAbility("steam");
    public static final PartAbility STEAM_IMPORT_ITEMS = new PartAbility("steam_import_items");
    public static final PartAbility STEAM_EXPORT_ITEMS = new PartAbility("steam_export_items");
    public static final PartAbility MAINTENANCE = new PartAbility("maintenance");
    public static final PartAbility MUFFLER = new PartAbility("muffler");
    public static final PartAbility TANK_VALVE = new PartAbility("tank_valve");
    public static final PartAbility PASSTHROUGH_HATCH = new PartAbility("passthrough_hatch");
    public static final PartAbility PARALLEL_HATCH = new PartAbility("parallel_hatch");
    public static final PartAbility INPUT_LASER = new PartAbility("input_laser");
    public static final PartAbility OUTPUT_LASER = new PartAbility("output_laser");

    /**
     * tier -> available blocks
     */
    private final Int2ObjectMap<Set<Block>> registry = new Int2ObjectOpenHashMap<>();

    @Getter
    private final String name;

    public PartAbility(String name) {
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
