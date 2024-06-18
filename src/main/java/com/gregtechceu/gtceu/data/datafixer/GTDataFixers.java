package com.gregtechceu.gtceu.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.gregtechceu.gtceu.common.datafixer.fixes.GTItemStackComponentizationFix;
import com.gregtechceu.gtceu.common.datafixer.fixes.OilVariantsRenameFix;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.gregtechceu.gtceu.api.datafixer.DataFixesInternals.BASE_SCHEMA;

public class GTDataFixers {

    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    public static void init() {
        if (!ConfigHolder.INSTANCE.compat.doDatafixers) {
            return;
        }

        GTCEu.LOGGER.info("Registering data fixers");

        DataFixesInternals api = DataFixesInternals.get();

        DataFixerBuilder builder = new DataFixerBuilder(GTCEuAPI.GT_DATA_VERSION);
        addFixers(builder);

        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("GTCEuM Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
        DataFixerBuilder.Result result = builder.build();
        result.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST, executor);
        api.registerFixer(GTCEuAPI.GT_DATA_VERSION, result.fixer());
    }

    public static void addFixers(DataFixerBuilder builder) {
        Schema schemaV0 = builder.addSchema(0, BASE_SCHEMA);
        builder.addFixer(new AddNewChoices(schemaV0, "Added GT block entities", References.BLOCK_ENTITY));

        Schema schemaV1 = builder.addSchema(1, SAME_NAMESPACED);
        builder.addFixer(ItemRenameFix.create(schemaV1, "advanced_nanomuscle_chestplate rename fix",
                createRenamer("gtceu:avanced_nanomuscle_chestplate", "gtceu:advanced_nanomuscle_chestplate")));

        builder.addFixer(ItemRenameFix.create(schemaV1, "U238 rename fix",
                createRenamer(Pattern.compile("uranium_"), "uranium_238_")));
        builder.addFixer(ItemRenameFix.create(schemaV1, "Pu239 rename fix",
                createRenamer(Pattern.compile("plutonium_"), "plutonium_239_")));

        builder.addFixer(ItemRenameFix.create(schemaV1, "Raw oil bucket rename fix",
                createRenamer(OilVariantsRenameFix.RENAMED_ITEM_IDS)));
        builder.addFixer(BlockRenameFix.create(schemaV1, "Raw oil block rename fix",
                createRenamer(OilVariantsRenameFix.RENAMED_BLOCK_IDS)));

        builder.addFixer(new GTItemStackComponentizationFix(schemaV1));
    }

    private static UnaryOperator<String> createRenamer(String pOldName, String pNewName) {
        return id -> Objects.equals(NamespacedSchema.ensureNamespaced(id), pOldName) ? pNewName : id;
    }

    private static UnaryOperator<String> createRenamer(Map<String, String> pRenameMap) {
        return id -> pRenameMap.getOrDefault(NamespacedSchema.ensureNamespaced(id), id);
    }

    private static UnaryOperator<String> createRenamer(Pattern check, String replaceWith) {
        return id -> check.matcher(NamespacedSchema.ensureNamespaced(id)).replaceAll(replaceWith);
    }
}
