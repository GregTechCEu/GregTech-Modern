package com.gregtechceu.gtceu.common.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.gregtechceu.gtceu.api.datafixer.LazyDataFixer;
import com.gregtechceu.gtceu.common.datafixer.fixes.AutoOutputTraitFix;
import com.gregtechceu.gtceu.common.datafixer.schemas.*;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.fixes.*;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.api.datafixer.DataFixesInternals.BASE_SCHEMA;

@SuppressWarnings("SameParameterValue")
public class GTDataFixers {

    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    public static void init() {
        if (!ConfigHolder.INSTANCE.compat.doDataFixers) {
            return;
        }

        GTCEu.LOGGER.info("Registering data fixers");

        DataFixer fixer = new LazyDataFixer(() -> {
            DataFixerBuilder builder = new DataFixerBuilder(GTCEu.GT_DATA_VERSION);
            addFixers(builder);

            if (SharedConstants.DATA_FIX_TYPES_TO_OPTIMIZE.isEmpty()) {
                return builder.buildUnoptimized();
            } else {
                Executor executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                        .setNameFormat("GTCEu Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
                return builder.buildOptimized(SharedConstants.DATA_FIX_TYPES_TO_OPTIMIZE, executor);
            }
        });
        DataFixesInternals.get().registerFixer(GTCEu.GT_DATA_VERSION, fixer);
    }

    public static void addFixers(DataFixerBuilder builder) {
        builder.addSchema(0, BASE_SCHEMA);

        Schema schemaV1 = builder.addSchema(1, V1::new);
        createBlockItemRenameFix(builder, schemaV1, "RTM Alloy Coil Block",
                createRenamer("tungstensteel_coil_block", "rtm_alloy_coil_block"));

        builder.addFixer(ItemRenameFix.create(schemaV1, "Advanced Nanomuscle Chestplate rename fix",
                createRenamer("gtceu:avanced_nanomuscle_chestplate", "gtceu:advanced_nanomuscle_chestplate")));

        createBlockItemRenameFix(builder, schemaV1, "Palladium Substation Casing",
                createRenamer("gtceu:palladium_substation", "gtceu:palladium_substation_casing"));

        builder.addFixer(BlockRenameFix.create(schemaV1, "Rename Tungstensteel Fluid Cell",
                createRenamer("gtceu:tungstensteel_fluid_cell", "gtceu:tungsten_steel_fluid_cell")));

        createBlockItemRenameFix(builder, schemaV1, "Rename Low Pressure Steam Miner",
                createRenamer("gtceu:steam_miner", "gtceu:lp_steam_miner"));

        builder.addFixer(ItemRenameFix.create(schemaV1, "Rename electric wire cutters",
                createRenamer(Pattern.compile("([lhi])v_([a-z0-9/._-]+)_wirecutter"), "$1v_$2_wire_cutter")));

        // separator

        Schema schemaV10 = builder.addSchema(10, SAME_NAMESPACED);
        builder.addFixer(new AutoOutputTraitFix(schemaV10));

        /*
        createBlockItemRenameFix(builder, schemaV11, "U238",
                createRenamer(Pattern.compile("gtceu:(.*)uranium_"), "gtceu:$1uranium_238_"));
        createBlockItemRenameFix(builder, schemaV11, "Pu239",
                createRenamer(Pattern.compile("gtceu:(.*)plutonium_"), "gtceu:$1plutonium_239_"));
        createBlockItemRenameFix(builder, schemaV11, "Red Granite",
                createRenamer(Pattern.compile("gtceu:(.*)granite_red"), "gtceu:$1red_granite"));
        createBlockItemRenameFix(builder, schemaV11, "Tungstensteel",
                createRenamer(Pattern.compile("gtceu:(.*)tungstensteel"), "gtceu:$1tungsten_steel"));

        createBlockItemRenameFix(builder, schemaV11, "Raw Oil",
                createRenamer(OilVariantsRenameFix.RENAMED_ITEM_IDS));
        */
    }

    private static void createBlockItemRenameFix(DataFixerBuilder builder, Schema schema, String name,
                                                 UnaryOperator<String> renamer) {
        builder.addFixer(ItemRenameFix.create(schema, "Rename " + name, renamer));
        builder.addFixer(BlockRenameFix.create(schema, "Rename " + name, renamer));
        builder.addFixer(BlockEntityRenameFix.create(schema, "Rename " + name, renamer));
    }

    private static UnaryOperator<String> createRenamer(String oldName, String newName) {
        return id -> Objects.equals(NamespacedSchema.ensureNamespaced(id), oldName) ? newName : id;
    }

    private static UnaryOperator<String> createRenamer(Map<String, String> renameMap) {
        return id -> renameMap.getOrDefault(NamespacedSchema.ensureNamespaced(id), id);
    }

    private static UnaryOperator<String> createRenamer(Pattern check, String replaceWith) {
        return id -> check.matcher(NamespacedSchema.ensureNamespaced(id)).replaceAll(replaceWith);
    }
}