package com.gregtechceu.gtceu.common.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.gregtechceu.gtceu.common.datafixer.fixes.*;
import com.gregtechceu.gtceu.common.datafixer.schemas.V1;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.api.datafixer.DataFixesInternals.BASE_SCHEMA;

@SuppressWarnings("SameParameterValue")
public class GTDataFixers {

    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    public static void init() {
        if (!ConfigHolder.INSTANCE.compat.doDataFixers) {
            return;
        }

        GTCEu.LOGGER.info("Registering data fixers");

        DataFixesInternals api = DataFixesInternals.get();
        api.createDataFixer("GTCEu", GTCEu.GT_DATA_VERSION, GTDataFixers::addFixers);
    }

    public static void addFixers(DataFixerBuilder builder) {
        Schema schemaV0 = builder.addSchema(0, BASE_SCHEMA);
        builder.addFixer(new AddNewChoices(schemaV0, "Add GT block entities", References.BLOCK_ENTITY));

        Schema schemaV1 = builder.addSchema(1, V1::new);

        Schema schemaV4 = builder.addSchema(4, SAME_NAMESPACED);
        createBlockItemRenameFix(builder, schemaV4, "RTM Alloy Coil Block",
                createRenamer("tungstensteel_coil_block", "rtm_alloy_coil_block"));

        Schema schemaV5 = builder.addSchema(5, SAME_NAMESPACED);
        builder.addFixer(ItemRenameFix.create(schemaV5, "Advanced Nanomuscle Chestplate rename fix",
                createRenamer("gtceu:avanced_nanomuscle_chestplate", "gtceu:advanced_nanomuscle_chestplate")));

        /*
        createBlockItemRenameFix(builder, schemaV5, "U238",
                createRenamer(Pattern.compile("gtceu:(.*)uranium_"), "gtceu:$1uranium_238_"));
        createBlockItemRenameFix(builder, schemaV5, "Pu239",
                createRenamer(Pattern.compile("gtceu:(.*)plutonium_"), "gtceu:$1plutonium_239_"));
        createBlockItemRenameFix(builder, schemaV5, "Red Granite",
                createRenamer(Pattern.compile("gtceu:(.*)granite_red"), "gtceu:$1red_granite"));
        createBlockItemRenameFix(builder, schemaV5, "Tungstensteel",
                createRenamer(Pattern.compile("gtceu:(.*)tungstensteel"), "gtceu:$1tungsten_steel"));

        createBlockItemRenameFix(builder, schemaV5, "Raw Oil",
                createRenamer(OilVariantsRenameFix.RENAMED_ITEM_IDS));
        */

        Schema schemaV6 = builder.addSchema(6, SAME_NAMESPACED);

        createBlockItemRenameFix(builder, schemaV6, "Palladium Substation Casing",
                createRenamer("gtceu:palladium_substation", "gtceu:palladium_substation_casing"));
    }

    private static void createBlockItemRenameFix(DataFixerBuilder builder, Schema schema, String name,
                                                 UnaryOperator<String> renamer) {
        builder.addFixer(ItemRenameFix.create(schema, name + " rename", renamer));
        builder.addFixer(BlockRenameFix.create(schema, name + " rename fix", renamer));
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